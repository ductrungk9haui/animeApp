package tvseries.koreandramaengsub.freemovieapp.fragments;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import tvseries.koreandramaengsub.freemovieapp.Config;
import tvseries.koreandramaengsub.freemovieapp.MainActivity;
import tvseries.koreandramaengsub.freemovieapp.ProfileActivity;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.SubscriptionActivity;
import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.network.RetrofitClient;
import tvseries.koreandramaengsub.freemovieapp.network.apis.MovieRequestApi;
import tvseries.koreandramaengsub.freemovieapp.network.apis.UserDataApi;
import tvseries.koreandramaengsub.freemovieapp.network.model.User;
import tvseries.koreandramaengsub.freemovieapp.utils.PreferenceUtils;
import tvseries.koreandramaengsub.freemovieapp.utils.ToastMsg;
import android.content.SharedPreferences;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdvancedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdvancedFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private MainActivity mActivity;
    private Unbinder mUnbinder;
    private DatabaseHelper mDBHelper;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AdvancedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdvancdedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdvancedFragment newInstance(String param1, String param2) {
        AdvancedFragment fragment = new AdvancedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advancded, container, false);
        mActivity = (MainActivity) getActivity();
        mUnbinder = ButterKnife.bind(this,view);
        mActivity.setTitle(getResources().getString(R.string.explore));
        mDBHelper = new DatabaseHelper(getContext());
        // Inflate the layout for this fragment
        return view;
    }

    @OnClick(R.id.subscribe_layout)
    void onSubscriptionClick(){
        Intent intent = new Intent(mActivity, SubscriptionActivity.class);
        startActivity(intent);

    }
    @OnClick(R.id.request_layout)
    void onRequestClick(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.movie_request_dialog, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        TextInputEditText nameEt, emailEt, movieNameEt, messageEt;
        TextView title;
        Button sendButton, closeButton;
        movieNameEt = view.findViewById(R.id.movieNameEditText);
        messageEt   = view.findViewById(R.id.messageEditText);
        sendButton  = view.findViewById(R.id.sendButton);
        closeButton = view.findViewById(R.id.closeButton);
        title       = view.findViewById(R.id.title);

        if (!mActivity.isDark)
            title.setTextColor(getResources().getColor(R.color.colorPrimary));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = mDBHelper.getUserData();
                String movieName = movieNameEt.getText().toString().trim();
                String message   = messageEt.getText().toString().trim();
                if (!movieName.isEmpty() && !message.isEmpty()){
                    Retrofit retrofit = RetrofitClient.getRetrofitInstance();
                    MovieRequestApi api = retrofit.create(MovieRequestApi.class);
                    Call<ResponseBody> call = api.submitRequest(Config.API_KEY, user.getName(), user.getEmail(), movieName, message);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.code() == 200) {
                                new ToastMsg(getContext()).toastIconSuccess("Request submitted");
                            } else {
                                new ToastMsg(getContext()).toastIconError(getResources().getString(R.string.something_went_text));
                            }
                            dialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            new ToastMsg(mActivity.getApplicationContext()).toastIconError(getResources().getString(R.string.something_went_text));
                            dialog.dismiss();
                        }
                    });
                }else{
                    new ToastMsg(getContext()).toastIconError("Please fill all require information");
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();


    }
    @OnClick(R.id.facebook_group)
    void onFbGroupClick(){
        String urlString = "https://www.facebook.com/groups/251155293295096";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    @OnClick(R.id.feedback_layout)
    void onFeedBackClick(){
        try {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }
    private Intent rateIntentForUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getActivity().getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21) {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        } else {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }
}