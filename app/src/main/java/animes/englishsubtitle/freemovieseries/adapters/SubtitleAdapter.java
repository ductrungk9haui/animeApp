package animes.englishsubtitle.freemovieseries.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.models.SubtitleModel;

public class SubtitleAdapter extends RecyclerView.Adapter<SubtitleAdapter.OriginalViewHolder> {
    private List<SubtitleModel> items = new ArrayList<>();
    private List<String> subList = new ArrayList<>();
    private Context ctx;
    private Listener listener;

    public SubtitleAdapter(Context context, List<SubtitleModel> items) {
        this.items = items;
        ctx = context;
    }

    public SubtitleAdapter(List<String> list) {
        this.subList = list;
    }

    @Override
    public SubtitleAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SubtitleAdapter.OriginalViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_subtitle, parent, false);
        vh = new SubtitleAdapter.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(SubtitleAdapter.OriginalViewHolder holder, final int position) {
        String language = "Unknown";
        if(subList.size() > 0 ){
            if(!subList.get(position).equals("Off")){
                File file = new File(subList.get(position));
                String fileName = file.getName();
                language = fileName.substring(fileName.lastIndexOf("_") + 1,fileName.lastIndexOf("."));
            }else{
                language = "Off";
            }

        }
        if(items.size() > 0){
            SubtitleModel obj = items.get(position);
            language = obj.getLanguage();
        }
        holder.name.setText(language);

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickSubtitles(position);
            }
        });

    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return items.size()>0?items.size():subList.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        private View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }
    public interface Listener {
        void onClickSubtitles(int position);
    }
}
