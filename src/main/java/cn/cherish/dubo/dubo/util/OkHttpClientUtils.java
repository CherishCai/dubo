package cn.cherish.dubo.dubo.util;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 1:16
 */
public class OkHttpClientUtils {
    private OkHttpClientUtils(){}

    private static final OkHttpClient client;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    static {
        client = new OkHttpClient();
    }

    public static OkHttpClient defaultClient(){
        return client;
    }

    public static Request postReq(String url, RequestBody body) {
        return new Request.Builder()
            .url(url)
            .post(body)
            .build();
    }

    public static Request postJsonReq(String url, String json) {
        RequestBody body = RequestBody.create(JSON, json);
        return postReq(url, body);
    }

    public static Request getReq(String url) {
        return new Request.Builder()
                .url(url)
                .get()
                .build();
    }

}
