package tvseries.koreandramaengsub.freemovieapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.downloader.PRDownloader;
import com.downloader.Status;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tvseries.koreandramaengsub.freemovieapp.adapters.DownloadHistoryAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.FileDownloadAdapter;
import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.models.VideoFile;
import tvseries.koreandramaengsub.freemovieapp.models.Work;
import tvseries.koreandramaengsub.freemovieapp.service.DownloadWorkManager;
import tvseries.koreandramaengsub.freemovieapp.utils.Constants;
import tvseries.koreandramaengsub.freemovieapp.utils.RtlUtils;
import tvseries.koreandramaengsub.freemovieapp.utils.ToastMsg;

public class DownloadActivity extends AppCompatActivity implements FileDownloadAdapter.OnProgressUpdateListener, DownloadHistoryAdapter.OnDeleteDownloadFileListener {
    public static DownloadActivity instance;
    public static final String ACTION_PLAY_VIDEO = "play_video";
    @BindView(R.id.download_rv)
    RecyclerView mDownloadRv;
    @BindView(R.id.downloaded_file_tv)
    TextView mDownloadedFileTV;
    @BindView(R.id.downloaded_file_rv)
    RecyclerView mDownloadedFileRv;
    @BindView(R.id.appBar)
    Toolbar mToolbar;
    @BindView(R.id.coordinator_lyt)
    CoordinatorLayout mNoItemLayout;
    @BindView(R.id.progress_layout)
    LinearLayout mProgressLayout;
    private Unbinder mUnbinder;
    private List<Work> mWorks = new ArrayList<>();
    private FileDownloadAdapter mFileDownloadAdapter;
    private DownloadHistoryAdapter mDownloadHistoryAdapter;
    private List<VideoFile> mVideoFiles = new ArrayList<>();
    private boolean mIsDark;
    DatabaseHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        mIsDark = sharedPreferences.getBoolean("dark", false);
        if (mIsDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }
        setContentView(R.layout.activity_download);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Downloads");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (mIsDark) {
            mToolbar.setBackgroundColor(getResources().getColor(R.color.dark));
        } else {
            mToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        mDBHelper = new DatabaseHelper(this);
        mFileDownloadAdapter = new FileDownloadAdapter(mWorks, this, mIsDark);
        mFileDownloadAdapter.setProgressUpdateListener(this);
        mDownloadRv.setLayoutManager(new LinearLayoutManager(this));
        mDownloadRv.setAdapter(mFileDownloadAdapter);
        mDownloadRv.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        // mDownloadRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        mDownloadedFileRv.setLayoutManager(layoutManager);
        mDownloadHistoryAdapter = new DownloadHistoryAdapter(this, mVideoFiles);
        mDownloadHistoryAdapter.setListener(this);
        mDownloadedFileRv.setAdapter(mDownloadHistoryAdapter);
        mDownloadedFileRv.setHasFixedSize(true);

        updateFiles();

    }

    @Override
    public void onItemClick(int position, Work work, FileDownloadAdapter.ViewHolder viewHolder) {
        if (PRDownloader.getStatus(work.getDownloadId()) == Status.RUNNING) {
            PRDownloader.pause(work.getDownloadId());
        } else if (PRDownloader.getStatus(work.getDownloadId()) == Status.PAUSED) {
            if (PRDownloader.getStatus(DownloadWorkManager.isDownloadingID) == Status.RUNNING) {
                PRDownloader.pause(DownloadWorkManager.isDownloadingID);
            }
            PRDownloader.resume(work.getDownloadId());
        } else {
            if (PRDownloader.getStatus(DownloadWorkManager.isDownloadingID) == Status.RUNNING) {
                PRDownloader.pause(DownloadWorkManager.isDownloadingID);
            }
            startDownload(work);
        }

    }

    @Override
    public void OnCancelClick(int position, Work work, FileDownloadAdapter.ViewHolder viewHolder) {
        Log.d("TRUNGX", "cancel " + work.getFileName());
        if (PRDownloader.getStatus(work.getDownloadId()) == Status.RUNNING || PRDownloader.getStatus(work.getDownloadId()) == Status.PAUSED) {
            PRDownloader.cancel(work.getDownloadId());
        } else {
            mDBHelper.deleteByDownloadId(work.getDownloadId());
            updateFiles();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadStartOrResume(DownloadWorkManager.onStartOrResume object) {
        Work work = object.getWork();
        final int downloadId = work.getDownloadId();
        updateFiles();
        Log.d("TRUNG", "start download " + " id: " + downloadId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateDownloadProgress(DownloadWorkManager.onProgress object) {
        Work work = object.getWork();
        final int downloadId = work.getDownloadId();
        final long downloadedByte = work.currentBytes;
        final long totalByte = work.totalBytes;
        final FileDownloadAdapter.ViewHolder viewHolder = mFileDownloadAdapter.getViewHolderFromId(downloadId);
        if (viewHolder != null) {
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                public void run() {
                    double totalKb = totalByte / 1024f;
                    double downloadKb = downloadedByte / 1024f;
                    double totalMb = totalKb / 1024;
                    double downloadMb = downloadKb / 1024;
                    // set download status
                    viewHolder.setDownloadStatus(getResources().getString(R.string.downloading));
                    viewHolder.setProgress((int) totalKb, (int) downloadKb);
                    viewHolder.setDownloadAmount(downloadMb, totalMb);
                }
            };
            handler.post(runnable);
        }
        Log.d("TRUNG", "Update Byte: : " + downloadedByte + "/" + totalByte);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadCompleted(DownloadWorkManager.onDownloadCompleted object) {
        Log.d("TRUNG", "download completed");
        int downloadId = object.getWork().getDownloadId();
        final FileDownloadAdapter.ViewHolder viewHolder = mFileDownloadAdapter.getViewHolderFromId(downloadId);
        if (viewHolder != null) {
            viewHolder.setDownloadStatus(getResources().getString(R.string.download_completed));
        }
        updateFiles();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadPause(DownloadWorkManager.onPause object) {
        int downloadId = object.getWork().getDownloadId();
        Log.d("TRUNG", "pause " + downloadId);
        final FileDownloadAdapter.ViewHolder viewHolder = mFileDownloadAdapter.getViewHolderFromId(downloadId);
        if (viewHolder != null) {
            viewHolder.setDownloadStatus(getResources().getString(R.string.download_pause));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCancelDownload(DownloadWorkManager.onCancel object) {
        updateFiles();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadError(DownloadWorkManager.onError object) {
        updateFiles();
        Log.d("TRUNG", "error " + " id: " + object.getDownloadID());
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver playVideoBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String fileName = intent.getStringExtra("fileName");

            String url = Constants.getDownloadDir(DownloadActivity.this) + context.getResources().getString(R.string.app_name)
                    + File.separator + fileName;
            Log.e("donwloadDir", "url: " + url);

            File file = new File(url);

            if (!file.exists()) {
                new ToastMsg(DownloadActivity.this).toastIconError(getString(R.string.file_not_found));
                return;
            }

            Log.d("url:", url);

            // hide the progress layout
            SystemClock.sleep(3000);
            progressHideShowControl();


            Intent playIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setDataAndType(Uri.parse(url), "video/*");
            startActivity(playIntent);

        }
    };

    public void startDownload(Work work) {
        String dir = work.getDir();
        String url = work.getUrl();
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        File file = new File(dir + "/" + fileName);
        if (file.exists()) {
            Toast.makeText(this, getString(R.string.file_already_downloaded), Toast.LENGTH_SHORT).show();
            return;
        }
        String workId;
        workId = url.replaceAll(" ", "_");
        workId = workId.replaceAll(":", "_");
        work.setWorkId(workId);
        mDBHelper.updateWork(work);
        Data data = new Data.Builder()
                .putString("url", work.getUrl())
                .putString("dir", work.getDir())
                .putString("fileName", work.getFileName())
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadWorkManager.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(request);
        Log.d("TRUNG","next download " + work.getFileName());
    }

    public void updateFiles() {
        mVideoFiles.clear();
        mWorks.clear();
        mWorks = mDBHelper.getAllWork();

        String path = Constants.getDownloadDir(DownloadActivity.this) + getResources().getString(R.string.app_name);
        File directory = new File(path);
        File[] files = directory.listFiles();
        assert files != null;
        //get file from path
        for (File file : files) {
            String fileName = file.getName();
            String filePath = file.getPath();
            String extension = fileName.substring(fileName.lastIndexOf("."));

            if (!extension.equals(".temp")) {
                VideoFile vf = new VideoFile();
                vf.setFileName(fileName);
                vf.setLastModified(file.lastModified());
                vf.setTotalSpace(file.length());
                vf.setPath(filePath);
                vf.setFileExtension(extension);
                mVideoFiles.add(vf);
            }
        }
        if (mDownloadHistoryAdapter != null) {
            mDownloadHistoryAdapter.notifyDataSetChanged();
        }
        if (mVideoFiles.size() > 0) {
            mDownloadedFileTV.setVisibility(View.VISIBLE);
            mDownloadedFileRv.setVisibility(View.VISIBLE);
            mNoItemLayout.setVisibility(View.GONE);
        } else {
            mDownloadedFileTV.setVisibility(View.GONE);
            mDownloadedFileRv.setVisibility(View.GONE);
            if (mWorks.size() == 0) {
                mNoItemLayout.setVisibility(View.VISIBLE);
            } else {
                mNoItemLayout.setVisibility(View.GONE);
            }
        }
        if (mWorks.size() == 0) {
            mDownloadRv.setVisibility(View.GONE);
        } else {
            mDownloadRv.setVisibility(View.VISIBLE);
            mFileDownloadAdapter.setNotifyChanged(mWorks);
        }


    }

    public void progressHideShowControl() {
        if (mProgressLayout.getVisibility() == View.VISIBLE) {
            mProgressLayout.setVisibility(View.GONE);
        } else {
            mProgressLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregisterReceiver(playVideoBroadcast);
        mUnbinder.unbind();
    }


    @Override
    public void onDeleteDownloadFile(VideoFile videoFile) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Attention");
        dialog.setMessage("Do you want to delete this file?");
        dialog.setIcon(R.drawable.ic_warning);
        dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mVideoFiles.remove(videoFile);
                deleteFile(videoFile);
            }
        });
        dialog.setNegativeButton("No", null);
        dialog.show();
    }


    private void deleteFile(VideoFile videoFile) {
        File file = new File(videoFile.getPath());
        if (file.exists()) {
            try {
                boolean isDeleted = file.getCanonicalFile().delete();
                if (isDeleted) {
                    Toast.makeText(this, "File deleted successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.something_went_text), Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (file.exists()) {
                boolean isDeleted = getApplicationContext().deleteFile(file.getName());
                if (isDeleted) {
                    Toast.makeText(this, "File deleted successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.something_went_text), Toast.LENGTH_SHORT).show();
                }
            }
        }
        updateFiles();
    }
}
