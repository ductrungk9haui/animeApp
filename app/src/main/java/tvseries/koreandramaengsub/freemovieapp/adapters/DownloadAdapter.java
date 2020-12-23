package tvseries.koreandramaengsub.freemovieapp.adapters;

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

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.downloader.PRDownloader;
import com.downloader.Status;
import com.google.android.gms.ads.reward.RewardedVideoAd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tvseries.koreandramaengsub.freemovieapp.DetailsActivity;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.models.CommonModels;
import tvseries.koreandramaengsub.freemovieapp.models.Work;
import tvseries.koreandramaengsub.freemovieapp.service.DownloadWorkManager;
import tvseries.koreandramaengsub.freemovieapp.utils.Constants;
import tvseries.koreandramaengsub.freemovieapp.utils.ToastMsg;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.OriginalViewHolder>{

    private List<CommonModels> mItems = new ArrayList<>();
    private Context mContext;
    private boolean isDialog;
    private List<Work> mWorks;
    private View v = null;
    private DatabaseHelper mDBHelper;
    private boolean mIsDownloading = false;

    private ServerAdapter.OnItemClickListener mOnItemClickListener;

    private DownloadAdapter.OriginalViewHolder viewHolder;

    private static DownloadAdapter instance;
    private boolean check=false;

    public RewardedVideoAd mrewardedAd;



    public DownloadAdapter(Context ctx, List<CommonModels> items,  boolean isDialog) {
        this.mContext = ctx;
        this.mItems = items;
        this.isDialog = isDialog;
        mDBHelper = new DatabaseHelper(ctx);
        mWorks = mDBHelper.getAllWork();

    }

    @Override
    public DownloadAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DownloadAdapter.OriginalViewHolder vh;
        if (isDialog){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_download_item_vertical, parent, false);
        }else {
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
        if(mWorks.size() >0){
            for(Work work :mWorks){
                if(work.getUrl().equals(obj.getStremURL())){
                    holder.icon.setColorFilter(ContextCompat.getColor(mContext, R.color.green_500));
                }
            }
        }
        if(isExistFile(obj.getTitle())) {
            holder.icon.setColorFilter(ContextCompat.getColor(mContext, R.color.green_500));
        }
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.icon.setColorFilter(ContextCompat.getColor(mContext, R.color.green_500));
                if (obj.isInAppDownload()) {
                    //in app download enabled
                    //PopUpAds.ShowAdmobInterstitialAds(ctx);
                   // if(check==true){
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                               // DetailsActivity.getInstance().showRewardedVideo();
                                downloadFileInsideApp(obj.getTitle(), obj.getStremURL());
                            }
                        }, 0);
//                    }
//                    else{
//
//                        DetailsActivity.getInstance().showRewardedVideo();
//                       downloadFileInsideApp(obj.getTitle(), obj.getStremURL());
//                        check=true;
//                    }
                } else {
                    String url = obj.getStremURL();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    mContext.startActivity(i);
                }
            }
        });
    }


    public static DownloadAdapter getInstance() {
        return instance;
    }
    public void downloadFileInsideApp(String title, String streamURL) {
        String fileName = title.toString();
        if (streamURL == null || streamURL.isEmpty()) {
            return;
        }
        String path = Constants.getDownloadDir(mContext) + mContext.getResources().getString(R.string.app_name);

        String fileExt = ".mp4"; // output like .mkv
        fileName = fileName + fileExt;

        fileName = fileName.replaceAll(" ", "_");
        fileName = fileName.replaceAll(":", "_");

        File file = new File(path, fileName); // e_ for encode
        if(file.exists()){
            DetailsActivity.getInstance().checkExist=true;
            new ToastMsg(mContext).toastIconError("File already exist.");
        }
        else {
            String dir = mContext.getExternalCacheDir().toString();
            String workId;
            workId = streamURL.replaceAll(" ", "_");
            workId = workId.replaceAll(":", "_");
            boolean isDuplicationFound = false;
            List<Work> workList = mDBHelper.getAllWork();
            for (Work w : workList) {
                if (w.getUrl().equals(streamURL)) {
                    isDuplicationFound = true;
                    new ToastMsg(mContext).toastIconError("File is added to download.");
                }
            }
            if (!isDuplicationFound) {
                Work work = new Work();
                work.setUrl(streamURL);
                work.setWorkId(workId);
                work.setFileName(fileName);
                work.setDir(dir);
                work.setDownloadStatus(mContext.getResources().getString(R.string.download_waiting));
                mDBHelper.insertWork(work);
                new ToastMsg(mContext).toastIconSuccess("added " + fileName + " to download");
                Log.d("TRUNG","insertWork download " + fileName + " id: " + work.getWorkId());
            }
            if(isDownloading() || mIsDownloading) return;
            mIsDownloading = true;
            Data data = new Data.Builder()
                    .putString("url", streamURL)
                    .putString("dir", dir)
                    .putString("fileName", fileName)
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadWorkManager.class)
                    .setInputData(data)
                    .build();

            //String workId = request.getId().toString();
           // Constants.workId = workId;
            WorkManager.getInstance(mContext).enqueue(request);
            new ToastMsg(mContext).toastIconSuccess("Download started");
            Log.d("TRUNG","Request download " + fileName );
        }
    }
    private boolean isExistFile(String title){
        String fileName = title;
        String path = Constants.getDownloadDir(mContext) + mContext.getResources().getString(R.string.app_name);

        String fileExt = ".mp4"; // output like .mkv
        fileName = fileName + fileExt;

        fileName = fileName.replaceAll(" ", "_");
        fileName = fileName.replaceAll(":", "_");

        File file = new File(path, fileName); // e_ for encode
        return file.exists();
    }

    private boolean isDownloading(){
        List<Work> works = mDBHelper.getAllWork();
        for(Work work : works){
            if(work.getDownloadId() == 0 && !work.getDownloadStatus().equals(mContext.getString(R.string.download_waiting))){
               return true;
            }
            if(PRDownloader.getStatus(work.getDownloadId()) == Status.RUNNING){
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
        @BindView(R.id.name) TextView name;
        @BindView(R.id.resolution_tv) TextView resolution;
        @BindView(R.id.size_tv) TextView size;
        @BindView(R.id.item_layout) LinearLayout itemLayout;
        @BindView(R.id.icon) ImageView icon;

        public OriginalViewHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);
        }
    }

}