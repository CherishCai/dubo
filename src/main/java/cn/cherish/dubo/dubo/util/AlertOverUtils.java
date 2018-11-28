package cn.cherish.dubo.dubo.util;

import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

/**
 * @author caihongwen@u51.com
 * @date 2018/11/28 22:48
 */
public class AlertOverUtils {

    private static final String URI = "https://api.alertover.com/v1/alert";
    private static final String SOURCE = "s-552ac647-ae21-4bdf-b200-883eadc0";
    private static final String RECEIVER = "g-3af7b687-93ef-465b-907c-be1db046";

    public static void main(String[] args) throws IOException {
        boolean send = send("gg", "55\r\n ggg","https://cn.bing.com/");
        System.out.println("send = " + send);
    }

    public static boolean send(String title, String content, String ctxUtl) throws IOException {
        ctxUtl = StringUtils.defaultString(ctxUtl);
        RequestBody formBody = new FormBody.Builder()
            .add("source", SOURCE)
            .add("receiver", RECEIVER)
            .add("title", title)
            .add("content", content)
            .add("url", ctxUtl)
            .build();

        Request request = OkHttpClientUtils.postReq(URI, formBody);
        OkHttpClient okHttpClient = OkHttpClientUtils.defaultClient();

        Response response = okHttpClient.newCall(request).execute();
        if (response == null) {
            return false;
        }
        String body = response.body().string();
        System.out.println("body = " + body);
        return true;

    }

}
