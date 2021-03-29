package animes.englishsubtitle.freemovieseries.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdScrollView;
import com.facebook.ads.NativeAdViewAttributes;
import com.facebook.ads.NativeAdsManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import animes.englishsubtitle.freemovieseries.utils.ads.AdsController;
import animes.englishsubtitle.freemovieseries.DetailsActivity;
import animes.englishsubtitle.freemovieseries.LoginActivity;
import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.models.CommonModels;
import animes.englishsubtitle.freemovieseries.utils.ItemAnimation;
import animes.englishsubtitle.freemovieseries.utils.PreferenceUtils;

public class CommonGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int POST_TYPE = 0;
    private static final int AD_TYPE = 1;

    private final int NATIVE_AD_VIEW_HEIGHT_DP = 300;
    private List<CommonModels> items = new ArrayList<>();
    private Context ctx;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;
    private int adCount;
    private int fromIndex;
    private AdsController adsController;
    private Activity activity;
    private NativeAdsManager mNativeAdsManager;
    private NativeAdViewAttributes mNativeAttributes;



    public CommonGridAdapter(Activity activity, Context context, List<CommonModels> items) {
        this.items = items;
        this.activity = activity;
        adsController = AdsController.getInstance(activity);
        ctx = context;
        adCount = 0;
        fromIndex = 9;
    }

    public void setAdsManager(NativeAdsManager manager, NativeAdViewAttributes attributes) {
        Log.w("AdsHelper in Recycle", "setAdsManager");
        mNativeAdsManager = manager;
        mNativeAttributes = attributes;
        notifyItemChanged(9);
    }

    public void setNotifyDataSetChanged() {
        if (items.size() == 0) {
            adCount = 0;
            fromIndex = 9;
        } else {
            if (adsController.isAdsEnable()) {
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


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == AD_TYPE) {
            View v = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.ads_container, parent, false);
            return new AdHolder(v);
        } else {
            CommonGridAdapter.OriginalViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_image_albums, parent, false);
            vh = new OriginalViewHolder(v);
            return vh;
        }


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == AD_TYPE) {
            AdHolder adHolder = (AdHolder) holder;
            if (mNativeAdsManager != null) {
                if(adHolder.fanAdsScrollViewContainer.getChildCount()==0){
                    NativeAdScrollView fanAdsScrollView = new NativeAdScrollView(activity, mNativeAdsManager, NATIVE_AD_VIEW_HEIGHT_DP, mNativeAttributes);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    fanAdsScrollView.setLayoutParams(params);
                    adHolder.fanAdsScrollViewContainer.removeAllViews();
                    adHolder.fanAdsScrollViewContainer.addView(fanAdsScrollView);
                }
                setAnimation(adHolder.fanAdsScrollViewContainer, position);
            }
        } else {
            OriginalViewHolder viewHolder = (OriginalViewHolder) holder;

            final CommonModels obj = items.get(position);
            // holder.qualityTv.setText(obj.getQuality());
//            if(obj.isPaid.equals("1")){
//                holder.qualityTv.setText("VIP");
//            }else {
//                holder.qualityTv.setText("Free");
//            }
            viewHolder.qualityTv.setText(obj.getStatus_sub());

            viewHolder.countEp.setVisibility(View.INVISIBLE);
            if (Integer.parseInt(obj.getCount_status_movie()) >= 0) {
                if (obj.getStatus_movie().equals("On-going")) {
                    viewHolder.countEp.setVisibility(View.VISIBLE);
                    if (obj.getCount_status_movie() != null) {
                        viewHolder.countEp.setText("EP" + String.valueOf(obj.getCount_status_movie()));
                    } else {
                        viewHolder.countEp.setText("EP0");
                    }
                    viewHolder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_on_going);
                } else if (obj.getStatus_movie().equals("Trailer")) {
                    viewHolder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_trailer);
                } else if (obj.getStatus_movie().equals("Full")) {
                    viewHolder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie);
                }
            } else if (Integer.parseInt(obj.getCount_status_movie()) == -1) {
                if (obj.getStatus_movie().equals("New")) {
                    viewHolder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_trailer);
                } else if (obj.getStatus_movie().equals("Trailer")) {
                    viewHolder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_trailer);
                } else {
                    viewHolder.statusMovie.setBackgroundResource(R.drawable.circle_status_movie_on_going);
                }
            }
            viewHolder.statusMovie.setText(String.valueOf(obj.getStatus_movie()));
            viewHolder.releaseDateTv.setText(obj.getTotal_view());
            viewHolder.name.setText(obj.getTitle());

            Picasso.get().load(obj.getImageUrl()).placeholder(R.drawable.poster_placeholder).into(viewHolder.image);

            viewHolder.lyt_parent.setOnClickListener(new View.OnClickListener() {
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
            setAnimation(viewHolder.itemView, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (adsController.isAdsEnable() && (position + 1) % 10 == 0) ? AD_TYPE : POST_TYPE;
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
        public TextView name, qualityTv, releaseDateTv, statusMovie, countEp;
        public MaterialRippleLayout lyt_parent;
        public View mainLayout;
        public View view;

        public CardView cardView;

        public OriginalViewHolder(View v) {
            super(v);
            view = v;
            image = v.findViewById(R.id.image);
            name = v.findViewById(R.id.name);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            qualityTv = v.findViewById(R.id.quality_tv);
            statusMovie = v.findViewById(R.id.status_movie);
            countEp = v.findViewById(R.id.count_ep);
            releaseDateTv = v.findViewById(R.id.release_date_tv);
            cardView = v.findViewById(R.id.top_layout);
            mainLayout = v.findViewById(R.id.main_content);
        }

    }

    public class AdHolder extends RecyclerView.ViewHolder {
        public LinearLayout fanAdsScrollViewContainer;

        public AdHolder(View v) {
            super(v);
            fanAdsScrollViewContainer = v.findViewById(R.id.hscroll_container);
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
            Log.w("AdsHelper in Recycle", "animate position " + position);
            ItemAnimation.animate(view, on_attach ? position : -1, animation_type);
            lastPosition = position;
        }
    }

}