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

import java.io.File;
import java.util.ArrayList;

import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.utils.Tools;

public class DownloadFolderAdapter extends RecyclerView.Adapter<DownloadFolderAdapter.ViewHolder> {
    private HistoryFolerListener listener;
    private Context context;
    private ArrayList<File> videoFiles;

    public DownloadFolderAdapter(Context context, ArrayList<File> videoFiles) {
        this.context = context;
        this.videoFiles = videoFiles;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_download_foler_history, parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final File videoFile = videoFiles.get(position);
        holder.fileNameTv.setText(videoFile.getName());
        // holder.fileSizeTv.setText("Size: " + Tools.byteToMb(videoFile.getTotalSpace()));
        holder.fileSizeTv.setText(Tools.milliToDate(videoFile.getTotalSpace()));
        // Log.d("Hoan-fileNameTV",videoFile.getName());
        holder.item_holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null){
                    listener.onDeleteDownloadFoler(videoFile);
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
                listener.onClickFoler(videoFile);
                Log.d("Hoan",String.valueOf(videoFile.getName()));
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

            fileNameTv = itemView.findViewById(R.id.file_name_tv_f);
            fileSizeTv = itemView.findViewById(R.id.file_size_tv_f);
            dateTv = itemView.findViewById(R.id.date_tv_f);
            item_holder = itemView.findViewById(R.id.item_view_f);

        }

    }

    public interface HistoryFolerListener {
        void onDeleteDownloadFoler(File videoFile);
        void onClickFoler(File videoFile);
    }

    public void setListener(HistoryFolerListener listener) {
        this.listener = listener;
    }
}

