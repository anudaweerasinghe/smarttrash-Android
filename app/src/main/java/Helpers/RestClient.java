package Helpers;

import retrofit2.Retrofit;

/**
 * Created by Anuda on 7/14/17.
 */

public class RestClient {

   static Retrofit RequestObject = new Retrofit.Builder()
            .baseUrl("http://localhost:8080/")
            .build();

    static public  GarbageBinService  garbageBinService= RequestObject.create(GarbageBinService.class);




}
