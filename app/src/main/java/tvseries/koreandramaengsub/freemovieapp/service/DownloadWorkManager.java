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
import com.downloader.Status;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import tvseries.koreandramaengsub.freemovieapp.DetailsActivity;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
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
        final String dir = data.getString("dir");
        final String fileName = data.getString("fileName").substring(0,data.getString("fileName").indexOf("."))+".mp4";

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
                                Work work = mDBHelper.getWorkByDownloadId(mDownloadId);
                                isDownloadingID = mDownloadId;
                                work.setDownloadStatus(mContext.getResources().getString(R.string.downloading));
                                // set app close status false
                                work.setAppCloseStatus("false");
                                // save the data to the database
                                mDBHelper.updateWork(work);
                                //new ToastMsg(context).toastIconSuccess("Download started.");
                                Log.d("TRUNG","start download " + fileName);
                                EventBus.getDefault().post(new onStartOrResume(work));
                            }
                        })
                        .setOnPauseListener(new OnPauseListener() {
                            @Override
                            public void onPause() {
                                Log.d("TRUNG","onPause download " + fileName);
                                Work work = mDBHelper.getWorkByDownloadId(mDownloadId);
                                work.setDownloadStatus(mContext.getResources().getString(R.string.download_pause));
                                work.setDownloadSize(mCurrentBytes);
                                work.setTotalSize(mTotalBytes);
                                work.setAppCloseStatus("false");
                                // save the data to the database
                                mDBHelper.updateWork(work);
                                EventBus.getDefault().post(new onPause(work));
                            }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel() {
                                mDBHelper.deleteByDownloadId(mDownloadId);
                                new ToastMsg(mContext).toastIconSuccess(fileName+" canceled");
                                Log.d("TRUNG","onCancel download " + fileName);
                                EventBus.getDefault().post(new onCancel());
                            }
                        })
                        .setOnProgressListener(new OnProgressListener() {
                            @Override
                            public void onProgress(Progress progress) {
                                Work work = new Work();
                               /* Work work = mDBHelper.getWorkByDownloadId(mDownloadId);*/
                                work.setDownloadId(mDownloadId);
                                work.setCurrentBytes(progress.currentBytes);
                                work.setTotalBytes(progress.totalBytes);
                                mCurrentBytes = progress.currentBytes + "";
                                mTotalBytes = progress.totalBytes + "";
                                EventBus.getDefault().post(new onProgress(work));
                            }
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                Work work = mDBHelper.getWorkByDownloadId(mDownloadId);
                                work.setDownloadStatus(mContext.getResources().getString(R.string.download_completed));
                                mDBHelper.deleteByDownloadId(mDownloadId);
                                new ToastMsg(mContext).toastIconSuccess(fileName+" Completed");
                                EventBus.getDefault().post(new onDownloadCompleted(work));
                                nextDownload();
                            }

                            @Override
                            public void onError(Error error) {
                                DetailsActivity.getInstance().checkFailLink=true;
                                mDBHelper.deleteByDownloadId(mDownloadId);
                                new ToastMsg(mContext).toastIconError(fileName+" Error");
                                Log.d("TRUNG","onError download " + fileName);
                                nextDownload();
                                EventBus.getDefault().post(new onError(mDownloadId));
                            }
                        });

               List<Work> works = mDBHelper.getAllWork();
               for(Work work : works){
                   if(work.getUrl().equals(url) && work.getFileName().equals(fileName)){
                       work.setDownloadId(mDownloadId);
                       work.setDownloadStatus(mContext.getResources().getString(R.string.download_start));
                       work.setAppCloseStatus("false");
                       mDBHelper.updateWork(work);
                       return;
                   }
               }
               Log.d("TRUNG","start download " + fileName + " id: " + mDownloadId);
            }
        }).start();
        return Result.success();
    }

    private boolean isDownloading(){
        List<Work> works = mDBHelper.getAllWork();
        for(Work work : works){
            if(PRDownloader.getStatus(work.getDownloadId()) == Status.RUNNING){
                return true;
            }
        }
        return false;
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
                        .putString("dir", nextDownloadWork.getDir())
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
        public onCancel(){
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
