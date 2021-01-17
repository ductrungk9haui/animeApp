package tvseries.koreandramaengsub.freemovieapp;

import android.Manifest;
import android.animation.Animator;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.volcaniccoder.bottomify.BottomifyNavigationView;
import com.volcaniccoder.bottomify.OnNavigationItemChangeListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;
import tvseries.koreandramaengsub.freemovieapp.adapters.NavigationAdapter;
import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.fragments.DownFragment;
import tvseries.koreandramaengsub.freemovieapp.fragments.HomeFragment;
import tvseries.koreandramaengsub.freemovieapp.fragments.MoviesFragment;
import tvseries.koreandramaengsub.freemovieapp.fragments.TvSeriesFragment;
import tvseries.koreandramaengsub.freemovieapp.models.NavigationModel;
import tvseries.koreandramaengsub.freemovieapp.nav_fragments.CountryFragment;
import tvseries.koreandramaengsub.freemovieapp.nav_fragments.FavoriteFragment;
import tvseries.koreandramaengsub.freemovieapp.nav_fragments.GenreFragment;
import tvseries.koreandramaengsub.freemovieapp.utils.Constants;
import tvseries.koreandramaengsub.freemovieapp.utils.PreferenceUtils;
import tvseries.koreandramaengsub.freemovieapp.utils.RtlUtils;
import tvseries.koreandramaengsub.freemovieapp.utils.SpacingItemDecoration;
import tvseries.koreandramaengsub.freemovieapp.utils.Tools;
import tvseries.koreandramaengsub.freemovieapp.utils.ads.PopUpAds;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Serializable {
    @BindView(R.id.search_root_layout)
    LinearLayout mSearchRootLayout;
    @BindView(R.id.search_bar)
    CardView mSearchBar;
    @BindView(R.id.bt_menu)
    ImageView mMenuIv;
    @BindView(R.id.page_title_tv)
    TextView mPageTitle;
    @BindView(R.id.search_iv)
    ImageView mSearchIv;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_head_layout)
    LinearLayout mNavHeaderLayout;
    @BindView(R.id.theme_switch)
    SwitchCompat mThemeSwitch;
    @BindView(R.id.group_facebook)
    RelativeLayout mGroupFBLayout;
    @BindView(R.id.rate_app)
    RelativeLayout mRateApp;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.coordinator_lyt)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.tv_noitem)
    TextView mTvNoItem;
    @BindView(R.id.bottomify_nav)
    BottomifyNavigationView mBottomNaviDark;
    @BindView(R.id.bottomify_nav_light)
    BottomifyNavigationView mBottomNaviLight;

    Unbinder mUnbinder;
    boolean isSearchBarHide = false;
    private NavigationAdapter mAdapter;
    private List<NavigationModel> mListNavi = new ArrayList<>();
    private boolean mStatus = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    public boolean isDark;
    private String navMenuStyle;
    private final int PERMISSION_REQUEST_CODE = 100;
    private DatabaseHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        mDBHelper = new DatabaseHelper(MainActivity.this);
        navMenuStyle = mDBHelper.getConfigurationData().getAppConfig().getMenu();

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", true);
        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);
        setupTheme();
        if (PreferenceUtils.isLoggedIn(MainActivity.this)) {
            if (!PreferenceUtils.isActivePlan(MainActivity.this)) {
                PopUpAds.ShowAdmobInterstitialAds(MainActivity.this);
            }
        }
        //PopUpAds.ShowAdmobInterstitialAds(MainActivity.this);
        // To resolve cast button visibility problem. Check Cast State when app is open.
        CastContext castContext = CastContext.getSharedInstance(this);
        castContext.getCastState();
        setupFirebaseAnalytics();

        if (sharedPreferences.getBoolean("firstTime", true)) {
            showTermServicesDialog();
        }
        checkStorePermission();
        setupNavigation();
        //----external method call--------------
        loadFragment(new HomeFragment());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_action, menu);
        return true;
    }

    private boolean loadFragment(Fragment fragment) {
        setFailure(false);
        if (fragment != null) {
            if (fragment instanceof HomeFragment) {
                NaviSelected(mAdapter.getViewHolder(0),0);
            }else if(fragment instanceof MoviesFragment){
                NaviSelected(mAdapter.getViewHolder(1),1);
            }else if(fragment instanceof  TvSeriesFragment){
                NaviSelected(mAdapter.getViewHolder(2),2);
            }else if(fragment instanceof  FavoriteFragment){
                NaviSelected(mAdapter.getViewHolder(6),6);
            }else{
               mBottomNaviDark.clearSelection();
               mBottomNaviLight.clearSelection();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();


            return true;
        }
        return false;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_search:

                final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {

                        Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                        intent.putExtra("q", s);
                        startActivity(intent);

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else if (Config.isOpenChildFragment) {
            loadFragment(new HomeFragment());
            Config.isOpenChildFragment = false;
        } else {
            new AlertDialog.Builder(MainActivity.this).setMessage("Do you want to exit ?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();

        }
    }


    //----nav menu item click---------------
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // set item as selected to persist highlight
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
        return true;
    }

    private void showTermServicesDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_term_of_services);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        Button declineBt = dialog.findViewById(R.id.bt_decline);
        Button acceptBt = dialog.findViewById(R.id.bt_accept);

        if (isDark) {
            declineBt.setBackground(getResources().getDrawable(R.drawable.btn_rounded_grey_outline));
            acceptBt.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
        }

        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        acceptBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
                editor.putBoolean("firstTime", false);
                editor.apply();
                dialog.dismiss();
            }
        });

        declineBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    private boolean checkStoragePermission() {
        // checking storage permission
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");

                    // creating the download directory named oxoo
                    createDownloadDir();

                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    // creating download folder
    public void createDownloadDir() {
        File file = new File(Constants.getDownloadDir(MainActivity.this), getResources().getString(R.string.app_name));
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public void goToSearchActivity() {
        startActivity(new Intent(MainActivity.this, SearchActivity.class));
    }

    private Intent rateIntentForUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21) {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        } else {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

    private void setupBottomNaviBar() {
        if (isDark) {
            mBottomNaviDark.setVisibility(View.VISIBLE);
            mBottomNaviDark.setBackgroundColor(getResources().getColor(R.color.black_window_light));

            mBottomNaviDark.setActiveNavigationIndex(0);
            mBottomNaviDark.setOnNavigationItemChangedListener(new OnNavigationItemChangeListener() {
                @Override
                public void onNavigationItemChanged(@NotNull BottomifyNavigationView.NavigationItem navigationItem) {
                    switch (navigationItem.getPosition()) {
                        case 0:
                            loadFragment(new HomeFragment());
                            break;
                        case 1:
                            loadFragment(new MoviesFragment());
                            Config.isOpenChildFragment = true;
                            break;
                        case 2:
                            loadFragment(new TvSeriesFragment());
                            Config.isOpenChildFragment = true;
                            break;
                        case 3:
                            loadFragment(new DownFragment());
                            Config.isOpenChildFragment = true;
                            break;
                        case 4:
                            loadFragment(new FavoriteFragment());
                            Config.isOpenChildFragment = true;
                            break;
                    }
                }
            });
        } else {
            mBottomNaviLight.setVisibility(View.VISIBLE);
            mBottomNaviLight.setBackgroundColor(getResources().getColor(R.color.white));

            mBottomNaviLight.setActiveNavigationIndex(0);
            mBottomNaviLight.setOnNavigationItemChangedListener(new OnNavigationItemChangeListener() {
                @Override
                public void onNavigationItemChanged(@NotNull BottomifyNavigationView.NavigationItem navigationItem) {
                    switch (navigationItem.getPosition()) {
                        case 0:
                            loadFragment(new HomeFragment());
                            break;
                        case 1:
                            loadFragment(new MoviesFragment());
                            Config.isOpenChildFragment = true;
                            break;
                        case 2:
                            loadFragment(new TvSeriesFragment());
                            Config.isOpenChildFragment = true;
                            break;
                        case 3:
                            loadFragment(new DownFragment());
                            Config.isOpenChildFragment = true;
                            break;
                        case 4:
                            loadFragment(new FavoriteFragment());
                            Config.isOpenChildFragment = true;
                            break;
                    }
                }
            });
        }
    }

    public void animateSearchBar(final boolean hide) {
        if (isSearchBarHide && hide || !isSearchBarHide && !hide) return;
        isSearchBarHide = hide;
        int moveY = hide ? -(2 * mSearchRootLayout.getHeight()) : 0;
        mSearchRootLayout.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }

    public void setTitle(String title) {
        mPageTitle.setText(title);
        mSearchRootLayout.setTranslationY(0);
    }

    @OnClick(R.id.bt_menu)
    void onMenuIvClick(View view) {
        openDrawer();
    }

    @OnClick(R.id.search_iv)
    void onSearchIvClick(View view) {
        startActivity(new Intent(MainActivity.this, SearchActivity.class));
    }

    @OnClick(R.id.group_facebook)
    void onGroupFacebookClick(View view) {
        String urlString = "https://www.facebook.com/Korean-Drama-Engsub-112928027254307";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @OnClick(R.id.rate_app)
    void onRateApp(View view) {
        try {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    @OnCheckedChanged(R.id.theme_switch)
    void onThemeSwitchChange(boolean isChecked) {
        SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
        editor.putBoolean("dark", isChecked);
        editor.apply();
        if (isDark != isChecked) {
            mDrawerLayout.closeDrawers();
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            finish();
        }
    }

    public DatabaseHelper getDBHelper() {
        return mDBHelper;
    }

    private void setupTheme() {
        if (isDark) {
            mMenuIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu));
            mSearchIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_white));
            mThemeSwitch.setChecked(true);
            mNavHeaderLayout.setBackgroundColor(getResources().getColor(R.color.nav_head_bg));
        } else {
            mThemeSwitch.setChecked(false);
            mNavHeaderLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        setupBottomNaviBar();
    }

    private void setupFirebaseAnalytics() {
        //---analytics-----------
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "main_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void checkStorePermission() {
        // checking storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkStoragePermission()) {
                createDownloadDir();
            } else {
                requestPermission();
            }
        } else {
            createDownloadDir();
        }
    }

    final NavigationAdapter.OriginalViewHolder[] viewHolder = {null};

    private void setupNavigation() {
        //----navDrawer------------------------
        mNavigationView.setNavigationItemSelectedListener(this);
        //----fetch array------------
        String[] navItemName = getResources().getStringArray(R.array.nav_item_name);
        String[] navItemImage = getResources().getStringArray(R.array.nav_item_image);
        String[] navItemImage2 = getResources().getStringArray(R.array.nav_item_image_2);
        String[] navItemName2 = getResources().getStringArray(R.array.nav_item_name_2);

        //----navigation view items---------------------
        if (navMenuStyle == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

        } else if (navMenuStyle.equals("grid")) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            recyclerView.addItemDecoration(new SpacingItemDecoration(2, Tools.dpToPx(this, 15), true));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        }
        recyclerView.setHasFixedSize(true);

        mStatus = PreferenceUtils.isLoggedIn(this);
        if (mStatus) {
            PreferenceUtils.updateSubscriptionStatus(MainActivity.this);
            for (int i = 0; i < navItemName.length; i++) {
                NavigationModel models = new NavigationModel(navItemImage[i], navItemName[i]);
                mListNavi.add(models);
            }
        } else {
            for (int i = 0; i < navItemName2.length; i++) {
                NavigationModel models = new NavigationModel(navItemImage2[i], navItemName2[i]);
                mListNavi.add(models);
            }
        }


        //set data and list adapter
        mAdapter = new NavigationAdapter(this, mListNavi, navMenuStyle);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new NavigationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, NavigationModel obj, int position, NavigationAdapter.OriginalViewHolder holder) {
                //----------------------action for click items nav---------------------
                if (position == 0) {
                    if (isDark) {
                        mBottomNaviDark.setActiveNavigationIndex(0);
                    } else {
                        mBottomNaviLight.setActiveNavigationIndex(0);
                    }

                } else if (position == 1) {
                    if (isDark) {
                        mBottomNaviDark.setActiveNavigationIndex(1);
                    } else {
                        mBottomNaviLight.setActiveNavigationIndex(1);
                    }
                    Config.isOpenChildFragment = true;
                } else if (position == 2) {
                    if (isDark) {
                        mBottomNaviDark.setActiveNavigationIndex(2);
                    } else {
                        mBottomNaviLight.setActiveNavigationIndex(2);
                    }
                    Config.isOpenChildFragment = true;
                } else if (position == 3) {
                    loadFragment(new GenreFragment());
                    Config.isOpenChildFragment = true;
                } else if (position == 4) {
                    loadFragment(new CountryFragment());
                    Config.isOpenChildFragment = true;
                } else {
                    if (mStatus) {

                        if (position == 5) {
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            startActivity(intent);
                        } else if (position == 6) {
                            if (isDark) {
                                mBottomNaviDark.setActiveNavigationIndex(4);
                            } else {
                                mBottomNaviLight.setActiveNavigationIndex(4);
                            }
                            Config.isOpenChildFragment = true;
                        } else if (position == 7) {
                            Intent intent = new Intent(MainActivity.this, SubscriptionActivity.class);
                            startActivity(intent);
                        } else if (position == 8) {
                            Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                            startActivity(intent);
                        } else if (position == 9) {
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        } else if (position == 10) {

                            new AlertDialog.Builder(MainActivity.this).setMessage("Are you sure to logout ?")
                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            if (user != null) {
                                                FirebaseAuth.getInstance().signOut();
                                            }

                                            SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                                            editor.putBoolean(Constants.USER_LOGIN_STATUS, false);
                                            editor.apply();
                                            editor.commit();

                                            DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                                            databaseHelper.deleteUserData();

                                            PreferenceUtils.clearSubscriptionSavedData(MainActivity.this);

                                            Intent intent = new Intent(MainActivity.this, FirebaseSignUpActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    }).create().show();
                        }

                    } else {
                        if (position == 5) {
                            Intent intent = new Intent(MainActivity.this, FirebaseSignUpActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (position == 6) {
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }

                    }

                }


                //----behaviour of bg nav items-----------------
                if (!obj.getTitle().equals("Settings") && !obj.getTitle().equals("Login") && !obj.getTitle().equals("Sign Out")) {
                    NaviSelected(holder, position);
                }

                mDrawerLayout.closeDrawers();
            }
        });
    }

    private void NaviSelected(NavigationAdapter.OriginalViewHolder holder, int position) {
        if(holder==null)return;
        if (isDark) {
            mAdapter.chanColor(viewHolder[0], position, R.color.nav_bg);
        } else {
            mAdapter.chanColor(viewHolder[0], position, R.color.white);
        }

        if (navMenuStyle.equals("grid")) {
            holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            holder.name.setTextColor(getResources().getColor(R.color.white));
        } else {
            holder.selectedLayout.setBackground(getResources().getDrawable(R.drawable.round_grey_transparent));
            holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        viewHolder[0] = holder;
    }

    public View getToolbar() {
        return mSearchRootLayout;
    }

    public void setFailure(boolean isFailed, String failText) {
        mCoordinatorLayout.setVisibility(isFailed ? View.VISIBLE : View.GONE);
        mTvNoItem.setText(failText);
    }

    public void setFailure(boolean isFailed) {
        mCoordinatorLayout.setVisibility(isFailed ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
