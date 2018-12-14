package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.DuboUtils;
import cn.cherish.dubo.dubo.util.MailUtils;
import cn.cherish.dubo.dubo.util.SMSUtils;
import com.aliyuncs.exceptions.ClientException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
public class DuboService extends AbstractService {

//    private static final ScheduledExecutorService sch = new ScheduledThreadPoolExecutor(2);
//    private static final ScheduledExecutorService schInLoop = new ScheduledThreadPoolExecutor(2);
    private static final long defaultAwardTimeInterval = 1000L;

    private static volatile long awardTimeInterval = defaultAwardTimeInterval;
    private static volatile int periodNumber = 0;

    private static volatile int largeTermNumInDB = 0;

   /* private Callable<Boolean> callable = () -> {
        DuboService.awardTimeInterval = DuboService.defaultAwardTimeInterval;

        DuboUtils.Current current = DuboUtils.getCurrent();
        if (current == null) {
            log.error("callable get DuboUtils.Current is null");
            return false;
        }

        int firstPeriod = current.getFirstPeriod();

        int curPeriodNumber = current.getCurrent().getPeriodNumber();
        long nextAwardTimeInterval = current.getNext().getAwardTimeInterval();
        log.info("callable curPeriodNumber:{},nextAwardTimeInterval:{}", curPeriodNumber, nextAwardTimeInterval);

        if (nextAwardTimeInterval > 0) {
            // 需要等待一段时间再去获取
            DuboService.awardTimeInterval = nextAwardTimeInterval;
            DuboService.periodNumber = curPeriodNumber;
            return true;
        }

        // 此时需要轮询获取数据 直到 periodNumber 被改变
        boolean loop = true;
        while (loop) {
            ScheduledFuture<Boolean> scheduledFuture = schInLoop.schedule(() -> {
                DuboUtils.Current currentLoop = DuboUtils.getCurrent();
                if (currentLoop == null) {
                    log.error("callable:loop get DuboUtils.Current is null");
                    return false;
                }

                DuboUtils.Current.Msg loopMsg = currentLoop.getCurrent();
                log.info("callable:loop get currentLoop:{}", currentLoop);

                int loopPeriodNumber = loopMsg.getPeriodNumber();

                return DuboService.periodNumber != loopPeriodNumber;

            }, 1, TimeUnit.SECONDS);

            try {
                Boolean result = scheduledFuture.get();
                log.info("callable:loop result:{}", result);
                if (result == null) {
                    result = false;
                }
                loop = !result;
            } catch (Exception e) {
                log.error("callable:sch error", e);
                loop = true;
            }
        }// end while

        DuboUtils.Current curResult = DuboUtils.getCurrent();
        if (curResult == null) {
            log.error("callable:last get DuboUtils.Current is null");
            return true;
        }

        DuboService.periodNumber = curResult.getCurrent().getPeriodNumber();
        String awardNumbers = curResult.getCurrent().getAwardNumbers();
        log.info("callable awardNumbers:{}", awardNumbers);

        TimeUnit.SECONDS.sleep(5);

//        doSaveTerm();

        return false;
    };


//    @PostConstruct
    *//* public for test*//* public void init() {
        // 寻找最近的一个
       *//* Term term = termService.findLargeTerm();
        if (term != null) {
            largeTermNumInDB = term.getTermNum();
        }*//*

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            schInLoop.shutdown();
            sch.shutdown();
        }));

        new Thread(() -> {
            while (true) {
                try {
                    ScheduledFuture<Boolean> future = sch.schedule(callable, awardTimeInterval, TimeUnit.MILLISECONDS);
                    future.get();
                } catch (Exception ignore) {}
                log.info("init:sch periodNumber:{},awardTimeInterval:{}", periodNumber, awardTimeInterval);
            }
        }).start();

    }*/

    public volatile String type = "car";

    /**
     * 每十秒
     */
    @Scheduled(fixedDelay = 10 * 1000, initialDelay = 5 * 1000)
    public void dealData() {
        log.info("dealData every 10 sec");

        // 避免数据遗漏
//        checkDBData();

        // 处理全局缓存
        dealCache();
    }

    public void dealCache() {
        DuboUtils.History history = DuboUtils.getHistory(180);

        long start = System.currentTimeMillis();
        dealHistory(history);
        long end = System.currentTimeMillis();

        log.info("deal in cache use {}ms", (end - start));
    }

    private static boolean needSendSMS = false;
    private static int tipNum1 = 10;
    private static int tipNum2 = 12;

    {
        mailSubject = "赛车";
        mailContent = "";

        url = "http://pk.qilin68.com";
        evenOddTickNum = 8;
    }

    @Override
    protected void afterDealHistory(List<Term> terms) {
        if (CollectionUtils.isEmpty(terms)) {
            return;
        }

        // 单双 ❌ 五次
        evenOddTick(terms, 1, evenOddTickNum);
        bigSmallTick(terms, 1, evenOddTickNum);

        evenOddTick(terms, 2, 5);
        bigSmallTick(terms, 2, 5);


        bigOdd(terms);
        evenBig(terms);
        oddBig(terms);
        smallOdd(terms);

        /*if (needSendSMS) {
            try {
                SMSUtils.send(SMSUtils.phones2, "C" + SMSUtils.randomCode());
            } catch (Exception e) {
                log.error("send sms error", e);
                try {
                    SMSUtils.send(SMSUtils.phones2, "C" + SMSUtils.randomCode());
                } catch (ClientException e1) {
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
        list = list.stream().sorted(Comparator.comparingLong(Term::getTermNum)).collect(Collectors.toList());

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
                    log.info("oddBig r:{},c:{},curTermNum:{},count:{}", r, c, curTermNum, count);
                    lastStage = big1;

                    if ((count == tipNum1 || count == tipNum2)
                        && newestNumStr.endsWith(String.valueOf(curTermNum + 1))) {
                        needSendSMS = true;
                        String url = PRE_URL + "/car/v4OddBig.html";
                        mailContent = "<a style='font-size:36px' href='" + url + "'>赛车单大小</a>";
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
                    log.info("evenBig r:{},c:{},curTermNum:{},count:{}", r, c, curTermNum, count);

                    lastStage = big1;

                    if ((count == tipNum1 || count == tipNum2)
                        && newestNumStr.endsWith(String.valueOf(curTermNum + 1))) {
                        needSendSMS = true;
                        String url = PRE_URL + "/car/evenBig.html";
                        mailContent = "<a style='font-size:36px' href='" + url + "'>赛车双大小</a>";
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
                    log.info("bigOdd r:{},c:{},curTermNum:{},count:{}", r, c, curTermNum, count);

                    lastStage = odd1;

                    if ((count == tipNum1 || count == tipNum2)
                        && newestNumStr.endsWith(String.valueOf(curTermNum + 1))) {
                        needSendSMS = true;
                        String url = PRE_URL + "/car/bigOdd.html";
                        mailContent = "<a style='font-size:36px' href='" + url + "'>赛车大单双</a>";
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
                    log.info("smallOdd r:{},c:{},curTermNum:{},count:{}", r, c, curTermNum, count);

                    lastStage = odd1;

                    if ((count == tipNum1 || count == tipNum2)
                        && newestNumStr.endsWith(String.valueOf(curTermNum + 1))) {
                        needSendSMS = true;
                        String url = PRE_URL + "/car/smallOdd.html";
                        mailContent = "<a style='font-size:36px' href='" + url + "'>赛车小单双</a>";
                    }
                }
            }
        }
    }

}
