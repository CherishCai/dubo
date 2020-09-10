package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.DuboUtils;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
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
//@Service
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
    protected String getType() {
        return type;
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    protected void afterDealHistory(List<Term> sortedTerms) {
        if (CollectionUtils.isEmpty(sortedTerms)) {
            return;
        }

        Set<Integer> data67890 = Sets.newHashSet(6, 7, 8, 9, 10);
        Set<Integer> data24 = Sets.newHashSet(2, 4);
        Set<Integer> data680 = Sets.newHashSet(6, 8, 10);

        Set<Integer> data12345 = Sets.newHashSet(1, 2, 3, 4, 5);
        Set<Integer> data79 = Sets.newHashSet(7, 9);
        Set<Integer> data135 = Sets.newHashSet(1, 3, 5);


        dataMajor(sortedTerms, data24CountFailAlert, data680, data24, data67890);

        dataMajor(sortedTerms, data24CountFailAlert, data135, data24, data12345);

        dataMajor(sortedTerms, data24CountFailAlert, data680, data79, data67890);
    }

}
