package tvseries.koreandramaengsub.freemovieapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.models.Work;

public class FileDownloadAdapter extends RecyclerView.Adapter<FileDownloadAdapter.ViewHolder> {

    private final boolean isDark;
    private List<Work> mWorks;
    private Context mContext;
    private ArrayList<ViewHolder> mViewHolders = new ArrayList<>();

    private OnProgressUpdateListener mProgressUpdateListener;

    public FileDownloadAdapter(List<Work> mWorks, Context mContext, boolean isDark) {
        this.mWorks = mWorks;
        this.mContext = mContext;
        this.isDark = isDark;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_file_download_item, parent,
                false);
        return new ViewHolder(v);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Work work = mWorks.get(position);
        holder.id = work.getDownloadId();
        mViewHolders.add(holder);
        holder.fileNameTv.setText(work.getFileName());
        Log.d("TRUNGX", "" + work.getFileName());
        if (work.getDownloadSize() != null && work.getTotalSize() != null) {
            double downloadedByte = Double.parseDouble(work.getDownloadSize());
            double totalByte = Double.parseDouble(work.getTotalSize());
            double totalKb = totalByte / 1024;
            double downloadKb = downloadedByte / 1024;
            double totalMb = totalKb / 1024;
            double downloadMb = downloadKb / 1024;

            holder.progressBar.setMax((int) totalKb);

            if (work.getDownloadStatus().equals("Waiting")) {
                holder.progressBar.setProgress((int) 0);
            } else {
                holder.progressBar.setProgress((int) downloadKb);
            }
            holder.setProgress((int) totalKb, (int) downloadKb);
            holder.setDownloadAmount(downloadMb, totalMb);
        } else {
//            holder.setDownloadAmount(0, 0);
        }
        if (work.getDownloadStatus() != null) {
            holder.setDownloadStatus(work.getDownloadStatus());
        }
    }

    @Override
    public int getItemCount() {
        return mWorks.size();
    }

    public void setNotifyChanged(List<Work> works) {
        mWorks = works;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public int id;
        @BindView(R.id.file_name_tv)
        TextView fileNameTv;
        @BindView(R.id.play_pause_iv)
        ImageView startPauseIv;
        @BindView(R.id.close_iv)
        ImageView closeIV;
        @BindView(R.id.download_amount_tv)
        TextView downloadAmountTv;
        @BindView(R.id.progressBarOne)
        ProgressBar progressBar;
        @BindView(R.id.download_status_tv)
        TextView downloadStatusTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.play_pause_iv)
        void onPauseClick() {
            if (mProgressUpdateListener != null) {
                mProgressUpdateListener.onItemClick(getAdapterPosition(), mWorks.get(getAdapterPosition()),
                        this);
            }
        }

        @OnClick(R.id.close_iv)
        void onCloseClick() {
            if (mProgressUpdateListener != null) {
                mProgressUpdateListener.OnCancelClick(getAdapterPosition(), mWorks.get(getAdapterPosition()),
                        this);
            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void setDownloadStatus(String status) {
            if (status != downloadStatusTv.getText()) {
                downloadStatusTv.setText(status);
                Log.d("TRUNGX", "" + status);
                if (status.equals(mContext.getResources().getString(R.string.download_pause)) || status.equals(mContext.getResources().getString(R.string.download_waiting))) {
                    startPauseIv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_play_circle_tranparent));
                } else {
                    startPauseIv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_pause_circle_transparent));
                }
            }
        }

        public void setProgress(int totalKb, int downloadKb) {
            progressBar.setMax((int) totalKb);
            progressBar.setProgress((int) downloadKb);
        }

        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        public void setDownloadAmount(double downloadMb, double totalMb) {
//            downloadAmountTv.setText(Double.parseDouble(String.format("%.1f", downloadMb)) + " MB / "
 //                   + Double.parseDouble(String.format("%.1f", totalMb)) + " MB");
        }
    }

    public interface OnProgressUpdateListener {
        void onItemClick(int position, Work work, ViewHolder viewHolder);

        void OnCancelClick(int position, Work work, ViewHolder viewHolder);
    }


    public void setProgressUpdateListener(OnProgressUpdateListener progressUpdateListener) {
        this.mProgressUpdateListener = progressUpdateListener;
    }

    public ViewHolder getViewHolderFromId(int downloadId) {
        for (ViewHolder viewHolder : mViewHolders) {
            if (viewHolder.id == downloadId) return viewHolder;
        }
        return null;
    }
}
