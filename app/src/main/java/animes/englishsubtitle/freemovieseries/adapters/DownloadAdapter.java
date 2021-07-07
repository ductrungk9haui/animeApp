package animes.englishsubtitle.freemovieseries.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.BuildConfig;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.downloader.PRDownloader;
import com.downloader.Status;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import animes.englishsubtitle.freemovieseries.DetailsActivity;
import animes.englishsubtitle.freemovieseries.utils.PreferenceUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.models.CommonModels;
import animes.englishsubtitle.freemovieseries.models.Work;
import animes.englishsubtitle.freemovieseries.service.DownloadWorkManager;
import animes.englishsubtitle.freemovieseries.utils.Constants;
import animes.englishsubtitle.freemovieseries.utils.ToastMsg;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static animes.englishsubtitle.freemovieseries.DetailsActivity.TAG;
import static com.applovin.sdk.AppLovinSdkUtils.runOnUiThread;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.OriginalViewHolder> {

    private List<CommonModels> mItems = new ArrayList<>();
    private Context mContext;
    private String mFilmName;
    private boolean isDialog;
    private List<Work> mWorks;
    private View v = null;
    private DatabaseHelper mDBHelper;
    public boolean mIsDownloading = false;

    private ServerAdapter.OnItemClickListener mOnItemClickListener;

    private DownloadAdapter.OriginalViewHolder viewHolder;

    private static DownloadAdapter instance;
    private boolean check = false;

    public RewardedAd mrewardedAd;
    public static boolean is_hide_subscribe_layout=false;


    public DownloadAdapter(Context ctx, String filmName, List<CommonModels> items, boolean isDialog) {
        this.mContext = ctx;
        this.mItems = items;
        this.isDialog = isDialog;
        this.mFilmName = filmName;
        mDBHelper = new DatabaseHelper(ctx);
        mWorks = mDBHelper.getAllWork();

    }


    @Override
    public DownloadAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DownloadAdapter.OriginalViewHolder vh;
        if (isDialog) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_download_item_vertical, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_download_item, parent, false);
        }
        vh = new DownloadAdapter.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final DownloadAdapter.OriginalViewHolder holder, final int position) {

        final CommonModels obj = mItems.get(position);
        holder.name.setText(obj.getTitle());
        holder.resolution.setText(obj.getResulation());
        holder.size.setText(obj.getFileSize());
        //DetailsActivity.getInstance().loadAdReward();
        if (mWorks.size() > 0) {
            for (Work work : mWorks) {
                if (work.getUrl().equals(obj.getStremURL())) {
                    holder.icon.setColorFilter(ContextCompat.getColor(mContext, R.color.primary_color));
                }
            }
        }
        if (isExistFile(obj.getTitle())) {
            holder.icon.setColorFilter(ContextCompat.getColor(mContext, R.color.primary_color));
        }
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log.d("Hoan-sub-sub", String.valueOf(obj.getListSub()));
                epiPaidControl(obj.getIs_epi_download_paid());
                if(is_hide_subscribe_layout!=true){
                    holder.icon.setColorFilter(ContextCompat.getColor(mContext, R.color.primary_color));
                    if (obj.isInAppDownload()) {
                        //Toast.makeText(mContext, "Download after ad finish (5s)", Toast.LENGTH_SHORT).show();
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    downloadFileInsideApp(obj);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 0);

                    } else {
                        String url = obj.getStremURL();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        mContext.startActivity(i);
                    }
                }
            }
        });
    }

    private void epiPaidControl(String isPaid) {
        if (isPaid.equals("1")) {
            if (PreferenceUtils.isLoggedIn(DetailsActivity.getInstance())) {
                if (PreferenceUtils.isActivePlan(DetailsActivity.getInstance())) {
                    if (PreferenceUtils.isValid(DetailsActivity.getInstance())) {
                        //mContentDetails.setVisibility(VISIBLE);
                        DetailsActivity.getInstance().mSubscriptionLayout.setVisibility(GONE);
                        //Log.e("SUBCHECK", "validity: " + PreferenceUtils.isValid(DetailsActivity.this));

                    } else {
                        Log.e("SUBCHECK", "not valid");
                            /*contentDetails.setVisibility(GONE);
                            subscriptionLayout.setVisibility(VISIBLE);*/
                        PreferenceUtils.updateSubscriptionStatus(DetailsActivity.getInstance());
                        //paidControl(isPaid);
                    }
                } else {
                    Log.e("SUBCHECK", "not active plan");
                    is_hide_subscribe_layout=true;
                    DetailsActivity.getInstance().mContentDetails.setVisibility(GONE);
                    DetailsActivity.getInstance().hideDownloadServerDialog();
                    DetailsActivity.getInstance().mSubscriptionLayout.setVisibility(VISIBLE);
                }
            }
        }
    }



    public static DownloadAdapter getInstance() {
        return instance;
    }

    @SuppressLint("RestrictedApi")
    public void downloadFileInsideApp(CommonModels obj) throws Exception {

        String fileName = mFilmName + "_" + obj.getTitle();
        String streamURL = obj.getStremURL();
        String namefilm=mFilmName;
        if (streamURL == null || streamURL.isEmpty()) {
            return;
        }
        String path = Constants.getDownloadDir(mContext) + mContext.getResources().getString(R.string.app_name);
        String fileExt;
        if(!streamURL.substring(streamURL.lastIndexOf('.')).equals("mp4") && !streamURL.substring(streamURL.lastIndexOf('.')).equals("m3u8")){
            fileExt=".m3u8";
        }else {
            fileExt = streamURL.substring(streamURL.lastIndexOf('.'));; // output like .mkv
        }

        fileName = fileName + fileExt;

        fileName = fileName.replaceAll(" ", "_");
        fileName = fileName.replaceAll(":", "_");
        fileName = fileName.replaceAll("'", "_");

        File file = new File(path, fileName); // e_ for encode
        //Log.d("Hoan",file.getAbsolutePath());
        if (file.exists()) {
            new ToastMsg(mContext).toastIconError("File already exist.");
        } else {
            String dir = mContext.getExternalCacheDir().toString();
            String workId;
            workId = streamURL.replaceAll(" ", "_");
            //workId = workId.replaceAll(":", "_");
            workId = workId.replaceAll("'", "_");
            List<Work> workList = mDBHelper.getAllWork();
            for (Work w : workList) {
                if (w.getUrl().equals(streamURL)) {
                    new ToastMsg(mContext).toastIconError("File is added to download.");
                    return;
                }
            }
            Work work = new Work();
            work.setUrl(streamURL);
            work.setWorkId(workId);
            work.setFileName(fileName);
            work.setMovieName(namefilm);
            work.setDir(dir);
            work.setSubList(obj.getListSub());
            work.setDownloadStatus(mContext.getResources().getString(R.string.download_waiting));
            mDBHelper.insertWork(work);
            new ToastMsg(mContext).toastIconSuccess("Added " + fileName + " to download");
            Log.d("TRUNG", "insertWork download " + fileName + " id: " + work.getWorkId());
            if (isDownloading() || mIsDownloading) return;

            mIsDownloading = true;

            Data data;

            if(fileExt.equals(".m3u8")){
                data = new Data.Builder()
                        .putString("url", streamURL)
                        .putString("type", ".m3u8")
                        .putString("fileName", fileName)
                        .build();
                Log.d("Hoan-check",fileName);
            }else {
                data = new Data.Builder()
                        .putString("url", streamURL)
                        .putString("type", ".mp4")
                        .putString("fileName", fileName)
                        .build();
                Log.d("Hoan-check","mp4");
            }




            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadWorkManager.class)
                    .setInputData(data)
                    .build();

            // String workId = request.getId().toString();
            //  Constants.workId = workId;
            WorkManager.getInstance(mContext).enqueue(request);
            new ToastMsg(mContext).toastIconSuccess("Download started");
            Log.d("TRUNG", "Request download " + fileName);
        }
    }


    private boolean isExistFile(String title) {
        String fileName = title;
        String path = Constants.getDownloadDir(mContext) + mContext.getResources().getString(R.string.app_name);

        String fileExt = ".mp4"; // output like .mkv
        fileName = fileName + fileExt;

        fileName = fileName.replaceAll(" ", "_");
        fileName = fileName.replaceAll(":", "_");
        fileName = fileName.replaceAll("'", "_");

        File file = new File(path, fileName); // e_ for encode
        return file.exists();
    }

    private boolean isDownloading() {
        List<Work> works = mDBHelper.getAllWork();
        for (Work work : works) {
            if (work.getDownloadId() == 0 && !work.getDownloadStatus().equals(mContext.getString(R.string.download_waiting))) {
                return true;
            }
            if (PRDownloader.getStatus(work.getDownloadId()) == Status.RUNNING) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.resolution_tv)
        TextView resolution;
        @BindView(R.id.size_tv)
        TextView size;
        @BindView(R.id.item_layout)
        LinearLayout itemLayout;
        @BindView(R.id.icon)
        ImageView icon;

        public OriginalViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

}