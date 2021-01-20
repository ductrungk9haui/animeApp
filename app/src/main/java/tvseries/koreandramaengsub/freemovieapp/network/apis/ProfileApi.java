package tvseries.koreandramaengsub.freemovieapp.network.apis;

import android.net.Uri;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import tvseries.koreandramaengsub.freemovieapp.network.model.ResponseStatus;

import retrofit2.Call;
import retrofit2.http.Field;

import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ProfileApi {
    //@FormUrlEncoded
    @Multipart
    @POST("update_profile")
    Call<ResponseStatus> updateProfile(@Header("API-KEY") String apiKey,
                                       @Part("id") RequestBody id,
                                       @Part("name") RequestBody name,
                                       @Part("email") RequestBody email,
                                       @Part("phone") RequestBody phone,
                                       @Part("password") RequestBody password,
                                       @Part("current_password") RequestBody currentPassword,
                                       @Part MultipartBody.Part photo,
                                       @Part("gender") RequestBody gender);

}
