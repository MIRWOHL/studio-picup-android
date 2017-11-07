package com.picup.calling.network;

import com.picup.calling.base.PicupApplication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import static com.picup.calling.BuildConfig.LOGCAT_ENABLED;

/**
 * Created by frank.truong on 3/22/2017.
 */

public interface PicupService {
    JsonSerializer<Calendar> calSerializer = new JsonSerializer<Calendar>() {
        @Override
        public JsonElement serialize(Calendar src, Type typeOfSrc, JsonSerializationContext context) {
            return (src != null ? new JsonPrimitive(src.getTime().getTime()) : new JsonPrimitive(0));
        }
    };
    JsonDeserializer<Calendar> calDeserializer = new JsonDeserializer<Calendar>() {
        @Override
        public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Calendar calendar = null;

            if (json != null) {
                calendar = Calendar.getInstance();
                calendar.setTime(new Date(json.getAsLong()));
            }
            return calendar;
        }
    };
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    Gson gson = new GsonBuilder().registerTypeAdapter(Calendar.class, calSerializer)
                                 .registerTypeAdapter(Calendar.class, calDeserializer).create();

    public static final Retrofit retrofit = new Retrofit.Builder().baseUrl(PicupApplication.baseUrl)
                                                                  .addConverterFactory(GsonConverterFactory.create(gson))
                                                                    .client(httpClient.addInterceptor(LOGCAT_ENABLED ? logging.setLevel(HttpLoggingInterceptor.Level.BODY) : logging.setLevel(HttpLoggingInterceptor.Level.NONE)).build())
                                                                  .build();
    @GET("authenticate")
    //Call<AuthenticateInfo> authenticate(@Query(value="emailAddress", encoded = false) String emailAddress, @Query(value="password", encoded = false) String password, @Query("applicationId") String applicationId);
    Call<AuthenticateInfo> authenticate(@QueryMap(encoded = true) Map<String, String> authenticateParams);
    @GET("authenticate")
    Call<AuthenticateInfo> refresh(@QueryMap(encoded = true) Map<String, String> authenticateRefreshParams);
    @GET("picupnumbers")
    Call<LineNumbers> picupNumbers(@Header("Authorization") String token, @QueryMap(encoded = true) Map<String, String> userIdParams);
    @GET("picupnumbers")
    Call<List<String>> lineNumbersForUser(@QueryMap(encoded = true) Map<String, String> userIdParams);
    @GET("picupnumbers/{accountId}")
    Call<List<String>> lineNumbersForAccount(@Path("accountId") String accountId);
    @GET("callhistory/{accountId}")
    Call<CallHistory> callHistory(@Header("Authorization") String token, @Path("accountId") String accountId, @QueryMap(encoded = true) Map<String, String> callHistoryParams);
    @Headers({"Content-Type: application/json", "applicationId: MobileAPI"})
    @POST("calls/{accountId}")
    Call<CallAccess> makeCall(@Header("Authorization") String token, @Path("accountId") String accountId, @Body CallAccessForm callAccessForm);
}
