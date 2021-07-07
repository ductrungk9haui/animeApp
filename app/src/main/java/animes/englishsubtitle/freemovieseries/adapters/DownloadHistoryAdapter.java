package animes.englishsubtitle.freemovieseries.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.models.VideoFile;
import animes.englishsubtitle.freemovieseries.utils.Tools;

public class DownloadHistoryAdapter extends RecyclerView.Adapter<DownloadHistoryAdapter.ViewHolder> {
    private HistoryDownloadedListener listener;
    private Context context;
    private List<VideoFile> videoFiles;

    public DownloadHistoryAdapter(Context context, List<VideoFile> videoFiles) {
        this.context = context;
        this.videoFiles = videoFiles;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_download_history, parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final VideoFile videoFile = videoFiles.get(position);
        Log.d("Hoan-movieName-history",videoFile.getMovieName());
        holder.fileNameTv.setText(videoFile.getFileName().replace(videoFile.getMovieName()+"_","").replace(".mp4",""));
        holder.fileSizeTv.setText("Size: " + Tools.byteToMb(videoFile.getTotalSpace()));
        holder.dateTv.setText(Tools.milliToDate(videoFile.getLastModified()));
        holder.item_holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null){
                    listener.onDeleteDownloadFile(videoFile);
                }
                return false;
            }
        });

        holder.item_holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          /*      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoFile.getPath()));
                intent.setDataAndType(Uri.parse(videoFile.getPath()), "video/*");
                context.startActivity(intent);*/
                listener.onPlayVideo(videoFile);
            }
        });


    }

    @Override
    public int getItemCount() {
        return videoFiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTv, fileSizeTv, dateTv;
        RelativeLayout item_holder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fileNameTv = itemView.findViewById(R.id.file_name_tv);
            fileSizeTv = itemView.findViewById(R.id.file_size_tv);
            dateTv = itemView.findViewById(R.id.date_tv);
            item_holder = itemView.findViewById(R.id.item_view);

        }

    }

    public interface HistoryDownloadedListener {
        void onDeleteDownloadFile(VideoFile videoFile);
        void onPlayVideo(VideoFile videoFile);
    }

    public void setListener(HistoryDownloadedListener listener) {
        this.listener = listener;
    }
}
