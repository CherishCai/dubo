package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.DuboUtils;
import com.google.common.collect.Sets;
import java.util.Date;
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
        DuboUtils.History history = DuboUtils.getHistory(180);

        long start = System.currentTimeMillis();
        dealHistory(history);
        long end = System.currentTimeMillis();

        log.info("deal {} in cache use {}ms", type, (end - start));
    }

    @Override
    protected void afterDealHistory(List<Term> sortedTerms) {
        if (CollectionUtils.isEmpty(sortedTerms)) {
            return;
        }

        data24(sortedTerms);

    }

    protected int data24CountFailAlert = 6;

    protected void data24(List<Term> sortedTerms){
        int size = sortedTerms.size();
        int columnLen = sortedTerms.get(0).getTermDataArr().length;

        Set<Integer> data67890 = Sets.newHashSet(6, 7, 8, 9, 10);
        Set<Integer> data24 = Sets.newHashSet(2, 4);
        Set<Integer> data680 = Sets.newHashSet(6, 8, 10);

        // 遍历每列
        for (int c = 0; c < columnLen / 3; c++) {
            // 连续错误的次数
            int countFail = 0;

            // 遍历每行 首末最后处理
            for (int r = 1; r <= size - 2; r++) {

                // 当前行
                Term curTerm = sortedTerms.get(r);
                Long termNum = curTerm.getTermNum();
                Integer[] termDataArr = curTerm.getTermDataArr();
                Integer termData = termDataArr[c * 3];
                // 0:小，1:大
                Integer bigSmall = termDataArr[c * 3 + 2];

                // 判断匹配24
                if (!data24.contains(termData)) {
                    continue;
                }

                // 前面一行
                Integer[] preTermDataArr = sortedTerms.get(r - 1).getTermDataArr();
                Integer preTermData = preTermDataArr[c * 3];

                // 后面一行
                Integer[] nextTermDataArr = sortedTerms.get(r + 1).getTermDataArr();
                Integer nextTermData = nextTermDataArr[c * 3];
                Integer nextBigSmall = nextTermDataArr[c * 3 + 2];

                // 前面一行的判断
                if (data680.contains(preTermData)) {
                    if (1 == nextBigSmall) {
                        countFail = 0;
                    } else {
                        countFail++;
                    }
                }

                // 后面一行的判断
                if (data67890.contains(nextTermData)) {
                    // 判断是否最后一行
                    if (r == size - 2) {
                        // 快要告警了！！！！！
                        break;
                    }

                    Integer next2BigSmall = sortedTerms.get(r + 2).getTermDataArr()[c * 3 + 2];
                    if (1 == next2BigSmall) {
                        countFail = 0;
                    } else {
                        countFail++;
                    }

                }
                log.info("{} c={},r={},termNum={},countFail={}", name, c, r, termNum, countFail);

            }// 遍历每行 首末最后处理

            // 是都达到预警阈值
            if (countFail < data24CountFailAlert) {
                continue;
            }

            // 最新一行
            Integer[] lastTermDataArr = sortedTerms.get(size - 1).getTermDataArr();
            Integer lastTermData = lastTermDataArr[c * 3];
            // 0:小，1:大
            Integer lastBigSmall = lastTermDataArr[c * 3 + 2];

            boolean isAlert = false;

            // 24 该判断前一行是大
            if (data24.contains(lastTermData)) {
                if (1 == sortedTerms.get(size - 2).getTermDataArr()[c * 3 + 2]) {
                    // do alert
                    isAlert = true;
                }
            }
            // 大  该判断前一行是24
            if (1 == lastBigSmall) {
                if (data24.contains(sortedTerms.get(size - 2).getTermDataArr()[c * 3])) {
                    // do alert
                    isAlert = true;
                }
            }

            if (isAlert) {

                String alertContent = ""
                    + "种类：24，前判680，后判67890\r\n"
                    + "错误：" + countFail + "次\r\n"
                    + "列号：" + (c + 1) + "\n"
                    + "时间：" + new Date() + "\n"
                    + "";
                AbstractService.AlertTask alertTask = AbstractService.AlertTask.builder()
                    .title(name)
                    .content(alertContent)
                    .url(url)
                    .build();
                alertTaskQueue.add(alertTask);
            }

        }// 遍历每列
    }

}
