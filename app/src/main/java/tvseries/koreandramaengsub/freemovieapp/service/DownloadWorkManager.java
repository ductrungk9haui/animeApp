package tvseries.koreandramaengsub.freemovieapp.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
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

import java.util.List;

import tvseries.koreandramaengsub.freemovieapp.DetailsActivity;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.models.Work;
import tvseries.koreandramaengsub.freemovieapp.utils.Constants;
import tvseries.koreandramaengsub.freemovieapp.utils.ToastMsg;

import static android.app.Activity.RESULT_OK;

public class DownloadWorkManager extends Worker {
    private LiveDataHelper liveDataHelper;
    public static final String START_PAUSE_ACTION = "startPause";
    public static final String START_PAUSE_STATUS = "startPauseStatus";
    public static final String START_PAUSE_FEEDBACK_STATUS = "startPauseFeedbackStatus";
    public static final String PROGRESS_RECEIVER = "progress_receiver";

    String fileName;
    int downloadId;

    Context context;

    DatabaseHelper helper;
    public static boolean isDownloading;
    public static int isDownloadingID;
    private String workId;

    private long downloadByte, totalByte;

    public DownloadWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        helper = new DatabaseHelper(context);
        workId = Constants.workId;
       // liveDataHelper = LiveDataHelper.getInstance();

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
                String path = Constants.getDownloadDir(context) + context.getResources().getString(R.string.app_name);
                downloadId = PRDownloader.download(url, path, fileName)
                        .build()
                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                            @Override
                            public void onStartOrResume() {
                                Work work = helper.getWorkByDownloadId(downloadId);
                                if(isDownloading){
                                    PRDownloader.pause(downloadId);
                                    Log.d("TRUNG","waiting download " + fileName);
                                    return;
                                }
                                isDownloading = true;
                                isDownloadingID = downloadId;
                                Intent intent = new Intent(START_PAUSE_FEEDBACK_STATUS);
                                intent.putExtra("result", RESULT_OK);
                                intent.putExtra("downloadId", downloadId);
                                intent.putExtra("status", "start");
                                intent.putExtra("fileName", fileName);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                work.setDownloadStatus(context.getResources().getString(R.string.downloading));
                                // set app close status false

                                work.setAppCloseStatus("false");
                                // save the data to the database
                                helper.updateWork(work);
                                //new ToastMsg(context).toastIconSuccess("Download started.");
                                Log.d("TRUNG","start download " + fileName);

                            }
                        })
                        .setOnPauseListener(new OnPauseListener() {
                            @Override
                            public void onPause() {
                                Intent intent = new Intent(START_PAUSE_FEEDBACK_STATUS);
                                intent.putExtra("result", RESULT_OK);
                                intent.putExtra("downloadId", downloadId);
                                intent.putExtra("status", "pause");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                Work work = helper.getWorkByDownloadId(downloadId);

                                work.setDownloadSize(downloadByte + "");
                                work.setTotalSize(totalByte + "");
                                if(work.getDownloadStatus().equals(context.getResources().getString(R.string.downloading))){
                                    work.setDownloadStatus("Paused");
                                }else{
                                    work.setDownloadStatus("Waiting");
                                }

                                work.setAppCloseStatus("false");
                                work.setFileName(fileName);
                                // save the data to the database
                                helper.updateWork(work);

                                Log.d("TRUNG","onPause download " + fileName);

                            }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel() {
                                isDownloading = isDownloading();
                                //helper.deleteAllDownloadData();
                                helper.deleteByDownloadId(downloadId);
                                new ToastMsg(context).toastIconSuccess(fileName+" canceled");
                                Log.d("TRUNG","onCancel download " + fileName);
                            }
                        })
                        .setOnProgressListener(new OnProgressListener() {
                            @Override
                            public void onProgress(Progress progress) {
                                Intent intent = new Intent(PROGRESS_RECEIVER);
                                intent.putExtra("result", RESULT_OK);
                                intent.putExtra("downloadId", downloadId);
                                intent.putExtra("currentByte", progress.currentBytes);
                                intent.putExtra("workId", workId);
                                intent.putExtra("totalByte", progress.totalBytes);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                downloadByte = progress.currentBytes;
                                totalByte = progress.totalBytes;
                                //final int currentProgress = (int) ((progress.currentBytes / 1024) / 1024); //mb
                               // liveDataHelper.updatePercentage(currentProgress); //mb);
                            }
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                isDownloading = false;
                                helper.deleteByDownloadId(downloadId);
                                new ToastMsg(context).toastIconSuccess(fileName+" Completed");
                                Log.d("TRUNG","Completed download " + fileName);
                                onCompletedDownload();
                               // liveDataHelper.completeStatus(true);
                            }

                            @Override
                            public void onError(Error error) {
                                //error.getConnectionException().printStackTrace();
                                //new ToastMsg(context).toastIconError("something went wrong");
                                DetailsActivity.getInstance().checkFailLink=true;
                                //helper.deleteAllDownloadData();
                                helper.deleteByDownloadId(downloadId);

                                Log.d("TRUNG","onError download " + fileName);
                            }
                        });

                boolean isDuplicationFound = false;
                List<Work> workList = helper.getAllWork();
                for (Work w : workList) {
                    if (w.getDownloadId() == downloadId) {
                        isDuplicationFound = true;
                    }
                }

                if (!isDuplicationFound) {
                    Work work = new Work();
                    work.setWorkId(Constants.workId);
                    work.setDownloadId(downloadId);
                    work.setFileName(fileName);
                    work.setUrl(url);
                    work.setAppCloseStatus("false");
                    long v = helper.insertWork(work);
                    Log.d("TRUNG","insertWork download " + fileName + " id: " + work.getWorkId());
                }
            }
        }).start();

        return Result.success();
    }

    private boolean isDownloading(){
        List<Work> works = helper.getAllWork();
        for(Work work : works){
            if(PRDownloader.getStatus(work.getDownloadId()) == Status.RUNNING){
                return true;
            }
        }
        return false;
    }

    private void onCompletedDownload(){
        List<Work> works = helper.getAllWork();
        if(works.size() > 0){
            Work nextDownloadWork = works.get(0);
            PRDownloader.resume(nextDownloadWork.getDownloadId());
            Log.d("TRUNG","next download " + nextDownloadWork.getFileName());
            return;
        }
        Log.d("TRUNG","finished download");
    }
    @Override
    public void onStopped() {
        super.onStopped();
    }
}
