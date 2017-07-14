package Helpers;

import retrofit2.Retrofit;

/**
 * Created by Anuda on 7/14/17.
 */

public class RestClient {

   static Retrofit RequestObject = new Retrofit.Builder()
            .baseUrl("http://188.166.230.183/IdeaTrash")
            .build();

    static public  GarbageBinService  garbageBinService= RequestObject.create(GarbageBinService.class);




}
