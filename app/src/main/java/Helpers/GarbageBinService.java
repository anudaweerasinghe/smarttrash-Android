package Helpers;

import java.util.List;

import models.api_models.NearestBinRequest;
import models.api_models.PicVerifyRequest;
import models.api_models.QRVerifyRequest;
import models.api_models.RewardsStatusRequest;
import models.app_models.Bins;
import models.app_models.NearestBin;
import models.api_models.SignUp;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by Anuda on 7/14/17.
 */

public interface GarbageBinService {

    @POST("garbageapi/new-user")
    Call<Void> signUp(@Body SignUp signUp);


    @POST("garbageapi/nearest-bin")
    Call<NearestBin>nearestBinRequest(@Body NearestBinRequest nearestBinRequest);

    @GET("garbageapi/all-bins")
    Call<List<Bins>>bins();


    @POST("garbageapi/rewards-status")
    Call<Integer> redeemStatus(@Body RewardsStatusRequest rewardsStatusRequest);

    @POST("redeem/code-verify")
    Call<ResponseBody> codeVerify(@Body QRVerifyRequest QRVerifyRequest);

//    @Multipart
//    @POST("redeem/upload")
//    Call<Void> uploadPic(@Part MultipartBody.Part image, @Query("location") String location);

    @POST("redeem/pic-verify")
    Call<ResponseBody> verifyPic(@Body PicVerifyRequest picVerifyRequest);


}
