package ottu.payment.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstance {


    public static native String getLink();
    public static native String getLinkPg();

    private static Retrofit retrofit;
    private static Retrofit retrofit1;
    private static final String BASE_URL = "https://ksa.ottu.dev/b/";
    private static final String BASE_URLPg = "https://pg.ottu.dev/pg/";

    public static GetDataService getRetrofitInstance() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(getLink())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
            return retrofit.create(GetDataService.class);
        }
        return retrofit.create(GetDataService.class);
    }

    public static GetDataService getRetrofitInstancePg() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        if (retrofit1 == null) {
            retrofit1 = new Retrofit.Builder()
                    .baseUrl(getLinkPg())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
            return retrofit1.create(GetDataService.class);
        }
        return retrofit1.create(GetDataService.class);
    }


}