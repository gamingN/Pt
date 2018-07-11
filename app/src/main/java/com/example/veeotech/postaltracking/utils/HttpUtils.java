package com.example.veeotech.postaltracking.utils;

import android.util.Log;

import com.example.veeotech.postaltracking.server.IServer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by VeeoTech on 17/4/2018.
 */

public class HttpUtils {

//    public static String DB_URL="http://192.168.0.96:10000/postal_tracking/";

    public static final String DB_URL="http://easy-logistics.com.hk/postal/api/";

    public static IServer getIserver(){
        Retrofit retrofit = new Retrofit.Builder()
                .client(HttpUtils.getClient())
                .baseUrl(HttpUtils.DB_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        IServer iService = retrofit.create(IServer.class);
        return iService;
    }


    public static OkHttpClient getClient() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        //设定日志级别
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        builder.addInterceptor(httpLoggingInterceptor);
        //错误重连
        builder.retryOnConnectionFailure(true);
        return builder.build();
    }

}
