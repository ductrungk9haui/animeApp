package animes.englishsubtitle.freemovieseries.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
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

import animes.englishsubtitle.freemovieseries.DetailsActivity;
import animes.englishsubtitle.freemovieseries.LoginActivity;
import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.models.CommonModels;
import animes.englishsubtitle.freemovieseries.models.single_details.SingleDetails;
import animes.englishsubtitle.freemovieseries.network.RetrofitClient;
import animes.englishsubtitle.freemovieseries.network.apis.SingleDetailsApi;
import animes.englishsubtitle.freemovieseries.utils.ItemAnimation;
import animes.englishsubtitle.freemovieseries.utils.PreferenceUtils;
import retrofit2.Retrofit;

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.OriginalViewHolder> {

    private List<CommonModels> items = new ArrayList<>();
    private Context ctx;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    public HomePageAdapter(Context context, List<CommonModels> items) {
        this.items = items;
        ctx = context;
    }


    @Override
    public HomePageAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        HomePageAdapter.OriginalViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_home_view, parent, false);
        vh = new HomePageAdapter.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final HomePageAdapter.OriginalViewHolder holder, final int position) {
        final CommonModels obj = items.get(position);
        holder.name.setText(obj.getTitle());
        Picasso.get().load(obj.getImageUrl()).placeholder(R.drawable.poster_placeholder).into(holder.image);
        //holder.qualityTv.setText(obj.getQuality());
        if (Integer.parseInt(obj.getIsPaid()) == 1) {
            holder.qualityTv.setText("VIP");
        } else {
            holder.qualityTv.setText("Free");
        }
        holder.countEp.setVisibility(View.INVISIBLE);
        if (obj.getStatus_movie().equals("On-going")) {
            holder.countEp.setVisibility(View.VISIBLE);
            if (obj.getCount_status_movie() != null) {
                holder.countEp.setText("EP" + obj.getCount_status_movie());
            } else {
                holder.countEp.setText("EP0");
            }
            holder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_on_going);

        } else if (obj.getStatus_movie().equals("Trailer")) {
            holder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_trailer);
        } else if (obj.getStatus_movie().equals("New")) {
            holder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_on_going);
        } else if (obj.getStatus_movie().equals("Aired")) {
            holder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_on_going);
        } else if (obj.getStatus_movie().equals("Full")) {
            holder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie);
        }
        holder.statusMovie.setText(String.valueOf(obj.getStatus_movie()));
        holder.releaseDateTv.setText(obj.getReleaseDate());


        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreferenceUtils.isMandatoryLogin(ctx)) {
                    if (PreferenceUtils.isLoggedIn(ctx)) {
                        Intent intent = new Intent(ctx, DetailsActivity.class);
                        intent.putExtra("vType", obj.getVideoType());
                        intent.putExtra("id", obj.getId());

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ctx.startActivity(intent);
                    } else {
                        ctx.startActivity(new Intent(ctx, LoginActivity.class));
                    }
                } else {
                    Intent intent = new Intent(ctx, DetailsActivity.class);
                    intent.putExtra("vType", obj.getVideoType());
                    intent.putExtra("id", obj.getId());

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ctx.startActivity(intent);
                }

            }
        });

        setAnimation(holder.itemView, position);

    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name, qualityTv, releaseDateTv, statusMovie, countEp;
        public MaterialRippleLayout lyt_parent;


        public OriginalViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            name = v.findViewById(R.id.name);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            qualityTv = v.findViewById(R.id.quality_tv);
            statusMovie = v.findViewById(R.id.status_movie);
            countEp = v.findViewById(R.id.count_ep);
            releaseDateTv = v.findViewById(R.id.release_date_tv);
        }
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
