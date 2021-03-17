package animes.englishsubtitle.freemovieseries.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Picasso;

import java.util.List;

import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.network.model.CommonModel;
import animes.englishsubtitle.freemovieseries.utils.ItemAnimation;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    List<CommonModel> commonModels;
    Context context;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public SearchAdapter(List<CommonModel> commonModels, Context context) {
        this.commonModels = commonModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.card_home_view, parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CommonModel commonModel = commonModels.get(position);

        if (commonModel != null) {

            holder.name.setText(commonModel.getTitle());
            holder.qualityTv.setText(commonModel.getVideoQuality());
            holder.releaseDateTv.setText(commonModel.getRelease());
            if(Integer.parseInt(commonModel.getCount_status_movie())>=0){
                if(commonModel.getStatus_movie().equals("On-going")){
                    holder.countEp.setVisibility(View.VISIBLE);
                    if(commonModel.getCount_status_movie()!=null){
                        holder.countEp.setText("EP"+String.valueOf(commonModel.getCount_status_movie()));
                    }else {
                        holder.countEp.setText("EP0");
                    }

                    holder.statusMovie.setText(String.valueOf(commonModel.getStatus_movie()));
                    holder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_on_going);

                }else if(commonModel.getStatus_movie().equals("Trailer")){
                    holder.statusMovie.setText(String.valueOf(commonModel.getStatus_movie()));
                    holder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_trailer);
                }
                else{
                    holder.statusMovie.setText(String.valueOf(commonModel.getStatus_movie()));
                }
            }else if(Integer.parseInt(commonModel.getCount_status_movie())==-1){
                if(commonModel.getStatus_movie().equals("New")){
                    holder.statusMovie.setText("New");
                }else if(commonModel.getStatus_movie().equals("Trailer")){
                    holder.statusMovie.setText(String.valueOf(commonModel.getStatus_movie()));
                    holder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_trailer);
                }
                else {
                    holder.statusMovie.setText(String.valueOf(commonModel.getStatus_movie()));
                    holder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_on_going);
                }
            }
            Picasso.get().load(commonModel.getThumbnailUrl()).into(holder.image);


        }

        setAnimation(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return commonModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {



        public ImageView image;
        public TextView name, qualityTv, releaseDateTv, statusMovie, countEp;
        public MaterialRippleLayout lyt_parent;


        public ViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            name = v.findViewById(R.id.name);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            qualityTv = v.findViewById(R.id.quality_tv);
            releaseDateTv = v.findViewById(R.id.release_date_tv);
            statusMovie=v.findViewById(R.id.status_movie);
            countEp=v.findViewById(R.id.count_ep);


            lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(commonModels.get(getAdapterPosition()));
                    }

                }
            });

        }
    }

    public interface OnItemClickListener {

        void onItemClick(CommonModel commonModel);

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }

        });



        super.onAttachedToRecyclerView(recyclerView);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, on_attach ? position : -1, animation_type);
            lastPosition = position;
        }
    }
}
