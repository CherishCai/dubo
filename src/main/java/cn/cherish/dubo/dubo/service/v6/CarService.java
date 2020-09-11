package cn.cherish.dubo.dubo.service.v6;

import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.v6.DuboUtilsV6;
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
public class CarService extends AbstractService {

    public volatile String type = "car";
    public volatile String name = "赛车";

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
        CarResult history = DuboUtilsV6.getHistory();

        long start = System.currentTimeMillis();
        dealHistory(history);
        long end = System.currentTimeMillis();

        log.info("deal {} in cache use {}ms", type, (end - start));
    }

    public void dealCacheMock() {
        CarResult history = DuboUtilsV6.getHistoryMock();

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

    /**
     *
     * @param termsCacheWithBigOdd 从大到小的数据
     */
    @Override
    protected void afterDealHistory(List<Term> termsCacheWithBigOdd) {
        if (CollectionUtils.isEmpty(termsCacheWithBigOdd)) {
            return;
        }
        log.info("afterDealHistory, TermNum={}", termsCacheWithBigOdd.get(0).getTermNum());

        dataMajor(termsCacheWithBigOdd, 9, true);
        dataMajor(termsCacheWithBigOdd, 9, false);
    }

    /**
     *
     * @param sortedTerms 从小到大的数据
     */
    @Override
    protected void afterDealSortHistory(List<Term> sortedTerms) {

    }

    public static void main(String[] args) {
        new CarService().dealCacheMock();
    }


}
