package animes.englishsubtitle.freemovieseries.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerView;
import com.google.android.ads.nativetemplates.TemplateView;

import java.util.ArrayList;
import java.util.List;

import animes.englishsubtitle.freemovieseries.ItemMovieActivity;
import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.models.CommonModels;
import animes.englishsubtitle.freemovieseries.models.GenreModel;
import animes.englishsubtitle.freemovieseries.network.model.config.AdsConfig;
import animes.englishsubtitle.freemovieseries.utils.Constants;
import animes.englishsubtitle.freemovieseries.utils.ItemAnimation;
import animes.englishsubtitle.freemovieseries.utils.PreferenceUtils;
import animes.englishsubtitle.freemovieseries.utils.ads.BannerAds;
import animes.englishsubtitle.freemovieseries.utils.ads.NativeAds;

public class GenreHomeAdapter extends RecyclerView.Adapter<GenreHomeAdapter.OriginalViewHolder> {

    private List<GenreModel> items = new ArrayList<>();
    private List<CommonModels> listData = new ArrayList<>();
    private Context ctx;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;
    private boolean isAdActive = false;
    AdsConfig adsConfig;
    Activity activity;


    public GenreHomeAdapter(Activity activity, Context context, List<GenreModel> items) {
        this.activity = activity;
        this.items = items;
        ctx = context;
        isAdActive = getConfigAd();
    }

    private boolean getConfigAd() {
        adsConfig = new DatabaseHelper(ctx).getConfigurationData().getAdsConfig();
        return !(PreferenceUtils.isLoggedIn(activity) && PreferenceUtils.isActivePlan(activity));
    }

    public void onDestroy() {
        NativeAds.releaseAdmobNativeAd();
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
        if (isAdActive && (position + 1) % 2 == 0) {
            if(adsConfig.getAdsEnable().equals("1")){
                if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                    holder.nativeAdView.setVisibility(View.VISIBLE);
                    NativeAds.showAdmobNativeAds(activity, holder.nativeAdView);
                } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.START_APP)) {
                    holder.bannerViewStartApp.setVisibility(View.VISIBLE);
                    Appodeal.setBannerViewId(holder.bannerViewStartApp.getId());
                    Appodeal.show(activity, Appodeal.BANNER_VIEW);
                } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.NETWORK_AUDIENCE)) {
                    holder.adview.setVisibility(View.VISIBLE);
                    BannerAds.showFANBanner(ctx, holder.adview);
                }
            }
        } else {
            holder.nativeAdView.setVisibility(View.GONE);
            holder.bannerViewStartApp.setVisibility(View.GONE);
            holder.adview.setVisibility(View.GONE);
        }

        setAnimation(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        RecyclerView recyclerView;
        Button btnMore;
        View titleLayout;
        TemplateView nativeAdView;
        RelativeLayout adview;
        BannerView bannerViewStartApp;

        public OriginalViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            recyclerView = v.findViewById(R.id.recyclerView);
            btnMore = v.findViewById(R.id.btn_more);
            titleLayout = v.findViewById(R.id.title_layout);
            nativeAdView = v.findViewById(R.id.admob_nativead_template);
            adview = v.findViewById(R.id.adView);
            bannerViewStartApp = v.findViewById(R.id.appodealBannerView);
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
