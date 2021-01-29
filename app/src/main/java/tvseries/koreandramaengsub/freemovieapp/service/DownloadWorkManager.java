package tvseries.koreandramaengsub.freemovieapp.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import tvseries.koreandramaengsub.freemovieapp.DetailsActivity;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.models.SubtitleModel;
import tvseries.koreandramaengsub.freemovieapp.models.Work;
import tvseries.koreandramaengsub.freemovieapp.utils.Constants;
import tvseries.koreandramaengsub.freemovieapp.utils.ToastMsg;

public class DownloadWorkManager extends Worker {
    int mDownloadId;
    Context mContext;
    DatabaseHelper mDBHelper;
    public static int isDownloadingID;
    private String mWorkId;
    private String mCurrentBytes;
    private String mTotalBytes;
    public DownloadWorkManager(@NonNull Context mContext, @NonNull WorkerParameters workerParams) {
        super(mContext, workerParams);
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public Result doWork() {
        mDBHelper = new DatabaseHelper(mContext);
        Data data = getInputData();
        final String url = data.getString("url");
        final String type = data.getString("type");
        final String fileNameSub = data.getString("fileName").substring(0,data.getString("fileName").indexOf("."));
        final String fileName = fileNameSub + type;
        final int downloadMainId = data.getInt("downloadID",0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Enabling database for resume support even after the application is killed:
                final PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                        .setDatabaseEnabled(true)
                        .setReadTimeout(30_000)
                        .setConnectTimeout(30_000)
                        .build();
                PRDownloader.initialize(getApplicationContext(), config);
                String path = Constants.getDownloadDir(mContext) + mContext.getResources().getString(R.string.app_name);
                mDownloadId = PRDownloader.download(url, path, fileName)
                        .build()
                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                            @Override
                            public void onStartOrResume() {
                                if(type.equals(".mp4")){
                                    Work work = mDBHelper.getWorkByDownloadId(mDownloadId);
                                    if(work.getDownloadStatus().equals(mContext.getResources().getString(R.string.download_start))){
                                        downloadSubtitle(work,fileNameSub);
                                    }else if(work.getDownloadStatus().equals(mContext.getResources().getString(R.string.download_pause))){
                                        for(int subId : work.getDownloadSubIdList()){
                                            PRDownloader.resume(subId);
                                        }
                                    }
                                    isDownloadingID = mDownloadId;
                                    work.setDownloadStatus(mContext.getResources().getString(R.string.downloading));
                                    // set app close status false
                                    work.setAppCloseStatus("false");
                                    // save the data to the database
                                    mDBHelper.updateWork(work);
                                    //new ToastMsg(context).toastIconSuccess("Download started.");
                                    EventBus.getDefault().post(new onStartOrResume(work));
                                }
                                Log.d("TRUNG","onStartOrResume " + fileName);
                            }
                        })
                        .setOnPauseListener(new OnPauseListener() {
                            @Override
                            public void onPause() {
                                Log.d("TRUNG","onPause download " + fileName);
                                if(type.equals(".mp4")) {
                                    Work work = mDBHelper.getWorkByDownloadId(mDownloadId);
                                    work.setDownloadStatus(mContext.getResources().getString(R.string.download_pause));
                                    work.setDownloadSize(mCurrentBytes);
                                    work.setTotalSize(mTotalBytes);
                                    work.setAppCloseStatus("false");
                                    // save the data to the database
                                    mDBHelper.updateWork(work);
                                    for(int subId : work.getDownloadSubIdList()){
                                        PRDownloader.pause(subId);
                                    }
                                    EventBus.getDefault().post(new onPause(work));
                                }
                            }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel() {
                                Log.d("TRUNG","onCancel download " + fileName);
                                if(type.equals(".mp4")) {
                                    Work work = mDBHelper.getWorkByDownloadId(mDownloadId);
                                    for(int subId : work.getDownloadSubIdList()){
                                        PRDownloader.cancel(subId);
                                    }
                                    mDBHelper.deleteByDownloadId(mDownloadId);
                                    new ToastMsg(mContext).toastIconSuccess(fileName + " canceled");
                                    nextDownload();
                                    EventBus.getDefault().post(new onCancel(work));
                                }
                            }
                        })
                        .setOnProgressListener(new OnProgressListener() {
                            @Override
                            public void onProgress(Progress progress) {
                                if(type.equals(".mp4")) {
                                    Work work = new Work();
                                    /* Work work = mDBHelper.getWorkByDownloadId(mDownloadId);*/
                                    work.setDownloadId(mDownloadId);
                                    work.setCurrentBytes(progress.currentBytes);
                                    work.setTotalBytes(progress.totalBytes);
                                    mCurrentBytes = progress.currentBytes + "";
                                    mTotalBytes = progress.totalBytes + "";
                                    EventBus.getDefault().post(new onProgress(work));
                                }
                            }
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                if(type.equals(".mp4")) {
                                    Work work = mDBHelper.getWorkByDownloadId(mDownloadId);
                                    work.setDownloadStatus(mContext.getResources().getString(R.string.download_completed));
                                    mDBHelper.deleteByDownloadId(mDownloadId);
                                    EventBus.getDefault().post(new onDownloadCompleted(work));
                                    nextDownload();
                                    new ToastMsg(mContext).toastIconSuccess(fileName+" Completed");
                                }
                                Log.d("TRUNG", "onDownloadComplete " + fileName);
                            }

                            @Override
                            public void onError(Error error) {
                                if(type.equals(".mp4")) {
                                    Work work = mDBHelper.getWorkByDownloadId(mDownloadId);
                                    for(int subId : work.getDownloadSubIdList()){
                                        PRDownloader.cancel(subId);
                                    }
                                    mDBHelper.deleteByDownloadId(mDownloadId);
                                    new ToastMsg(mContext).toastIconError(fileName + " Error");
                                    nextDownload();
                                    EventBus.getDefault().post(new onError(mDownloadId));
                                }
                                Log.d("TRUNG", "onError download " + fileName);
                            }
                        });

