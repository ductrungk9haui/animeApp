package tvseries.koreandramaengsub.freemovieapp.nav_fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.volcaniccoder.bottomify.BottomifyNavigationView;
import com.volcaniccoder.bottomify.OnNavigationItemChangeListener;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tvseries.koreandramaengsub.freemovieapp.Config;
import tvseries.koreandramaengsub.freemovieapp.MainActivity;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.fragments.DownFragment;
import tvseries.koreandramaengsub.freemovieapp.fragments.HomeFragment;
import tvseries.koreandramaengsub.freemovieapp.fragments.MoviesFragment;
import tvseries.koreandramaengsub.freemovieapp.fragments.TvSeriesFragment;

import static android.content.Context.MODE_PRIVATE;

public class MainHomeFragment extends Fragment {
    @BindView(R.id.bottomify_nav) BottomifyNavigationView bottomifyNavigationViewDark;
    @BindView(R.id.bottomify_nav_light) BottomifyNavigationView bottomifyNavigationViewLight;
    private MainActivity mActivity;
    Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home, container, false);
        mActivity = (MainActivity) getActivity();
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            //bottomifyNavigationView
            bottomifyNavigationViewDark.setVisibility(View.VISIBLE);
            bottomifyNavigationViewDark.setBackgroundColor(getResources().getColor(R.color.black_window_light));
        } else {
            //bottomifyNavigationView light
            bottomifyNavigationViewLight.setVisibility(View.VISIBLE);
            bottomifyNavigationViewLight.setBackgroundColor(getResources().getColor(R.color.white));
        }

        //bottomifyNavigationView
        bottomifyNavigationViewDark.setActiveNavigationIndex(0);
        bottomifyNavigationViewDark.setOnNavigationItemChangedListener(new OnNavigationItemChangeListener() {
            @Override
            public void onNavigationItemChanged(@NotNull BottomifyNavigationView.NavigationItem navigationItem) {
                switch (navigationItem.getPosition()) {
                    case 0:
                        loadFragment(new HomeFragment());
                        break;
                    case 1:
                        loadFragment(new MoviesFragment());
                        Config.isOpenChildFragment = true;
                        break;
                    case 2:
                        loadFragment(new TvSeriesFragment());
                        Config.isOpenChildFragment = true;
                        break;
                    case 3:
                        loadFragment(new DownFragment());
                        Config.isOpenChildFragment = true;
                        break;
                    case 4:
                        loadFragment(new FavoriteFragment());
                        Config.isOpenChildFragment = true;
                        break;
                    //case 5:

                   // case 4:


                }
            }
        });

        //bottomify light
        bottomifyNavigationViewLight.setActiveNavigationIndex(0);
        bottomifyNavigationViewLight.setOnNavigationItemChangedListener(new OnNavigationItemChangeListener() {
            @Override
            public void onNavigationItemChanged(@NotNull BottomifyNavigationView.NavigationItem navigationItem) {
                switch (navigationItem.getPosition()){
                    case 0:
                        loadFragment(new HomeFragment());
                        break;
                    case 1:
                        loadFragment(new MoviesFragment());
                        break;
                    case 2:
                        loadFragment(new TvSeriesFragment());
                        break;
                    case 3:
                        loadFragment(new DownFragment());
                        break;
                    case 4:
                        loadFragment(new FavoriteFragment());
                        break;
                }
            }
        });


        loadFragment(new HomeFragment());

    }

    //----load fragment----------------------
    private boolean loadFragment(Fragment fragment){
        if (fragment!=null){
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,fragment)
                    .commit();

            return true;
        }
        return false;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mUnbinder!=null){
            mUnbinder.unbind();
        }
    }

}