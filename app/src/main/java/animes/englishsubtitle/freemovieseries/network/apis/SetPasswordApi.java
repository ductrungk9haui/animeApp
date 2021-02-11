package animes.englishsubtitle.freemovieseries.network.apis;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import animes.englishsubtitle.freemovieseries.network.model.ResponseStatus;

public interface SetPasswordApi {
    @FormUrlEncoded
    @POST("set_password")
    Call<ResponseStatus> setPassword(@Header("API-KEY") String apiKey,
                                     @Field("user_id") String userId,
                                     @Field("password") String password,
                                     @Field("firebase_auth_uid") String firebaseUID);
}
