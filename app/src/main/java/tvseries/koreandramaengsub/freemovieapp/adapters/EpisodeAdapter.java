package tvseries.koreandramaengsub.freemovieapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tvseries.koreandramaengsub.freemovieapp.DetailsActivity;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.models.EpiModel;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.OriginalViewHolder> {

    private List<EpiModel> items = new ArrayList<>();
    private Context ctx;
    final EpisodeAdapter.OriginalViewHolder[] viewHolderArray = {null};
    private OnTVSeriesEpisodeItemClickListener mOnTVSeriesEpisodeItemClickListener;
    EpisodeAdapter.OriginalViewHolder viewHolder;
    int i=0;
    private int lastPosition = -1;
    private DetailsActivity activity;
    ArrayList<OriginalViewHolder> allViewHolder = new ArrayList<>();
    private int seasonNo;

    public interface OnTVSeriesEpisodeItemClickListener {
        void onEpisodeItemClickTvSeries(String type, View view, EpiModel obj, int position, OriginalViewHolder holder);
    }

    public void setOnEmbedItemClickListener(OnTVSeriesEpisodeItemClickListener mItemClickListener) {
        this.mOnTVSeriesEpisodeItemClickListener = mItemClickListener;
    }

    public EpisodeAdapter(Context context, List<EpiModel> items) {
        this.items = items;
        activity = (DetailsActivity) context;
        ctx = context;
    }

    @Override
    public EpisodeAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EpisodeAdapter.OriginalViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_episode_item, parent, false);
        vh = new EpisodeAdapter.OriginalViewHolder(v);
        return vh;
    }
    public void setNowPlaying(int nowPlaying){
        lastPosition = nowPlaying;
    }
    @Override
    public void onBindViewHolder(final EpisodeAdapter.OriginalViewHolder holder, final int position) {

        final EpiModel obj = items.get(position);
        holder.name.setText(obj.getEpi());
        holder.position = position;

        Picasso.get().load(obj.getImageUrl()).placeholder(R.drawable.poster_placeholder)
                .into(holder.episodIv);


// code chỗ này còn sai ạ, dùng để check tập phim đang xem dở = cách đổi màu chữ
        if (holder.position == lastPosition){
            //((DetailsActivity)ctx).setMediaUrlForTvSeries(obj.getStreamURL(), obj.getSeson(), obj.getEpi());
            //new DetailsActivity().initMoviePlayer(obj.getStreamURL(),obj.getServerType(),ctx);
            holder.name.setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
            holder.playStatusTv.setText("Last Played");
            holder.playStatusTv.setVisibility(View.VISIBLE);
            viewHolderArray[0] =holder;
        }else{
            chanColor(holder,position);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DetailsActivity)ctx).hideDescriptionLayout();
                ((DetailsActivity)ctx).showSeriesLayout();
                ((DetailsActivity)ctx).setMediaUrlForTvSeries(obj.getStreamURL(), obj.getSeson(), obj.getEpi());
                boolean castSession = ((DetailsActivity)ctx).getCastSession();
                //Toast.makeText(ctx, "cast:"+castSession, Toast.LENGTH_SHORT).show();
                if (!castSession) {
                    if (obj.getServerType().equalsIgnoreCase("embed")){
                        if (mOnTVSeriesEpisodeItemClickListener != null){
                            mOnTVSeriesEpisodeItemClickListener.onEpisodeItemClickTvSeries("embed", v, obj, position, viewHolder);
                        }
                    }else {
                        activity.initMoviePlayer(obj.getStreamURL(), obj.getServerType(), ctx);
                        if (mOnTVSeriesEpisodeItemClickListener != null){
                            mOnTVSeriesEpisodeItemClickListener.onEpisodeItemClickTvSeries("normal", v, obj, position, viewHolder);
                        }
                    }
                } else {
                    ((DetailsActivity)ctx).showQueuePopup(ctx, holder.cardView, ((DetailsActivity)ctx).getMediaInfo());

                }
                setNowPlaying(position);
                chanColor(viewHolderArray[0],position);
                holder.name.setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
                holder.playStatusTv.setText("Playing");
                holder.playStatusTv.setVisibility(View.VISIBLE);


                viewHolderArray[0] =holder;
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView name, playStatusTv;
        public MaterialRippleLayout cardView;
        public ImageView episodIv;
        public int position = -1;
        public OriginalViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            playStatusTv = v.findViewById(R.id.play_status_tv);
            cardView=v.findViewById(R.id.lyt_parent);
            episodIv=v.findViewById(R.id.image);
        }
    }

    private void chanColor(EpisodeAdapter.OriginalViewHolder holder, int pos){

        if (holder!=null){
            holder.name.setTextColor(ctx.getResources().getColor(R.color.grey_20));
            holder.playStatusTv.setVisibility(View.GONE);
        }
    }


}