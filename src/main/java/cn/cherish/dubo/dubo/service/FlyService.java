package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.DuboUtils;
import java.util.List;
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

    public volatile String type = "fly";
    public volatile String name = "飞艇";

    /**
     * 每十秒
     */
    @Scheduled(fixedDelay = 10 * 1000, initialDelay = 5 * 1000)
    public void dealData() {
        log.info("dealData {} every 10 sec", type);
        // 处理全局缓存
        dealCache();
    }


    public void dealCache() {
        DuboUtils.History history = DuboUtils.getFlyHistory(180);

        long start = System.currentTimeMillis();
        dealHistory(history);
        long end = System.currentTimeMillis();

        log.info("deal {} in cache use {}ms", type, (end - start));
    }

    @Override
    protected void afterDealHistory(List<Term> terms) {
        if (CollectionUtils.isEmpty(terms)) {
            return;
        }

    }

}
