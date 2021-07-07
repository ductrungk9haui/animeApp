package animes.englishsubtitle.freemovieseries.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.models.SubtitleModel;
import animes.englishsubtitle.freemovieseries.models.Work;
import animes.englishsubtitle.freemovieseries.utils.Constants;
import animes.englishsubtitle.freemovieseries.utils.ToastMsg;
import io.reactivex.disposables.CompositeDisposable;

public class DownloadWorkManager extends Worker {
    int mDownloadId;
    Context mContext;
    DatabaseHelper mDBHelper;
    public static int isDownloadingID;
    private String mWorkId;
    private String mCurrentBytes;
    private String mTotalBytes;
    private AppCompatActivity activity;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final CompositeDisposable compositeDisposable1 = new CompositeDisposable();
//    public final DownloadProgressCallback callback = new DownloadProgressCallback() {
//        @Override
//        public void onProgressUpdate(float progress, long etaInSeconds) {
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                public void run() {
//                    Work work = new Work();
//                    work.setDownloadId(mDownloadId);
//                    work.setCurrentBytes((long) progress);
//                    mCurrentBytes = String.valueOf(progress) + "";
//                    work.setTotalBytes(100);
//                    mTotalBytes = String.valueOf(100)+ "";
//                    EventBus.getDefault().post(new onProgress(work));
//                    Log.d("Hoan-progress", String.valueOf(progress));
//                }
//            });
//        }
//    };
//    public final DownloadProgressCallback callback2 = new DownloadProgressCallback() {
//        @Override
//        public void onProgressUpdate(float progress, long etaInSeconds) {
//            runOnUiThread(() -> {
//            // tvCommandStatus.setText(String.valueOf(progress) + "% (ETA " + String.valueOf(etaInSeconds) + " seconds)");
//
//                    }
//            );
//        }
//    };


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
        final String fileNameSub = data.getString("fileName").substring(0,data.getString("fileName").lastIndexOf("."));
        Log.d("Hoan-fileNameSub",fileNameSub);
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
                                Log.d("Hoan-type",type);
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
                                else if(type.equals(".m3u8")){
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
                                }else if(type.equals(".m3u8")){
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
                                    // new ToastMsg(mContext).toastIconError(fileName + " Error");
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
                        if(work.getUrl().equals(url) && work.getFileName().equals(fileName)){
                            //work.addDownloadSubId(mDownloadId);
                            work.setDownloadId(mDownloadId);
                            work.setDownloadStatus(mContext.getResources().getString(R.string.download_start));
                            work.setAppCloseStatus("false");
                            mDBHelper.updateWork(work);
                            Log.d("Hoan-download-mana", String.valueOf(mDownloadId));



                            File downldir=new File(mContext.getCacheDir().toString());
//                            String nameDownl=fileName.replace(".m3u8",".mp4")
//                                    .replace(work.getMovieName()+"_","");
                            String cmd = String.format("-i %s -acodec %s -bsf:a aac_adtstoasc -vcodec %s %s", work.getUrl().toString(), "copy", "copy", downldir.getAbsoluteFile() + "/"+fileName.replace(".m3u8",".mp4"));
                            Log.d("Hoan-cmd-file",cmd);
                            String[] commands = cmd.split(" ");

                            FFmpegSession session = FFmpegKit.execute(commands);
                            if (ReturnCode.isSuccess(session.getReturnCode())) {
                                Work work1 = mDBHelper.getWorkByDownloadId(mDownloadId);
                                work1.setDownloadStatus(mContext.getResources().getString(R.string.download_completed));
                                Log.d("Hoan-mfile",work.getMovieName());
                                final File mfile=new File(Constants.getDownloadDir(mContext) + mContext.getResources().getString(R.string.app_name),work.getMovieName());
                                if (!mfile.exists()) {
                                    mfile.mkdirs();
                                }
                                mDBHelper.deleteByDownloadId(mDownloadId);
                                nextDownload();
                                EventBus.getDefault().post(new onDownloadCompleted(work));
                                Log.d("Hoan-sucess","1");


//                                String nameDownl=fileName.replace(".m3u8",".mp4")
//                                    .replace(movieName+"_","");
                                try {
                                    // Log.d("Hoan-mfile",mfile.getAbsolutePath() + "/"+nameDownl);
                                    Files.move(Paths.get(downldir.getAbsoluteFile() + "/"+fileName.replace(".m3u8",".mp4")), Paths.get(mfile.getAbsolutePath() + "/"+fileName.replace(".m3u8",".mp4")));
                                    Log.d("Hoan-movie-file","1");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                            } else if (ReturnCode.isCancel(session.getReturnCode())) {

                                // CANCEL
                                Log.d("Hoan-cancel","1");

                            } else {
                                Work work2 = mDBHelper.getWorkByDownloadId(mDownloadId);
                                for(int subId : work2.getDownloadSubIdList()){
                                    PRDownloader.cancel(subId);
                                }
                                mDBHelper.deleteByDownloadId(mDownloadId);
                                //new ToastMsg(mContext).toastIconError(fileName + " Error");
                                nextDownload();
                                EventBus.getDefault().post(new onError(mDownloadId));
                                //DownloadActivity.getInstance().updateFiles();
                                Log.d("Hoan-succesful","0");
                            }

                            FFmpegKitConfig.enableStatisticsCallback(new StatisticsCallback() {

                                @Override
                                public void apply(final Statistics newStatistics) {
                                    // newStatistics.getSize();
                                    //    Log.d("Hoan",String.valueOf(newStatistics.getSize())+String.valueOf(newStatistics.getSpeed()));
                                }
                            });
                        }
                    }

                }
                Log.d("TRUNG","start download " + fileName + " id: " + mDownloadId);
            }
        }).start();
        return Result.success();
    }

    private void  downloadSubtitle(Work obj,String fileName){

        for(SubtitleModel sublist : obj.getListSubs()){
            Log.d("Hoan-listsub",String.valueOf(obj.getListSubs()));
            String subtitleName = fileName +"_" + sublist.getLanguage() + ".vtt";
            String path = Constants.getDownloadDir(mContext).toString()+"/";
            File file = new File(path, subtitleName.replace(" ","_")); // e_ for encode
            Log.d("Hoan-downsub-path", subtitleName);
            if(file.exists()){
                Log.d("TRUNG","sub already exist.");
                //new ToastMsg(mContext).toastIconError("sub already exist.");
                continue;
            }
            Data data = new Data.Builder()
                    .putString("url", sublist.getUrl())
                    .putString("type", ".vtt")
                    .putInt("downloadID", mDownloadId)
                    .putString("fileName",obj.getMovieName()+"/"+subtitleName)
                    .build();
            Log.d("Hoan-sublist",obj.getMovieName()+"/"+subtitleName);
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
                String checkUrl=nextDownloadWork.getUrl().substring(nextDownloadWork.getUrl().lastIndexOf('.'));
                // Log.d("Hoan-nextdownload",String.valueOf(nextDownloadWork.getUrl()));
                Data data;

                data = new Data.Builder()
                        .putString("url", nextDownloadWork.getUrl())
                        .putString("type", ".m3u8")
                        .putString("fileName", nextDownloadWork.getFileName())
                        .build();
                Log.d("Hoan-check-next",nextDownloadWork.getFileName());


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