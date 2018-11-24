package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.DuboUtils;
import cn.cherish.dubo.dubo.util.MailUtils;
import cn.cherish.dubo.dubo.util.SMSUtils;
import com.aliyuncs.exceptions.ClientException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
    private static int tipNum1 = 10;
    private static int tipNum2 = 12;

    private static String mailSubject = "飞艇";
    private static String mailContent = "";
    @Override
    protected void afterDealHistory(List<Term> terms) {
        if (CollectionUtils.isEmpty(terms)) {
            return;
        }

        // 单双 ❌ 五次
        evenOddTick(terms);
        bigSmallTick(terms);

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

    private static final int EVEN_ODD_TICK_NUM = 5;
    /**
     * 单双 ❌ 五次
     */
    private void evenOddTick(List<Term> terms) {
        List<Term> list = terms;
        if (CollectionUtils.isEmpty(list)) {
            return;
        }


        int size = list.size();
        Term term = list.get(size - 2);
        Long termNum = term.getTermNum();

        int len = term.getTermDataArr().length / 3;
        boolean[][] evenOddBool = new boolean[EVEN_ODD_TICK_NUM][len];

        for (int k = 0; k < EVEN_ODD_TICK_NUM; k++) {
            int cur = size - 1 - EVEN_ODD_TICK_NUM + k;

            Term curTerm = list.get(cur);
            Integer[] curTermDataArr = curTerm.getTermDataArr();

            Term nextTerm = list.get(cur + 1);
            Integer[] nextTermDataArr = nextTerm.getTermDataArr();

            for (int i = 0; i < len; i++) {
                Integer curI = curTermDataArr[i*3];

                // 寻找到下一列中与当前列相等的值，取得下一列的与前一列值
                for (int j = 0; j < len; j++) {

                    Integer nextI = nextTermDataArr[j*3];
                    if (Objects.equals(nextI, curI)) {
                        Integer nextEvenOdd = nextTermDataArr[j*3+1];
                        Integer curEvenOdd = curTermDataArr[j*3 + 1];

                        // 赋值给当前位置
                        evenOddBool[k][i] = Objects.equals(curEvenOdd, nextEvenOdd);
                        break;
                    }

                }

            }
        }

        printArr(len, evenOddBool);
        // 计算false
        for (int i = 0; i < len; i++) {
            boolean isFalseTick = false;
            for (int j = 0; j < EVEN_ODD_TICK_NUM; j++) {
                if (evenOddBool[j][i]) {
                    isFalseTick = true;
                    break;
                }
            }

            // 这一列满足全部false
            if (!isFalseTick) {
                // 列号
                int c = i + 1;

                String content = "<p style='font-size:36px'>"
                    + "<a href='http://ft.zzj321.com'>连续打X " + EVEN_ODD_TICK_NUM + "次</a><br>"
                    + "<b style='color:red'>种类：单双</b><br>"
                    + "期号：" + termNum + "<br>"
                    + "列号：" + c + "<br>"
                    + "时间：" + new Date() + "<br>"
                    + "</p>";

                SmsTask smsTask = SmsTask.builder()
                    .phoneNums(SMSUtils.phones2)
                    .smsCode("X" + c + SMSUtils.randomCode())
                    .build();
                smsTaskQueue.add(smsTask);

                MailTask mailTask = MailTask.builder()
                    .mailTargets(MailUtils.targets)
                    .mailSubject(mailSubject)
                    .mailContent(content)
                    .build();
                mailTaskQueue.add(mailTask);
            }
        }
    }

    /**
     * 大小 ❌ 五次
     */
    private void bigSmallTick(List<Term> terms) {
        List<Term> list = terms;
        if (CollectionUtils.isEmpty(list)) {
            return;
        }


        int size = list.size();
        Term term = list.get(size - 2);
        Long termNum = term.getTermNum();

        int len = term.getTermDataArr().length / 3;
        boolean[][] keepBool = new boolean[EVEN_ODD_TICK_NUM][len];

        for (int k = 0; k < EVEN_ODD_TICK_NUM; k++) {
            int cur = size - 1 - EVEN_ODD_TICK_NUM + k;

            Term curTerm = list.get(cur);
            Integer[] curTermDataArr = curTerm.getTermDataArr();

            Term nextTerm = list.get(cur + 1);
            Integer[] nextTermDataArr = nextTerm.getTermDataArr();

            for (int i = 0; i < len; i++) {
                Integer curI = curTermDataArr[i*3];

                // 寻找到下一列中与当前列相等的值，取得下一列的与前一列值
                for (int j = 0; j < len; j++) {

                    Integer nextI = nextTermDataArr[j*3];
                    if (Objects.equals(nextI, curI)) {
                        Integer nextEvenOdd = nextTermDataArr[j*3+2];
                        Integer curEvenOdd = curTermDataArr[j*3 + 2];

                        // 赋值给当前位置
                        keepBool[k][i] = Objects.equals(curEvenOdd, nextEvenOdd);
                        break;
                    }

                }

            }
        }

        printArr(len, keepBool);
        // 计算false
        for (int i = 0; i < len; i++) {
            boolean isFalseTick = false;
            for (int j = 0; j < EVEN_ODD_TICK_NUM; j++) {
                if (keepBool[j][i]) {
                    isFalseTick = true;
                    break;
                }
            }

            // 这一列满足全部false
            if (!isFalseTick) {
                // 列号
                int c = i + 1;

                String content = "<p style='font-size:36px'>"
                    + "<a href='http://ft.zzj321.com'>连续打X " + EVEN_ODD_TICK_NUM + "次</a><br>"
                    + "<b style='color:red'>种类：大小</b><br>"
                    + "期号：" + termNum + "<br>"
                    + "列号：" + c + "<br>"
                    + "时间：" + new Date() + "<br>"
                    + "</p>";

                SmsTask smsTask = SmsTask.builder()
                    .phoneNums(SMSUtils.phones2)
                    .smsCode("X" + c + SMSUtils.randomCode())
                    .build();
                smsTaskQueue.add(smsTask);

                MailTask mailTask = MailTask.builder()
                    .mailTargets(MailUtils.targets)
                    .mailSubject(mailSubject)
                    .mailContent(content)
                    .build();
                mailTaskQueue.add(mailTask);
            }
        }
    }


    private void printArr(int len, boolean[][] evenOddBool) {
        for (int k = 0; k < EVEN_ODD_TICK_NUM; k++) {
            for (int i = 0; i < len; i++) {
                System.out.print((evenOddBool[k][i] ? 1 : 0) + " ");
            }
            System.out.println();
        }
        System.out.println();
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
