package Helpers;

import java.util.List;

import models.api_models.NearestBinRequest;
import models.app_models.Bins;
import models.app_models.NearestBin;
import models.api_models.SignUp;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Anuda on 7/14/17.
 */

public interface GarbageBinService {

    @POST("new-user")
    Call<SignUp> signUp(@Body SignUp signUp);

    @GET("verify")
    Call<Void> logIn(@Query("phone") String phone,@Query("password") String password);

    @POST("nearest-bin")
    Call<NearestBin>nearestBinRequest(@Body NearestBinRequest nearestBinRequest);

    @GET("all-bins")
    Call<List<Bins>>bins();



}
