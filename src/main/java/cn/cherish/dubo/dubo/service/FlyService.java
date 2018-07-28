package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.DuboUtils;
import cn.cherish.dubo.dubo.util.SMSUtils;
import com.aliyuncs.exceptions.ClientException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 12:18
 */
@Slf4j
@Service
public class FlyService extends AbstractService {

    private static final long defaultAwardTimeInterval = 1000L;

    private static volatile long awardTimeInterval = defaultAwardTimeInterval;
    private static volatile int periodNumber = 0;

    public volatile String type = "fly";

    /**
     * 每十秒
     */
    @Scheduled(fixedDelay = 10 * 1000, initialDelay = 5 * 1000)
    public void dealData() {
        log.info("dealData every 10 sec");
        // 处理全局缓存
        dealCache();
    }


    public void dealCache() {
        DuboUtils.History history = DuboUtils.getFlyHistory(180);

        long start = System.currentTimeMillis();
        dealHistory(history);
        long end = System.currentTimeMillis();

        log.info("deal in cache use {}ms", (end - start));
    }

    private static boolean needSendSMS = false;
    private static int tipNum1 = 8;
    private static int tipNum2 = 12;
    @Override
    protected void afterDealHistory() {
        List<Term> list = this.termsCachev4;
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        bigOdd();
        evenBig();
        oddBig();
        smallOdd();

        if (needSendSMS) {
            try {
                SMSUtils.send(SMSUtils.phones2, "F612");
            } catch (ClientException e) {
                log.error("send sms error", e);
            }

        }
        needSendSMS = false;
    }

    private void oddBig() {
        List<Term> list = this.termsCachev4;
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        int len = list.size();
        int wid = list.get(0).getTermDataArr().length;
        int w = wid / 3;

        for (int r = 0; r < w; r++) {
            int count = 0;
            int lastStage = 0;

            for (int c = 0; c < len-1; c++) {
                Term term = list.get(c);
                Long curTermNum = term.getTermNum();

                Integer[] termDataArr = term.getTermDataArr();
                Integer termVal = termDataArr[r * 3];
                // 单双： 0 双 1单
                Integer odd = termDataArr[r * 3 + 1];
                // 大小： 0 小 1大
                Integer big = termDataArr[r * 3 + 2];

                if (odd > 0) {
                    Term term1 = list.get(c + 1);

                    Integer[] termDataArr1 = term.getTermDataArr();
                    Integer termVal1 = termDataArr1[r * 3];
                    // 单双： 0 双 1单
                    Integer odd1 = termDataArr1[r * 3 + 1];
                    // 大小： 0 小 1大
                    Integer big1 = termDataArr1[r * 3 + 2];

                    if (lastStage == big1) {
                        count++;
                    } else {
                        count = 1;
                    }
                    lastStage = big1;

                    if ((count == tipNum1 || count == tipNum2)
                        && newestNumStr.endsWith(String.valueOf(curTermNum + 1))) {
                        needSendSMS = true;
                    }
                }
            }
        }
    }

    private void evenBig() {
        List<Term> list = this.termsCachev4;
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        int len = list.size();
        int wid = list.get(0).getTermDataArr().length;
        int w = wid / 3;

        for (int r = 0; r < w; r++) {
            int count = 0;
            int lastStage = 0;

            for (int c = 0; c < len-1; c++) {
                Term term = list.get(c);
                Long curTermNum = term.getTermNum();

                Integer[] termDataArr = term.getTermDataArr();
                Integer termVal = termDataArr[r * 3];
                // 单双： 0 双 1单
                Integer odd = termDataArr[r * 3 + 1];
                // 大小： 0 小 1大
                Integer big = termDataArr[r * 3 + 2];

                if (odd == 0) {
                    Term term1 = list.get(c + 1);

                    Integer[] termDataArr1 = term.getTermDataArr();
                    Integer termVal1 = termDataArr1[r * 3];
                    // 单双： 0 双 1单
                    Integer odd1 = termDataArr1[r * 3 + 1];
                    // 大小： 0 小 1大
                    Integer big1 = termDataArr1[r * 3 + 2];

                    if (lastStage == big1) {
                        count++;
                    } else {
                        count = 1;
                    }
                    lastStage = big1;

                    if ((count == tipNum1 || count == tipNum2)
                        && newestNumStr.endsWith(String.valueOf(curTermNum + 1))) {
                        needSendSMS = true;
                    }
                }
            }
        }
    }

    private void bigOdd() {
        List<Term> list = this.termsCachev4;
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        int len = list.size();
        int wid = list.get(0).getTermDataArr().length;
        int w = wid / 3;

        for (int r = 0; r < w; r++) {
            int count = 0;
            int lastStage = 0;

            for (int c = 0; c < len-1; c++) {
                Term term = list.get(c);
                Long curTermNum = term.getTermNum();

                Integer[] termDataArr = term.getTermDataArr();
                Integer termVal = termDataArr[r * 3];
                // 单双： 0 双 1单
                Integer odd = termDataArr[r * 3 + 1];
                // 大小： 0 小 1大
                Integer big = termDataArr[r * 3 + 2];

                // 处理大数
                if (big > 0) {
                    Term term1 = list.get(c + 1);

                    Integer[] termDataArr1 = term.getTermDataArr();
                    Integer termVal1 = termDataArr1[r * 3];
                    // 单双： 0 双 1单
                    Integer odd1 = termDataArr1[r * 3 + 1];
                    // 大小： 0 小 1大
                    Integer big1 = termDataArr1[r * 3 + 2];

                    if (lastStage == odd1) {
                        count++;
                    } else {
                        count = 1;
                    }
                    lastStage = odd1;

                    if ((count == tipNum1 || count == tipNum2)
                        && newestNumStr.endsWith(String.valueOf(curTermNum + 1))) {
                        needSendSMS = true;
                    }
                }
            }
        }
    }

    private void smallOdd() {
        List<Term> list = this.termsCachev4;
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        int len = list.size();
        int wid = list.get(0).getTermDataArr().length;
        int w = wid / 3;

        for (int r = 0; r < w; r++) {
            int count = 0;
            int lastStage = 0;

            for (int c = 0; c < len-1; c++) {
                Term term = list.get(c);
                Long curTermNum = term.getTermNum();

                Integer[] termDataArr = term.getTermDataArr();
                Integer termVal = termDataArr[r * 3];
                // 单双： 0 双 1单
                Integer odd = termDataArr[r * 3 + 1];
                // 大小： 0 小 1大
                Integer big = termDataArr[r * 3 + 2];

                // 处理大数
                if (big == 0) {
                    Term term1 = list.get(c + 1);

                    Integer[] termDataArr1 = term.getTermDataArr();
                    Integer termVal1 = termDataArr1[r * 3];
                    // 单双： 0 双 1单
                    Integer odd1 = termDataArr1[r * 3 + 1];
                    // 大小： 0 小 1大
                    Integer big1 = termDataArr1[r * 3 + 2];

                    if (lastStage == odd1) {
                        count++;
                    } else {
                        count = 1;
                    }
                    lastStage = odd1;

                    if ((count == tipNum1 || count == tipNum2)
                        && newestNumStr.endsWith(String.valueOf(curTermNum + 1))) {
                        needSendSMS = true;
                    }
                }
            }
        }
    }

}
