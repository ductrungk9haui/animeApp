package tvseries.koreandramaengsub.freemovieapp.network.apis;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import tvseries.koreandramaengsub.freemovieapp.models.home_content.Video;

public interface DownApi {

    @GET("down")
    Call<List<Video>> getDown(@Header("API-KEY") String apiKey,
                                @Query("page") int page);
}
