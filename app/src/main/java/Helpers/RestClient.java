package Helpers;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Anuda on 7/14/17.
 */

public class RestClient {

   static Retrofit RequestObject = new Retrofit.Builder()
            .baseUrl("http://128.199.229.198:8080/garbageback/garbageapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    static public  GarbageBinService  garbageBinService= RequestObject.create(GarbageBinService.class);




}
