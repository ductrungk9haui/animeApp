package tvseries.koreandramaengsub.freemovieapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerView;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.ads.nativetemplates.TemplateView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tvseries.koreandramaengsub.freemovieapp.DetailsActivity;
import tvseries.koreandramaengsub.freemovieapp.LoginActivity;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.models.CommonModels;
import tvseries.koreandramaengsub.freemovieapp.network.model.config.AdsConfig;
import tvseries.koreandramaengsub.freemovieapp.utils.Constants;
import tvseries.koreandramaengsub.freemovieapp.utils.ItemAnimation;
import tvseries.koreandramaengsub.freemovieapp.utils.PreferenceUtils;
import tvseries.koreandramaengsub.freemovieapp.utils.ads.BannerAds;
import tvseries.koreandramaengsub.freemovieapp.utils.ads.NativeAds;

public class CommonGridAdapter extends RecyclerView.Adapter<CommonGridAdapter.OriginalViewHolder> {

    private List<CommonModels> items = new ArrayList<>();
    private Context ctx;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;
    private int adCount;
    private boolean isAdActive = false;
    private int fromIndex;
    AdsConfig adsConfig;
    private Activity activity;

    @Override
    public void onViewRecycled(@NonNull OriginalViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public CommonGridAdapter(Activity activity, Context context, List<CommonModels> items) {
        this.items = items;
        this.activity = activity;
        ctx = context;
        adCount = 0;
        fromIndex = 9;
        isAdActive = getConfigAd();
    }

    public void setNotifyDataSetChanged() {
        if (items.size() == 0) {
            adCount = 0;
            fromIndex = 9;
        } else {
            if (isAdActive) {
                int sizeItem = items.size();
                while (sizeItem >= fromIndex) {
                    items.add(9 + adCount, new CommonModels());
                    sizeItem = sizeItem - 9;
                    adCount = adCount + 10;
                    if (sizeItem < fromIndex) {
                        fromIndex = 9 + adCount;
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean getConfigAd() {
        adsConfig = new DatabaseHelper(ctx).getConfigurationData().getAdsConfig();
        return !(PreferenceUtils.isLoggedIn(activity) && PreferenceUtils.isActivePlan(activity));
    }

    @Override
    public CommonGridAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        CommonGridAdapter.OriginalViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_image_albums, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(CommonGridAdapter.OriginalViewHolder holder, final int position) {
        if (isAdActive && (position + 1) % 10 == 0) {
            holder.mainLayout.setVisibility(View.GONE);
            if (adsConfig.getAdsEnable().equals("1")) {
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

            holder.mainLayout.setVisibility(View.VISIBLE);
            holder.nativeAdView.setVisibility(View.GONE);
            holder.bannerViewStartApp.setVisibility(View.GONE);
            holder.adview.setVisibility(View.GONE);

            final CommonModels obj = items.get(position);
            setAnimation(holder.itemView, position);
            holder.qualityTv.setText(obj.getQuality());
            holder.releaseDateTv.setText(obj.getReleaseDate());
            holder.name.setText(obj.getTitle());

            Picasso.get().load(obj.getImageUrl()).placeholder(R.drawable.poster_placeholder).into(holder.image);

            holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PreferenceUtils.isMandatoryLogin(ctx)) {
                        if (PreferenceUtils.isLoggedIn(ctx)) {
                            goToDetailsActivity(obj);
                        } else {
                            ctx.startActivity(new Intent(ctx, LoginActivity.class));
                        }
                    } else {
                        goToDetailsActivity(obj);
                    }
                }
            });
        }
    }


    private void goToDetailsActivity(CommonModels obj) {
        Intent intent = new Intent(ctx, DetailsActivity.class);
        intent.putExtra("vType", obj.getVideoType());
        intent.putExtra("id", obj.getId());
        ctx.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name, qualityTv, releaseDateTv;
        public MaterialRippleLayout lyt_parent;
        public View mainLayout;
        public View view;

        public CardView cardView;
        TemplateView nativeAdView;
        RelativeLayout adview;
        BannerView bannerViewStartApp;

        public OriginalViewHolder(View v) {
            super(v);
            view = v;
            image = v.findViewById(R.id.image);
            name = v.findViewById(R.id.name);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            qualityTv = v.findViewById(R.id.quality_tv);
            releaseDateTv = v.findViewById(R.id.release_date_tv);
            cardView = v.findViewById(R.id.top_layout);
            nativeAdView = v.findViewById(R.id.admob_nativead_template);
            adview = v.findViewById(R.id.adView);
            bannerViewStartApp = v.findViewById(R.id.appodealBannerView);
            mainLayout = v.findViewById(R.id.main_content);
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