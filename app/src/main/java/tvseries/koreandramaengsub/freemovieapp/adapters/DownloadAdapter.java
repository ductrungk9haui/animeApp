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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.ads.reward.RewardedVideoAd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tvseries.koreandramaengsub.freemovieapp.DetailsActivity;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.models.CommonModels;
import tvseries.koreandramaengsub.freemovieapp.service.DownloadWorkManager;
import tvseries.koreandramaengsub.freemovieapp.utils.Constants;
import tvseries.koreandramaengsub.freemovieapp.utils.ToastMsg;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.OriginalViewHolder>{

    private List<CommonModels> items = new ArrayList<>();
    private Context ctx;
    private boolean isDialog;
    private View v = null;

    private ServerAdapter.OnItemClickListener mOnItemClickListener;

    private DownloadAdapter.OriginalViewHolder viewHolder;

    private static DownloadAdapter instance;
    private boolean check=false;

    public RewardedVideoAd mrewardedAd;



    public DownloadAdapter(Context ctx, List<CommonModels> items,  boolean isDialog) {
        this.ctx = ctx;
        this.items = items;
        this.isDialog = isDialog;

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

        final CommonModels obj = items.get(position);
        holder.name.setText(obj.getTitle());
        holder.resolution.setText(obj.getResulation());
        holder.size.setText(obj.getFileSize());
        //DetailsActivity.getInstance().loadAdReward();

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (obj.isInAppDownload()) {
                    //in app download enabled
                    //PopUpAds.ShowAdmobInterstitialAds(ctx);
                   // if(check==true){
                        new ToastMsg(ctx).toastIconSuccess("Download will started after 3s...");
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DetailsActivity.getInstance().showRewardedVideo();
                                downloadFileInsideApp(obj.getTitle(), obj.getStremURL());
                            }
                        }, 5000);
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
                    ctx.startActivity(i);
                }
            }
        });
    }

    public static DownloadAdapter getInstance() {
        return instance;
    }
    public void downloadFileInsideApp(String title, String streamURL) {
        String fileName = title.toString();
        int notificationId = new Random().nextInt(100 - 1) - 1;
        if (streamURL == null || streamURL.isEmpty()) {
            return;
        }
        String path = Constants.getDownloadDir(ctx) + ctx.getResources().getString(R.string.app_name);

        String fileExt = ".mp4"; // output like .mkv
        fileName = fileName + fileExt;

        fileName = fileName.replaceAll(" ", "_");
        fileName = fileName.replaceAll(":", "_");

        File file = new File(path, fileName); // e_ for encode
        if(file.exists()) {
            //new ToastMsg(ctx).toastIconError("File already exist.");
            DetailsActivity.getInstance().checkExist=true;
            //DetailsActivity.getInstance().checkClose=false;
            return;
        }
        else {
            String dir = ctx.getExternalCacheDir().toString();
            Data data = new Data.Builder()
                    .putString("url", streamURL)
                    .putString("dir", dir)
                    .putString("fileName", fileName)
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadWorkManager.class)
                    .setInputData(data)
                    .build();

            String workId = request.getId().toString();
            Constants.workId = workId;
            WorkManager.getInstance().enqueue(request);
            Log.d("TRUNG","Request download " + fileName + " id: " + workId);
        }

    }



    @Override
    public int getItemCount() {
        return items.size();
    }


    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView name, resolution, size;
        public LinearLayout itemLayout;

        public OriginalViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            resolution = v.findViewById(R.id.resolution_tv);
            size = v.findViewById(R.id.size_tv);
            itemLayout=v.findViewById(R.id.item_layout);
        }
    }

}