               List<Work> works = mDBHelper.getAllWork();
               for(Work work : works){
                   if(type.equals(".mp4")){
                       if(work.getUrl().equals(url) && work.getFileName().equals(fileName)){
                           work.setDownloadId(mDownloadId);
                           work.setDownloadStatus(mContext.getResources().getString(R.string.download_start));
                           work.setAppCloseStatus("false");
                           mDBHelper.updateWork(work);
                       }
                   }else{
                       if(work.getDownloadId() == downloadMainId){
                           work.addDownloadSubId(mDownloadId);
                           mDBHelper.updateWork(work);
                       }
                   }

               }
               Log.d("TRUNG","start download " + fileName + " id: " + mDownloadId);
            }
        }).start();
        return Result.success();
    }

    private void  downloadSubtitle(Work obj, String fileName){
        for(SubtitleModel sublist : obj.getListSubs()){
            String subtitleName = fileName +"_" + sublist.getLanguage() + ".vtt";
            String path = Constants.getDownloadDir(mContext) + mContext.getResources().getString(R.string.app_name);
            File file = new File(path, subtitleName); // e_ for encode
            if(file.exists()){
                Log.d("TRUNG","sub already exist.");
                //new ToastMsg(mContext).toastIconError("sub already exist.");
                continue;
            }
            Data data = new Data.Builder()
                    .putString("url", sublist.getUrl())
                    .putString("type", ".vtt")
                    .putInt("downloadID", mDownloadId)
                    .putString("fileName",subtitleName)
                    .build();
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadWorkManager.class)
                    .setInputData(data)
                    .build();
            WorkManager.getInstance(mContext).enqueue(request);
        }
    }

    private void nextDownload(){
        List<Work> works = mDBHelper.getAllWork();
        if(works.size() > 0){
            Work nextDownloadWork = works.get(0);
            if (nextDownloadWork.getDownloadStatus().equals(mContext.getString(R.string.download_pause))) {
                PRDownloader.resume(nextDownloadWork.getDownloadId());
            }else{
                Data data = new Data.Builder()
                        .putString("url", nextDownloadWork.getUrl())
                        .putString("type", ".mp4")
                        .putString("fileName", nextDownloadWork.getFileName())
                        .build();
                OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadWorkManager.class)
                        .setInputData(data)
                        .build();
                WorkManager.getInstance(mContext).enqueue(request);
                Log.d("TRUNG","next download " + nextDownloadWork.getFileName());
            }
            return;
        }
        Log.d("TRUNG","finished download");
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    public class onStartOrResume{
        Work work;
        public onStartOrResume( Work work){
            this.work = work;
        }
        public Work getWork() {
            return work;
        }

    }
    public class onPause{
        Work work;
        public onPause( Work work){
            this.work = work;
        }
        public Work getWork() {
            return work;
        }
    }
    public class onCancel{
        Work work;
        public onCancel( Work work){
            this.work = work;
        }
        public Work getWork() {
            return work;
        }
    }
    public class onProgress{
        Work work;
        public onProgress(Work work){
            this.work = work;
        }
        public Work getWork() {
            return work;
        }
    }

    public class onDownloadCompleted {
        Work work;
        public onDownloadCompleted(Work work) {
            this.work = work;
        }
        public Work getWork() {
            return work;
        }
    }
    public class onError{
        int downloadID;
        public onError(int id) {
            this.downloadID = id;
        }
        public int getDownloadID() {
            return downloadID;
        }
    }
}
