package animes.englishsubtitle.freemovieseries.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appodeal.ads.native_ad.views.NativeAdViewAppWall;
import com.google.android.ads.nativetemplates.TemplateView;

import java.util.ArrayList;
import java.util.List;

import animes.englishsubtitle.freemovieseries.utils.ads.AdsController;
import animes.englishsubtitle.freemovieseries.ItemMovieActivity;
import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.models.CommonModels;
import animes.englishsubtitle.freemovieseries.models.GenreModel;
import animes.englishsubtitle.freemovieseries.utils.ItemAnimation;

public class GenreHomeAdapter extends RecyclerView.Adapter<GenreHomeAdapter.OriginalViewHolder> {

    private List<GenreModel> items = new ArrayList<>();
    private List<CommonModels> listData = new ArrayList<>();
    private Context ctx;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;
    Activity activity;
    private AdsController adsController;


    public GenreHomeAdapter(Activity activity, Context context, List<GenreModel> items) {
        this.activity = activity;
        this.items = items;
        ctx = context;
        adsController = AdsController.getInstance(activity);
    }


    @Override
    public GenreHomeAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        GenreHomeAdapter.OriginalViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_genre_home, parent, false);
        vh = new GenreHomeAdapter.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(GenreHomeAdapter.OriginalViewHolder holder, final int position) {

        final GenreModel obj = items.get(position);
        holder.name.setText(obj.getName());

        HomePageAdapter adapter = new HomePageAdapter(ctx, obj.getList());
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(adapter);

        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, ItemMovieActivity.class);
                intent.putExtra("id", obj.getId());
                intent.putExtra("title", obj.getName());
                intent.putExtra("type", "genre");
                ctx.startActivity(intent);
            }
        });
        holder.titleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, ItemMovieActivity.class);
                intent.putExtra("id", obj.getId());
                intent.putExtra("title", obj.getName());
                intent.putExtra("type", "genre");
                ctx.startActivity(intent);
            }
        });
        if (adsController.isAdsEnable() && (position + 1) % 2 == 0) {
            holder.adContainer.setVisibility(View.VISIBLE);
            adsController.showNativeAds(holder.adContainer,false);
        } else {
            holder.nativeAdView.setVisibility(View.GONE);
            holder.adContainer.setVisibility(View.GONE);
        }

        setAnimation(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder  {

        public TextView name;
        RecyclerView recyclerView;
        Button btnMore;
        View titleLayout;
        TemplateView nativeAdView;;
        NativeAdViewAppWall appWallNativeAdas;
        View adContainer;

        public OriginalViewHolder(View v) {
            super(v);
            adContainer = v.findViewById(R.id.ads_container1);
            name = v.findViewById(R.id.tv_name);
            recyclerView = v.findViewById(R.id.recyclerView);
            btnMore = v.findViewById(R.id.btn_more);
            titleLayout = v.findViewById(R.id.title_layout);
            nativeAdView = adContainer.findViewById(R.id.admob_nativead_template);
           // bannerViewStartApp = v.findViewById(R.id.appodealBannerView);
            appWallNativeAdas = adContainer.findViewById(R.id.appodel_native_ad_app_wall);
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
