package animes.englishsubtitle.freemovieseries.network.apis;


import animes.englishsubtitle.freemovieseries.network.model.PasswordReset;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface PassResetApi {
    @FormUrlEncoded
    @POST("password_reset")
    Call<PasswordReset> resetPassword(@Header("API-KEY") String apiKey,
                                      @Field("email") String email);

}
