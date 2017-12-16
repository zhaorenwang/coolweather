package com.example.r.coolweather.until;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/12/14 0014.
 */
//和服务器 交互的工具类，利用框架OkHttp，现在发起一条HTTP请求只需要调用sendOkHttpRequest（）方法，传入请求地址并注册一个回调函数来处理服务器相应既可以了


public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);

    }
}
