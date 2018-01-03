package Helpers;

import java.util.List;

import models.api_models.NearestBinRequest;
import models.api_models.RedeemRequest;
import models.api_models.RewardsStatusRequest;
import models.app_models.Bins;
import models.app_models.NearestBin;
import models.api_models.SignUp;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Anuda on 7/14/17.
 */

public interface GarbageBinService {

    @POST("new-user")
    Call<Void> signUp(@Body SignUp signUp);


    @POST("nearest-bin")
    Call<NearestBin>nearestBinRequest(@Body NearestBinRequest nearestBinRequest);

    @GET("all-bins")
    Call<List<Bins>>bins();


    @POST("rewards-status")
    Call<Integer> redeemStatus(@Body RewardsStatusRequest rewardsStatusRequest);

    @POST("redeem")
    Call<Void> redeem(@Body RedeemRequest redeemRequest);



}
