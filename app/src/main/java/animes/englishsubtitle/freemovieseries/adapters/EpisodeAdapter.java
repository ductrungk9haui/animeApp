package animes.englishsubtitle.freemovieseries.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import animes.englishsubtitle.freemovieseries.DetailsActivity;
import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.models.EpiModel;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.OriginalViewHolder> {

    private List<EpiModel> items = new ArrayList<>();
    private Context ctx;
    final EpisodeAdapter.OriginalViewHolder[] viewHolderArray = {null};
    private OnTVSeriesEpisodeItemClickListener mOnTVSeriesEpisodeItemClickListener;
    EpisodeAdapter.OriginalViewHolder viewHolder;
    int i=0;
    private int lastPosition = -1;
    private DetailsActivity activity;
    private int seasonNo;

    public interface OnTVSeriesEpisodeItemClickListener {
        void onEpisodeItemClickTvSeries(String type, EpiModel obj, int position);
    }

    public void setOnEmbedItemClickListener(OnTVSeriesEpisodeItemClickListener mItemClickListener) {
        this.mOnTVSeriesEpisodeItemClickListener = mItemClickListener;
    }

    public EpisodeAdapter(Context context, List<EpiModel> items) {
        this.items = items;
        activity = (DetailsActivity) context;
        ctx = context;
        viewHolder = null;
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
        if(obj.getIs_epi_paid().equals("1")){
            holder.statusEpisode.setVisibility(View.VISIBLE );
            holder.statusEpisode.setText("VIP");
        }
        holder.position = position;

        Picasso.get().load(obj.getImageUrl()).placeholder(R.drawable.poster_placeholder)
                .into(holder.episodIv);

        if (holder.position == lastPosition){
            //((DetailsActivity)ctx).setMediaUrlForTvSeries(obj.getStreamURL(), obj.getSeson(), obj.getEpi());
            //new DetailsActivity().initMoviePlayer(obj.getStreamURL(),obj.getServerType(),ctx);
            holder.name.setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
            holder.playStatusTv.setText("Last Played");
            holder.playStatusTv.setVisibility(View.VISIBLE);
            viewHolder = holder;
        }else{
            chanColor(holder,position);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEpisode(holder,position,obj);
            }
        });
        if(position == 0 && viewHolder==null){
            viewHolder = holder;
        }

    }
    private void onClickEpisode(OriginalViewHolder holder,int position,EpiModel obj ){
        ((DetailsActivity)ctx).hideDescriptionLayout();
        ((DetailsActivity)ctx).showSeriesLayout();
        ((DetailsActivity)ctx).setMediaUrlForTvSeries(obj.getStreamURL(), obj.getSeson(), obj.getEpi());
        boolean castSession = ((DetailsActivity)ctx).getCastSession();
        //Toast.makeText(ctx, "cast:"+castSession, Toast.LENGTH_SHORT).show();
        if (!castSession) {
            if (obj.getServerType().equalsIgnoreCase("embed")){
                if (mOnTVSeriesEpisodeItemClickListener != null){
                    mOnTVSeriesEpisodeItemClickListener.onEpisodeItemClickTvSeries("embed", obj, position);
                }
            }else {
                activity.initMoviePlayer(obj.getStreamURL(), obj.getServerType(), ctx);
                if (mOnTVSeriesEpisodeItemClickListener != null){
                    mOnTVSeriesEpisodeItemClickListener.onEpisodeItemClickTvSeries("normal", obj, position);
                }
            }
        } else {
            ((DetailsActivity)ctx).showQueuePopup(ctx, ((DetailsActivity)ctx).getMediaInfo());

        }
        setNowPlaying(position);
        if(holder!=null){
            holder.name.setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
            holder.playStatusTv.setText("Playing");
            holder.playStatusTv.setVisibility(View.VISIBLE);
            if(viewHolder!=holder){
                chanColor(viewHolder ,position);
                viewHolder = holder;
            }
        }
    }
    public void getWatchEpisode(){
        if(items.size()>0){
            if(lastPosition == -1) lastPosition = 0;
            onClickEpisode(viewHolder, lastPosition, items.get(lastPosition));
        }else{
            Toast.makeText(ctx, "Wait for Episode updating, please try later", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView name, playStatusTv, statusEpisode;
        public MaterialRippleLayout cardView;
        public ImageView episodIv;
        public int position = -1;
        public OriginalViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            statusEpisode= v.findViewById(R.id.status_episode);
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