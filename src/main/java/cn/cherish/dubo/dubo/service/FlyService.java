package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.DuboUtils;
import cn.cherish.dubo.dubo.util.MailUtils;
import cn.cherish.dubo.dubo.util.SMSUtils;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
    private static int tipNum1 = 10;
    private static int tipNum2 = 12;

    {
        mailSubject = "飞艇";
        mailContent = "";
        url = "http://ft.zzj321.com";
        evenOddTickNum = 7;
    }

    @Override
    protected void afterDealHistory(List<Term> terms) {
        if (CollectionUtils.isEmpty(terms)) {
            return;
        }

        // 单双 ❌ 五次
        evenOddTick(terms, 1, evenOddTickNum);
        bigSmallTick(terms, 1, evenOddTickNum);

        evenOddTick(terms, 2, 7);
        bigSmallTick(terms, 2, 7);

        bigOdd(terms);
        evenBig(terms);
        oddBig(terms);
        smallOdd(terms);

        /*if (needSendSMS) {
            try {
                SMSUtils.send(SMSUtils.phones2, "F"+ SMSUtils.randomCode());
            } catch (Exception e) {
                log.error("send sms error", e);
                try {
                    SMSUtils.send(SMSUtils.phones2, "F"+ SMSUtils.randomCode());
                } catch (Exception e1) {
                    log.error("send sms error2", e);
                }
            }

            try {
                boolean mail = MailUtils.htmlMail(MailUtils.targets, mailSubject, mailContent);
                if (!mail) {
                    log.warn("send mail fail");
                }
            } catch (Exception e) {
                log.error("send mail error", e);
            }

        }*/
        log.info("car needSendSMS:{}", needSendSMS);
        needSendSMS = false;
    }

    private void oddBig(List<Term> terms) {
        List<Term> list = terms;
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

                    Integer[] termDataArr1 = term1.getTermDataArr();
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
                        String url = PRE_URL + "/fly/v4OddBig.html";
                        mailContent = "<a style='font-size:36px' href='" + url + "'>飞艇单大小</a>";
                    }
                }
            }
        }
    }

    private void evenBig(List<Term> terms) {
        List<Term> list = terms;
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

                    Integer[] termDataArr1 = term1.getTermDataArr();
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
                        String url = PRE_URL + "/fly/evenBig.html";
                        mailContent = "<a style='font-size:36px' href='" + url + "'>飞艇双大小</a>";
                    }
                }
            }
        }
    }

    private void bigOdd(List<Term> terms) {
        List<Term> list = terms;
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

                    Integer[] termDataArr1 = term1.getTermDataArr();
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
                        String url = PRE_URL + "/fly/bigOdd.html";
                        mailContent = "<a style='font-size:36px' href='" + url + "'>飞艇大单双</a>";
                    }
                }
            }
        }
    }

    private void smallOdd(List<Term> terms) {
        List<Term> list = terms;
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

                    Integer[] termDataArr1 = term1.getTermDataArr();
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
                        String url = PRE_URL + "/fly/smallOdd.html";
                        mailContent = "<a style='font-size:36px' href='" + url + "'>飞艇小单双</a>";
                    }
                }
            }
        }
    }

}
