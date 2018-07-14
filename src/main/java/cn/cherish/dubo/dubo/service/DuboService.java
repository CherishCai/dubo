package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.util.DuboUtils;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 12:18
 */
@Slf4j
@Service
public class DuboService extends AbstractService {

    private static final ScheduledExecutorService sch = new ScheduledThreadPoolExecutor(2);
    private static final ScheduledExecutorService schInLoop = new ScheduledThreadPoolExecutor(2);
    private static final long defaultAwardTimeInterval = 1000L;

    private static volatile long awardTimeInterval = defaultAwardTimeInterval;
    private static volatile int periodNumber = 0;

    private static volatile int largeTermNumInDB = 0;

    private Callable<Boolean> callable = () -> {
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
    /* public for test*/ public void init() {
        // 寻找最近的一个
       /* Term term = termService.findLargeTerm();
        if (term != null) {
            largeTermNumInDB = term.getTermNum();
        }*/

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

    }

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


}
