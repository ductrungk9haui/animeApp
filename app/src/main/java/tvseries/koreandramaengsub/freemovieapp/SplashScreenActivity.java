package tvseries.koreandramaengsub.freemovieapp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.network.RetrofitClient;
import tvseries.koreandramaengsub.freemovieapp.network.apis.ConfigurationApi;
import tvseries.koreandramaengsub.freemovieapp.network.model.config.ApkUpdateInfo;
import tvseries.koreandramaengsub.freemovieapp.network.model.config.Configuration;
import tvseries.koreandramaengsub.freemovieapp.utils.ApiResources;
import tvseries.koreandramaengsub.freemovieapp.utils.Constants;
import tvseries.koreandramaengsub.freemovieapp.utils.PreferenceUtils;
import tvseries.koreandramaengsub.freemovieapp.utils.ToastMsg;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreen";
    private final int PERMISSION_REQUEST_CODE = 100;
    private int SPLASH_TIME = 1500;
    private Thread timer;
    private DatabaseHelper db;
    AnimationDrawable anim;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splashscreen);
        db = new DatabaseHelper(SplashScreenActivity.this);

        imageView=(ImageView)findViewById(R.id.splash);
        if(imageView==null)throw new AssertionError();
        imageView.setBackgroundResource(R.drawable.loading);

        anim=(AnimationDrawable)imageView.getBackground();
        anim.start();

        //print keyHash for facebook login
       // createKeyHash(SplashScreenActivity.this, BuildConfig.APPLICATION_ID);


        // checking storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkStoragePermission()) {
                getConfigurationData();
            }
        } else {
            getConfigurationData();
        }

        timer = new Thread() {
            public void run() {
                try {
                    sleep(SPLASH_TIME);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    if (PreferenceUtils.isLoggedIn(SplashScreenActivity.this)) {
                        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        finish();
                    } else {

                        if (isLoginMandatory()) {
                            Intent intent = new Intent(SplashScreenActivity.this, FirebaseSignUpActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                            finish();
                        }
                    }

                }
            }
        };

    }

    public boolean isLoginMandatory() {
        return db.getConfigurationData().getAppConfig().getMandatoryLogin();
    }

    public void getConfigurationData() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ConfigurationApi api = retrofit.create(ConfigurationApi.class);
        Call<Configuration> call = api.getConfigurationData(Config.API_KEY);
        call.enqueue(new Callback<Configuration>() {
            @Override

            public void onResponse(Call<Configuration> call, Response<Configuration> response) {
                if (response.code() == 200) {
                    Configuration configuration = response.body();
                    if (configuration != null) {

                        ApiResources.CURRENCY = configuration.getPaymentConfig().getCurrency();
                        ApiResources.PAYPAL_CLIENT_ID = configuration.getPaymentConfig().getPaypalClientId();
                        ApiResources.EXCHSNGE_RATE = configuration.getPaymentConfig().getExchangeRate();
                        ApiResources.RAZORPAY_EXCHANGE_RATE = configuration.getPaymentConfig().getRazorpayExchangeRate();
                        //save genre, country and tv category list to constants
                        Constants.genreList = configuration.getGenre();
                        Constants.countryList = configuration.getCountry();
                        Constants.tvCategoryList = configuration.getTvCategory();

                        db.deleteAllDownloadData();
                        db.deleteAllAppConfig();
                        db.insertConfigurationData(configuration);
                        //apk update check
                        if (isNeedUpdate(configuration.getApkUpdateInfo().getVersionCode())) {
                            showAppUpdateDialog(configuration.getApkUpdateInfo());
                            return;
                        }

                        if (db.getConfigurationData() != null) {
                            timer.start();
                        } else {
                            showErrorDialog(getString(R.string.error_toast), getString(R.string.no_configuration_data_found));
                        }
                    } else {
                        showErrorDialog(getString(R.string.error_toast), getString(R.string.failed_to_communicate));
                    }
                } else {
                    showErrorDialog(getString(R.string.error_toast), getString(R.string.failed_to_communicate));
                }
            }

            @Override
            public void onFailure(Call<Configuration> call, Throwable t) {
                Log.e("ConfigError", t.getLocalizedMessage());
                showErrorDialog(getString(R.string.error_toast), getString(R.string.failed_to_communicate));
            }
        });
    }

    private void showAppUpdateDialog(final ApkUpdateInfo info) {
        new AlertDialog.Builder(this)
                .setTitle("New version: " + info.getVersionName())
                .setMessage(info.getWhatsNew())
                .setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //update clicked
                        dialog.dismiss();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.getApkUrl()));
                        startActivity(browserIntent);
                        finish();
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //exit clicked
                        if (info.isSkipable()) {
                            if (db.getConfigurationData() != null) {
                                timer.start();
                            } else {
                                new ToastMsg(SplashScreenActivity.this).toastIconError(getString(R.string.error_toast));
                                finish();
                            }
                        } else {
                            System.exit(0);
                        }
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }


    private boolean isNeedUpdate(String versionCode) {
        return Integer.parseInt(versionCode) > BuildConfig.VERSION_CODE;
    }

    // ------------------ checking storage permission ------------
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                Log.v(TAG, "Permission is granted");
                return true;

            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            //resume tasks needing this permission
            getConfigurationData();
        }
    }

    public static void createKeyHash(Activity activity, String yourPackage) {
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(yourPackage, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
