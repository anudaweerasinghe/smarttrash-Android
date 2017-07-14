package Helpers;

import models.app_models.NearestBin;
import models.api_models.SignUp;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Anuda on 7/14/17.
 */

public interface GarbageBinService {

    @POST("garbageapi/new-user")
    Call<SignUp> signUp(@Body SignUp signUp);

    @POST("garbageapi/verify")
    Call<Void> logIn(@Query("phone") String phone,@Query("password") String password);

    @POST("garbageapi/nearest-bin")
    Call<NearestBin>nearestBin(@Body NearestBin nearestBin);

}
