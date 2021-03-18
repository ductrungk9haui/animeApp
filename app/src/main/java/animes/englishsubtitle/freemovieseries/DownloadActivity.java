package animes.englishsubtitle.freemovieseries;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.downloader.PRDownloader;
import com.downloader.Status;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import animes.englishsubtitle.freemovieseries.adapters.DownloadHistoryAdapter;
import animes.englishsubtitle.freemovieseries.adapters.FileDownloadAdapter;
import animes.englishsubtitle.freemovieseries.adapters.SubtitleAdapter;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.models.SubtitleModel;
import animes.englishsubtitle.freemovieseries.models.VideoFile;
import animes.englishsubtitle.freemovieseries.models.Work;
import animes.englishsubtitle.freemovieseries.service.DownloadWorkManager;
import animes.englishsubtitle.freemovieseries.utils.Constants;
import animes.englishsubtitle.freemovieseries.utils.RtlUtils;
import animes.englishsubtitle.freemovieseries.utils.ToastMsg;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DownloadActivity extends AppCompatActivity implements FileDownloadAdapter.OnProgressUpdateListener, DownloadHistoryAdapter.HistoryDownloadedListener, SubtitleAdapter.Listener {
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
    @BindView(R.id.video_view)
    PlayerView mSimpleExoPlayerView;
    @BindView(R.id.player_layout)
    View mPlayerLayout;
    @BindView(R.id.volumn_seekbar)
    SeekBar mVolumnSeekbar;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.play)
    RelativeLayout mLPlay;
    @BindView(R.id.cast_control_view)
    PlayerControlView mCastControlView;
    @BindView(R.id.main)
    View mMainLayout;
    @BindView(R.id.img_full_scr)
    ImageView mImgFull;
    @BindView(R.id.img_server)
    ImageView mServerIv;
    @BindView(R.id.img_subtitle)
    ImageView mImgSubtitle;
    @BindView(R.id.volumn_layout)
    LinearLayout mVolumnControlLayout;
    @BindView(R.id.img_back)
    ImageView mImgBack;
    private boolean mActiveMovie;
    private List<String> mListSub = new ArrayList<>();
    private Unbinder mUnbinder;
    private List<Work> mWorks = new ArrayList<>();
    private FileDownloadAdapter mFileDownloadAdapter;
    private DownloadHistoryAdapter mDownloadHistoryAdapter;
    private List<VideoFile> mVideoFiles = new ArrayList<>();
    private boolean mIsDark;
    DatabaseHelper mDBHelper;
    public static SimpleExoPlayer mDownloadPlayer;
    private int mAspectClickCount = 1;
    private boolean mIsFullScr;
    private int mPlayerHeight;
    private AudioManager mAudioManager;
    private MediaSource mMediaSource;
    private android.app.AlertDialog mAlertDialog;
    VideoFile mPlayingVideoFile;

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


        mDBHelper = new DatabaseHelper(this);
        mFileDownloadAdapter = new FileDownloadAdapter(mWorks, this, mIsDark);
        mFileDownloadAdapter.setProgressUpdateListener(this);
        mDownloadRv.setLayoutManager(new LinearLayoutManager(this));
        mDownloadRv.setAdapter(mFileDownloadAdapter);
        mDownloadRv.setHasFixedSize(true);

        mProgressBar.setMax(100); // 100 maximum value for the progress value
        mProgressBar.setProgress(50);
        mPlayerHeight = mLPlay.getLayoutParams().height;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        // mDownloadRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        mDownloadedFileRv.setLayoutManager(layoutManager);
        mDownloadHistoryAdapter = new DownloadHistoryAdapter(this, mVideoFiles);
        mDownloadHistoryAdapter.setListener(this);
        mDownloadedFileRv.setAdapter(mDownloadHistoryAdapter);
        mDownloadedFileRv.setHasFixedSize(true);

        updateFiles();

        registerReceiver(playVideoBroadcast, new IntentFilter(ACTION_PLAY_VIDEO));

        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        if (mAudioManager != null) {
            mVolumnSeekbar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            int currentVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mVolumnSeekbar.setProgress(currentVolumn);
        }
        mVolumnSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    //volumnTv.setText(i+"");
                    mAudioManager.setStreamVolume(mDownloadPlayer.getAudioStreamType(), i, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSimpleExoPlayerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (visibility == 0) {
                    mImgBack.setVisibility(VISIBLE);
                    mImgFull.setVisibility(VISIBLE);
                    if (mListSub.size() != 0) {
                        mImgSubtitle.setVisibility(VISIBLE);
                    }
                    mServerIv.setVisibility(GONE);
                } else {
                    mServerIv.setVisibility(GONE);
                    mImgFull.setVisibility(GONE);
                    mImgSubtitle.setVisibility(GONE);
                    mVolumnControlLayout.setVisibility(GONE);
                }
            }
        });
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
    public void onBackPressed() {
        if (mLPlay.getVisibility() == View.VISIBLE) {
            mLPlay.setVisibility(View.GONE);
            mMainLayout.setVisibility(View.VISIBLE);
        } else {
            releasePlayer();
            super.onBackPressed();
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

    @Override
    protected void onPause() {
        super.onPause();
        if (mActiveMovie && mDownloadPlayer != null) {
            //Log.e("PLAY:::","PAUSE");
            mDownloadPlayer.setPlayWhenReady(false);
        }
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
        Work work = object.getWork();
        for (SubtitleModel sublist : work.getListSubs()) {
            String fileName = work.getFileName().substring(0, work.getFileName().lastIndexOf("."));
            String subtitleName = fileName + "_" + sublist.getLanguage() + ".vtt";
            String path = Constants.getDownloadDir(getApplicationContext()) + getApplicationContext().getResources().getString(R.string.app_name);
            File file = new File(path, subtitleName); // e_ for encode
            if (file.exists()) {
                deleteFile(file, true);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadError(DownloadWorkManager.onError object) {
        updateFiles();
        Log.d("TRUNG", "error " + " id: " + object.getDownloadID());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            releasePlayer();
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

        workId = workId.replaceAll("'", "_");
        work.setWorkId(workId);
        mDBHelper.updateWork(work);
        Data data = new Data.Builder()
                .putString("url", work.getUrl())
                .putString("type", ".mp4")
                .putString("fileName", work.getFileName())
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadWorkManager.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(request);
        Log.d("TRUNG", "next download " + work.getFileName());
    }

    public void updateFiles() {
        mVideoFiles.clear();
        mWorks.clear();
        mWorks = mDBHelper.getAllWork();
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        String df_language = sharedPreferences.getString("df_subtitle",Constants.DEFAULT_LANGUAGE);

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
                if (extension.equals(".mp4")) {
                    VideoFile vf = new VideoFile();
                    List<String> subPath = new ArrayList<>();
                    vf.setFileName(fileName);
                    vf.setLastModified(file.lastModified());
                    vf.setTotalSpace(file.length());
                    vf.setPath(filePath);
                    vf.setFileExtension(extension);
                    for (File fileSub : files) {
                        String fileSubName = fileSub.getName();
                        String fileSubPath = fileSub.getPath();
                        String SubExtension = fileSubName.substring(fileSubName.lastIndexOf("."));
                        if (SubExtension.equals(".vtt") && fileSubName.contains(fileName.substring(0, fileName.lastIndexOf(".")))) {
                            subPath.add(fileSubPath);
                            if ( fileSubName.contains(df_language)) {
                                vf.setDefaultSubPath(fileSubPath);
                            }
                        }
                    }
                    vf.setSubList(subPath);
                    mVideoFiles.add(vf);
                }

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
        unregisterReceiver(playVideoBroadcast);
        if (!mActiveMovie) {
            releasePlayer();
        }
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
                deleteFile(new File(videoFile.getPath()), false);
                for (String subLink : videoFile.getSubList()) {
                    deleteFile(new File(subLink), true);
                }
                if (mVideoFiles.size() == 0 || videoFile.getPath().equals(mPlayingVideoFile.getPath())) {
                    releasePlayer();
                    mMainLayout.setVisibility(VISIBLE);
                    mLPlay.setVisibility(GONE);
                }

            }
        });
        dialog.setNegativeButton("No", null);
        dialog.show();
    }

    @Override
    public void onPlayVideo(VideoFile videoFile) {
        mPlayingVideoFile = videoFile;
        mLPlay.setVisibility(View.VISIBLE);
        mPlayerLayout.setVisibility(VISIBLE);
        releasePlayer();
        mListSub.addAll(videoFile.getSubList());

        String filePath = videoFile.getPath();
        Uri videoUrl = Uri.fromFile(new File(filePath));
        Log.d("filePathLocation", filePath);
        //simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new
                AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new
                DefaultTrackSelector(videoTrackSelectionFactory);
        mDownloadPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);

        DataSpec dataSpec = new DataSpec(videoUrl);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }
        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        mMediaSource = buildMediaSourceNew(videoUrl);
        mDownloadPlayer.prepare(mMediaSource);
        mSimpleExoPlayerView.setPlayer(mDownloadPlayer);

       /* SubtitleView view = mSimpleExoPlayerView.getSubtitleView();
        int defaultSubtitleColor = Color.argb(255, 218, 218, 218);
        int outlineColor = Color.argb(255, 43, 43, 43);
        Typeface subtitleTypeface = ResourcesCompat.getFont(this, R.font.amazon);
        CaptionStyleCompat style = new CaptionStyleCompat(defaultSubtitleColor,
                Color.TRANSPARENT, Color.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                outlineColor, subtitleTypeface);
        view.setApplyEmbeddedStyles(false);
        view.setStyle(style);*/

        mDownloadPlayer.setPlayWhenReady(true);
        mActiveMovie = true;
        if (mListSub.size() > 0) {
            setSelectedSubtitle(mMediaSource, videoFile.getDefaultSubPath(), DownloadActivity.this);
        }

    }

    private MediaSource buildMediaSourceNew(Uri uri) {
        DataSource.Factory datasourceFactroy = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getResources().getString(R.string.app_name)));
        return new ExtractorMediaSource.Factory(datasourceFactroy).createMediaSource(uri);
    }

    private void setSelectedSubtitle(MediaSource mediaSource, String subtitle, Context context) {
        MergingMediaSource mergedSource;
        if (subtitle != null) {
            //Uri subtitleUri = Uri.parse(subtitle);
            Uri subtitleUri = Uri.fromFile(new File(subtitle));
            Format subtitleFormat = Format.createTextSampleFormat(
                    null, // An identifier for the track. May be null.
                    MimeTypes.TEXT_VTT, // The mime type. Must be set correctly.
                    Format.NO_VALUE, // Selection flags for the track.
                    "en"); // The subtitle language. May be null.

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, getResources().getString(R.string.app_name)), new DefaultBandwidthMeter());

            MediaSource subtitleSource = new SingleSampleMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(subtitleUri, subtitleFormat, C.TIME_UNSET);

            // MediaSource subtitleSource = buildMediaSourceNew(subtitleUri);
            mergedSource = new MergingMediaSource(mediaSource, subtitleSource);
            mDownloadPlayer.prepare(mergedSource, false, false);
            mDownloadPlayer.setPlayWhenReady(true);
            //resumePlayer();

        } else {
            Toast.makeText(context, "there is no subtitle", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFile(File file, boolean isSub) {
        if (file.exists()) {
            try {
                boolean isDeleted = file.getCanonicalFile().delete();
                if (isDeleted) {
                    Log.d("TRUNG", file.getName() + "was deleted");
                    if (!isSub) {
                        Toast.makeText(this, "File deleted successfully.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.something_went_text), Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (file.exists()) {
                boolean isDeleted = getApplicationContext().deleteFile(file.getName());
                if (isDeleted) {
                    Log.d("TRUNG", file.getName() + "was deleted");
                    if (!isSub) {
                        Toast.makeText(this, "File deleted successfully.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.something_went_text), Toast.LENGTH_SHORT).show();
                }
            }
        }

        updateFiles();
    }

    @OnClick(R.id.img_full_scr)
    void onFullScrClick() {
        controlFullScreenPlayer();
    }

    @OnClick(R.id.volumn_control_iv)
    void onVolumnControlClick() {
        mVolumnControlLayout.setVisibility(VISIBLE);
    }

    @OnClick(R.id.img_subtitle)
    void onSubtitleClick() {
        showSubtitleDialog(DownloadActivity.this, mListSub);
    }

    public void releasePlayer() {
        if (mDownloadPlayer != null) {
            mDownloadPlayer.setPlayWhenReady(false);
            mDownloadPlayer.stop();
            mDownloadPlayer.release();
            mDownloadPlayer = null;
            mSimpleExoPlayerView.setPlayer(null);
            mActiveMovie = false;
            mListSub.clear();
            //simpleExoPlayerView = null;
        }
    }

    public void showSubtitleDialog(Context context, List<String> list) {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_subtitle, viewGroup, false);
        ImageView cancel = dialogView.findViewById(R.id.cancel);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        SubtitleAdapter adapter = new SubtitleAdapter(list);
        adapter.setListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        mAlertDialog = builder.create();
        mAlertDialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.cancel();
            }
        });

    }

    @OnClick(R.id.aspect_ratio_iv)
    void onAspectRatioClick() {
        if (mAspectClickCount == 1) {
            //Toast.makeText(DetailsActivity.this, "Fill", Toast.LENGTH_SHORT).show();
            mSimpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            mDownloadPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            mAspectClickCount = 2;
        } else if (mAspectClickCount == 2) {
            //Toast.makeText(DetailsActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            mSimpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            mDownloadPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            mAspectClickCount = 3;
        } else if (mAspectClickCount == 3) {
            //Toast.makeText(DetailsActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            mSimpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            mDownloadPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            mAspectClickCount = 1;
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void controlFullScreenPlayer() {
        if (mIsFullScr) {
            mIsFullScr = false;
            mMainLayout.setVisibility(VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mLPlay.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, mPlayerHeight));
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            mIsFullScr = true;
            mMainLayout.setVisibility(GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mLPlay.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @OnClick(R.id.img_back)
    void onBackClick() {
        if (mActiveMovie) {
            if (mIsFullScr) {
                controlFullScreenPlayer();
            } else {
                releasePlayer();
                mLPlay.setVisibility(GONE);
                mMainLayout.setVisibility(VISIBLE);
            }
        }

    }

    @Override
    public void onClickSubtitles(int position) {
        setSelectedSubtitle(mMediaSource, mListSub.get(position), getApplicationContext());
        mAlertDialog.cancel();
    }
}
