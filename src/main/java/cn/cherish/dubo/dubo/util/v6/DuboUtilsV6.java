package cn.cherish.dubo.dubo.util.v6;

import cn.cherish.dubo.dubo.service.v6.CarResult;
import cn.cherish.dubo.dubo.util.OkHttpClientUtils;
import com.alibaba.fastjson.JSON;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 1:10
 */
@Slf4j
public final class DuboUtilsV6 {
    private DuboUtilsV6(){}

    // https://api.apiose122.com/pks/getPksHistoryList.do?lotCode=%s&t=%s
    // http://123.lotterycar.com/api/agindex.php?/cp=jssc&do=getPksHistoryList.do&date=&lotCode=10057
    public static final String HOST = "http://123.lotterycar.com";
    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    private static final String lotCode = random.nextInt(10000, 20000) + "";

    public static CarResult getHistory() {

        String t = System.currentTimeMillis() + "" + random.nextInt(9999);

        String url = HOST + "/api/agindex.php?/cp=jssc&do=getPksHistoryList.do&date=&lotCode=%s";

        url = String.format(url, lotCode);
        log.info("getHistory url:{}", url);

        try {
            Request request = OkHttpClientUtils.getReq(url);
            OkHttpClient okHttpClient = OkHttpClientUtils.defaultClient();

            Response response = okHttpClient.newCall(request).execute();
            if (response == null) {
                log.warn("getHistory response is null, url:{}", url);
                return null;
            }
            ResponseBody body = response.body();
            if (body == null) {
                log.warn("getHistory responseBody is null, url:{}", url);
                return null;
            }
            String bodyString = body.string();
            log.info("getHistory result:{}", bodyString);
            return JSON.parseObject(bodyString, CarResult.class);
        } catch (Exception e) {
            log.error("getHistory error, url:{}", url, e);
        }
        return null;
    }

    public static CarResult getHistoryMock(){
        return CarResult.mock();
    }

}
