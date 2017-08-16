package com.qiuwenyi.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by QiuWenYi on 2017/8/13.
 * 由于OkHttp的出色封装，这里和服务器进行交互的代码，非常简单，仅仅3行就完成了，现在我们发出一条Http请求
 * 只需要调用sendOkHttpRequest（）方法，传入请求地址，并注册一个回调来处理服务器响应就可以了。
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);

    }
}
