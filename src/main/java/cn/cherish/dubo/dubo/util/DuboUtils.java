package cn.cherish.dubo.dubo.util;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 1:10
 */
@Slf4j
public class DuboUtils {
    private DuboUtils(){}

    public static final String HOST = "http://kk.jsk412.vip";
    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    public static History getHistory(int count) {
        if (count < 1) {
            count = 1;
        }

        String t = System.currentTimeMillis() + "" + random.nextInt(9999);

        String url = HOST + "/pk10/getHistoryData.do?count=%s&t=%s";

        url = String.format(url, count, t);
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
            return JSON.parseObject(body.string(), History.class);
        } catch (Exception e) {
            log.error("getHistory error, url:{}", url, e);
        }
        return null;
    }

    public static History getFlyHistory(int count) {
        if (count < 1) {
            count = 1;
        }

        String t = System.currentTimeMillis() + "" + random.nextInt(9999);

        String url = HOST+"/xyft/getHistoryData.do?count=%s&t=%s";

        url = String.format(url, count, t);

        log.info("getFlyHistory url:{}", url);

        try {
            Request request = OkHttpClientUtils.getReq(url);
            OkHttpClient okHttpClient = OkHttpClientUtils.defaultClient();

            Response response = okHttpClient.newCall(request).execute();
            if (response == null) {
                log.warn("getFlyHistory response is null, url:{}", url);
                return null;
            }
            ResponseBody body = response.body();
            if (body == null) {
                log.warn("getFlyHistory responseBody is null, url:{}", url);
                return null;
            }
            return JSON.parseObject(body.string(), History.class);
        } catch (Exception e) {
            log.error("getFlyHistory error, url:{}", url, e);
        }
        return null;
    }

    public static Current getCurrent() {

        String url = HOST + "/ppk/getdata.php?t=" + System.currentTimeMillis() + "" + random.nextInt(9999);
        log.info("getCurrent url:{}", url);
        try {
            Request request = OkHttpClientUtils.getReq(url);
            OkHttpClient okHttpClient = OkHttpClientUtils.defaultClient();

            Response response = okHttpClient.newCall(request).execute();
            if (response == null) {
                log.warn("getCurrent response is null, url:{}", url);
                return null;
            }
            ResponseBody body = response.body();
            if (body == null) {
                log.warn("getCurrent responseBody is null, url:{}", url);
                return null;
            }
            return JSON.parseObject(body.string(), Current.class);
        } catch (Exception e) {
            log.error("getCurrent error, url:{}", url, e);
        }
        return null;
    }

    @NoArgsConstructor
    @Data
    public static class Current {

        /**
         * time : 1524713815958
         * firstPeriod : 678555
         * apiVersion : 1
         * current : {"awardTime":"2018-04-26 11:32:00","periodNumber":678585,"fullPeriodNumber":678585,"periodNumberStr":null,"awardTimeInterval":0,"awardNumbers":"6,9,4,8,7,3,5,2,10,1","delayTimeInterval":null,"pan":"1","isEnd":null,"nextMinuteInterval":null}
         * next : {"awardTime":"2018-04-26 11:37:00","periodNumber":678586,"fullPeriodNumber":0,"periodNumberStr":678586,"awardTimeInterval":4042,"awardNumbers":null,"delayTimeInterval":null,"pan":null,"isEnd":null,"nextMinuteInterval":null}
         */

        private Long time;
        private int firstPeriod;
        private Integer apiVersion;
        private Msg current;
        private Msg next;

        @NoArgsConstructor
        @Data
        public static class Msg {
            /**
             * awardTime : 2018-04-26 11:32:00
             * periodNumber : 678585
             * fullPeriodNumber : 678585
             * periodNumberStr : null
             * awardTimeInterval : 0
             * awardNumbers : 6,9,4,8,7,3,5,2,10,1
             * delayTimeInterval : null
             * pan : 1
             * isEnd : null
             * nextMinuteInterval : null
             */

            private String awardTime;
            private int periodNumber;
            private int fullPeriodNumber;
            private String periodNumberStr;
            private long awardTimeInterval;
            private String awardNumbers;
            private long delayTimeInterval;
            private String pan;
            private Object isEnd;
            private long nextMinuteInterval;
        }

    }
    
    @NoArgsConstructor
    @Data
    public static class History {

        /**
         * success : true
         * code : null
         * msg : null
         * rows : [{"id":0,"betEndTime":null,"termNum":"678555","lotteryNum":"03081002090106070405","lotteryTime":"2018-04-25 23:57:30","gameId":50,"n1":3,"n2":8,"n3":10,"n4":2,"n5":9,"n6":1,"n7":6,"n8":7,"n9":4,"n10":5,"n11":null,"n12":null,"n13":null,"n14":null,"n15":null,"n16":null,"n17":null,"n18":null,"n19":null,"n20":null,"n21":null,"lotteryDate":"2018-04-25 00:00:00","lotteryDateStr":"2018-04-25","termNumStr":""}]
         */

        private boolean success;
        private Object code;
        private String msg;
        private List<RowsBean> rows;

        @NoArgsConstructor
        @Data
        public static class RowsBean {
            /**
             * id : 0
             * betEndTime : null
             * termNum : 678555
             * lotteryNum : 03081002090106070405
             * lotteryTime : 2018-04-25 23:57:30
             * gameId : 50
             * n1 : 3
             * n2 : 8
             * n3 : 10
             * n4 : 2
             * n5 : 9
             * n6 : 1
             * n7 : 6
             * n8 : 7
             * n9 : 4
             * n10 : 5
             * n11 : null
             * n12 : null
             * n13 : null
             * n14 : null
             * n15 : null
             * n16 : null
             * n17 : null
             * n18 : null
             * n19 : null
             * n20 : null
             * n21 : null
             * lotteryDate : 2018-04-25 00:00:00
             * lotteryDateStr : 2018-04-25
             * termNumStr : 
             */

            private Integer id;
            private String betEndTime;
            private String termNum;
            private String lotteryNum;
            private String lotteryTime;
            private Integer gameId;
            private Integer n1;
            private Integer n2;
            private Integer n3;
            private Integer n4;
            private Integer n5;
            private Integer n6;
            private Integer n7;
            private Integer n8;
            private Integer n9;
            private Integer n10;
            private Integer n11;
            private Integer n12;
            private Integer n13;
            private Integer n14;
            private Integer n15;
            private Integer n16;
            private Integer n17;
            private Integer n18;
            private Integer n19;
            private Integer n20;
            private Integer n21;
            private String lotteryDate;
            private String lotteryDateStr;
            private String termNumStr;
        }
    }


}
