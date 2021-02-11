package animes.englishsubtitle.freemovieseries;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import animes.englishsubtitle.freemovieseries.adapters.LiveTvAdapter2;
import animes.englishsubtitle.freemovieseries.adapters.SearchAdapter;
import animes.englishsubtitle.freemovieseries.network.RetrofitClient;
import animes.englishsubtitle.freemovieseries.network.apis.SearchApi;
import animes.englishsubtitle.freemovieseries.network.model.CommonModel;
import animes.englishsubtitle.freemovieseries.network.model.SearchModel;
import animes.englishsubtitle.freemovieseries.network.model.TvModel;
import animes.englishsubtitle.freemovieseries.utils.ApiResources;
import animes.englishsubtitle.freemovieseries.utils.RtlUtils;
import animes.englishsubtitle.freemovieseries.utils.ToastMsg;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class SearchResultActivity extends AppCompatActivity implements SearchAdapter.OnItemClickListener {
    private TextView tvTitle, movieTitle, tvSeriesTv, searchQueryTv, countTv;
    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView movieRv, tvRv, tvSeriesRv;
    private SearchAdapter movieAdapter, tvSeriesAdapter;
    private LiveTvAdapter2 tvAdapter;
    private List<CommonModel> movieList = new ArrayList<>();
    private List<TvModel> tvList = new ArrayList<>();
    private List<CommonModel> tvSeriesList = new ArrayList<>();
    private List<String> titleList = new ArrayList<>();
    private List<String> idList = new ArrayList<>();

    private ApiResources apiResources;

    private String URL = null;
    private boolean isLoading = false;
    private ProgressBar progressBar;
    private int pageCount = 1;
    private LinearLayout movieLayout, tvSeriesLayout, tvLayout;
    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;

    private String type;
    private String query = "";
    private boolean isSplitQuery;
    private int indexKey;
    private ArrayList<String> mKeywordList = new ArrayList<>();
    private int range_to, range_from, tvCategoryId, genreId, countryId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "search_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        indexKey = 0;
        isSplitQuery = false;
        mKeywordList.clear();
        query = getIntent().getStringExtra("q");
        type = getIntent().getStringExtra("type");
        range_to = getIntent().getIntExtra("range_to", 0);
        range_from = getIntent().getIntExtra("range_from", 0);
        tvCategoryId = getIntent().getIntExtra("tv_category_id", 0);
        genreId = getIntent().getIntExtra("genre_id", 0);
        countryId = getIntent().getIntExtra("country_id", 0);

        tvTitle = findViewById(R.id.title);
        tvLayout = findViewById(R.id.tv_layout);
        movieLayout = findViewById(R.id.movie_layout);
        tvSeriesLayout = findViewById(R.id.tv_series_layout);
        tvTitle = findViewById(R.id.tv_title);
        movieTitle = findViewById(R.id.movie_title);
        tvSeriesTv = findViewById(R.id.tv_series_title);
        movieRv = findViewById(R.id.movie_rv);
        tvRv = findViewById(R.id.tv_rv);
        tvSeriesRv = findViewById(R.id.tv_series_rv);
        searchQueryTv = findViewById(R.id.title_tv);
        toolbar = findViewById(R.id.toolbar);
        countTv = findViewById(R.id.result_count);
        searchQueryTv.setText("Showing Result for : " + query);

        progressBar = findViewById(R.id.item_progress_bar);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search Result");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        URL = new ApiResources().getSearchUrl() + "&&q=" + query + "&&page=";

        coordinatorLayout = findViewById(R.id.coordinator_lyt);
        movieRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        //movieRv.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(this, 4), true));
        movieRv.setHasFixedSize(true);
        movieAdapter = new SearchAdapter(movieList, this);
        movieAdapter.setOnItemClickListener(this);
        movieRv.setAdapter(movieAdapter);


        tvRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        //tvRv.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(this, 8), true));
        tvRv.setHasFixedSize(true);
        tvAdapter = new LiveTvAdapter2(this, tvList);
        tvRv.setAdapter(tvAdapter);

        tvSeriesRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        //tvSeriesRv.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(this, 4), true));
        tvSeriesRv.setHasFixedSize(true);
        tvSeriesAdapter = new SearchAdapter(tvSeriesList, this);
        tvSeriesAdapter.setOnItemClickListener(this);
        tvSeriesRv.setAdapter(tvSeriesAdapter);

        if (query != null) {
            mKeywordList.clear();
            titleList.clear();
            idList.clear();
            movieList.clear();
            tvList.clear();
            tvSeriesList.clear();
            getSearchData();
        }
    }


    public void getSearchData() {
        Log.d("TRUNG: ", "search key " + query);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SearchApi searchApi = retrofit.create(SearchApi.class);
        Call<SearchModel> call = searchApi.getSearchData(Config.API_KEY, query, type, range_to, range_from, tvCategoryId, genreId, countryId);
        call.enqueue(new Callback<SearchModel>() {
            @Override
            public void onResponse(Call<SearchModel> call, retrofit2.Response<SearchModel> response) {
                if (response.code() == 200) {
                    String result = "Founded: ";
                    SearchModel searchModel = response.body();
                    addItemCommonModel(movieList,searchModel.getMovie());
                    addItemTvModel(tvList,searchModel.getTvChannels());
                    addItemCommonModel(tvSeriesList,searchModel.getTvseries());
                    if (!isSplitQuery) {
                        isSplitQuery = true;
                        getKeyword(query);
                    }
                    if (!(tvList.size() == 0 && movieList.size() == 0 && tvSeriesList.size() == 0)){
                       /* indexKey = -1;
                        isSplitQuery = false;*/
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        countTv.setVisibility(View.VISIBLE);

                        if (movieList.size() > 0) {
                            result = "Movie : " + movieList.size();
                            movieAdapter.notifyDataSetChanged();
                            movieLayout.setVisibility(View.VISIBLE);
                        } else {
                            movieLayout.setVisibility(View.GONE);
                        }

                        if (tvList.size() > 0) {
                            result = result + "  TV : " + tvList.size();
                            tvAdapter.notifyDataSetChanged();
                            tvLayout.setVisibility(View.VISIBLE);
                        } else {
                            tvLayout.setVisibility(View.GONE);
                        }

                        if (tvSeriesList.size() > 0) {
                            result = result + "  Series : " + tvSeriesList.size();
                            tvSeriesAdapter.notifyDataSetChanged();
                            tvSeriesLayout.setVisibility(View.VISIBLE);
                        } else {
                            tvSeriesLayout.setVisibility(View.GONE);
                        }
                    }
                    if (mKeywordList.size() > 0) {
                        if (indexKey < mKeywordList.size() -1 && isSplitQuery) {
                            query = mKeywordList.get(indexKey);
                            indexKey++;
                            getSearchData();
                            return;
                        }else{
                            indexKey = -1;
                            isSplitQuery = false;
                            if(shimmerFrameLayout.getVisibility() != View.GONE){
                                result = "Not found with your keyword. Please try another keyword";
                                shimmerFrameLayout.stopShimmer();
                                tvSeriesLayout.setVisibility(View.GONE);
                                tvLayout.setVisibility(View.GONE);
                                movieLayout.setVisibility(View.GONE);
                                shimmerFrameLayout.setVisibility(View.GONE);
                                coordinatorLayout.setVisibility(View.VISIBLE);
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                    countTv.setText(result);
                } else {
                    new ToastMsg(SearchResultActivity.this).toastIconSuccess("Something went wrong.");
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(Call<SearchModel> call, Throwable t) {

                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                coordinatorLayout.setVisibility(View.VISIBLE);
                t.printStackTrace();
                new ToastMsg(SearchResultActivity.this).toastIconSuccess("Something went wrong.");
            }
        });


    }

    private void addItemCommonModel(List<CommonModel> mainList, List<CommonModel> addList){
        if(addList.isEmpty())return;
        for(int i = 0; i< addList.size(); i++){
            CommonModel object = addList.get(i);
            if(!(titleList.contains(object.getTitle()) && idList.contains(object.getVideosId()))){
                titleList.add(object.getTitle());
                idList.add(object.getVideosId());
                mainList.add(addList.get(i));
            }
        }
    }
    private void addItemTvModel(List<TvModel> mainList, List<TvModel> addList){
        if(addList.isEmpty())return;
        for(int i = 0; i< addList.size(); i++){
            TvModel object = addList.get(i);
            if(!(titleList.contains(object.getTvName()) && idList.contains(object.getLiveTvId()))){
                titleList.add(object.getTvName());
                idList.add(object.getLiveTvId());
                mainList.add(addList.get(i));
            }
        }
    }

    private void getKeyword(String query) {
        splitQuery(query);
        /*for (int i = 1; i < query.length() - 3; i++) {
            String key = query.substring(0, query.length() - i);
            String keyReverse = query.substring(i);
            mKeywordList.add(key);
            mKeywordList.add(keyReverse);
        }*/
       // Collections.sort(mKeywordList,new MyComparator("0"));
    }

    private void splitQuery(String query) {
        query = query.replaceAll(" ","");
        if(query.length() <= 4){
            mKeywordList.add(query);
            return;
        }
        int index1 = 0;
        for(int i=0;i<query.length()-4;i++){
            if(index1 > query.length()-4 - index1)return;
            String sub1 = query.substring(index1, 4 + index1);
            String sub2 = query.substring(query.length()- 4 - index1, query.length() - index1);
            if(!mKeywordList.contains(sub1)){
                mKeywordList.add(sub1);
            }
            if(!mKeywordList.contains(sub2)){
                mKeywordList.add(sub2);
            }
            index1++;
        }
        /*String sub1 = query.substring(0, query.length() / 2);
        String sub2 = query.substring(query.length() / 2);

        if (sub1.length() >= 3) {
            mKeywordList.add(sub1);
            splitQuery(sub1);
        }
        if (sub2.length() >= 3) {
            mKeywordList.add(sub2);
            splitQuery(sub2);
        }*/

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(CommonModel commonModel) {

        String type = "";
        if (commonModel.getIsTvseries().equals("1")) {
            type = "tvseries";
        } else {
            type = "movie";
        }

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("vType", type);
        intent.putExtra("id", commonModel.getVideosId());
        startActivity(intent);

    }

    public class MyComparator implements java.util.Comparator<String> {

        private int referenceLength;

        public MyComparator(String reference) {
            super();
            this.referenceLength = reference.length();
        }

        public int compare(String s1, String s2) {
            int dist1 = Math.abs(s1.length() - referenceLength);
            int dist2 = Math.abs(s2.length() - referenceLength);

            return dist1 - dist2;
        }
    }
}
