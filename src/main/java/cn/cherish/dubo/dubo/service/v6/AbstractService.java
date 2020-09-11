package cn.cherish.dubo.dubo.service.v6;

import cn.cherish.dubo.dubo.constant.DuboMsgType;
import cn.cherish.dubo.dubo.dto.resp.DuboMsgResp;
import cn.cherish.dubo.dubo.dto.resp.TermCacheResp;
import cn.cherish.dubo.dubo.entity.Combination;
import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.AlertOverUtils;
import cn.cherish.dubo.dubo.util.IntUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

/**
 * @author caihongwen@u51.com
 * @date 2018/7/14 13:40
 */
@Slf4j
public abstract class AbstractService {

    public volatile String SPILT = ",";
    public volatile String newestNumStr = "";
    public volatile Map<String, List<Combination>> allMap = new HashMap<>();
    public volatile Map<String, List<Combination>> sub15Map = new HashMap<>();

    public volatile Map<String, List<Combination>> todayMap = new HashMap<>();
    public volatile Map<String, List<Combination>> todaySub15Map = new HashMap<>();
    public volatile List<Term> termsCache = Collections.emptyList();
    public volatile List<Term> termsCacheWithBigOdd = Collections.emptyList();

    // https://api.apiose122.com/pks/getPksHistoryList.do?lotCode=10037
    protected String url = "http://ft.zzj321.com";

    protected static final Queue<AlertTask> alertTaskQueue = new ConcurrentLinkedQueue<>();

    protected abstract String getType();
    protected abstract String getName();

    public TermCacheResp getTermsCache(){
        List<Term> list = termsCacheWithBigOdd;
        if (!CollectionUtils.isEmpty(list)) {
            list = list.stream().sorted(Comparator.comparingLong(Term::getTermNum)).collect(Collectors.toList());
        }
        return TermCacheResp.builder().newestNumStr(newestNumStr).records(list).build();
    }

    public DuboMsgResp getMsg(String kk) {
        DuboMsgResp duboMsgResp = null;

        DuboMsgResp.DuboMsgRespBuilder builder = DuboMsgResp.builder();
        builder.newestNumStr(newestNumStr);

        if (DuboMsgType.all.name().equals(kk)) {
            duboMsgResp = builder
                .all(allMap)
                .build();
        } else if (DuboMsgType.sub15.name().equals(kk)) {
            duboMsgResp = builder
                .sub15(sub15Map)
                .build();
        } else if (DuboMsgType.today.name().equals(kk)) {
            duboMsgResp = builder
                .today(todayMap)
                .build();
        } else if (DuboMsgType.todaySub15.name().equals(kk)) {
            duboMsgResp = builder
                .todaySub15(todaySub15Map)
                .build();
        } else {
            duboMsgResp = builder
                .all(allMap)
                .sub15(sub15Map)
                .today(todayMap)
                .todaySub15(todaySub15Map)
                .build();
        }
        return duboMsgResp;
    }

    protected Term newTerm(CarResult.ResultBean.DataBean rowsBean) {

        long termNumLong = rowsBean.getPreDrawIssue();

        Term newTerm = new Term();
        BeanUtils.copyProperties(rowsBean, newTerm);
        newTerm.setTermNum(termNumLong);
        newTerm.setCreatedTime(new Date());

        // 处理term data
        String termStr = rowsBean.getPreDrawCode();
        String[] split = termStr.split(SPILT);
        Integer[] ints = IntUtils.strToInts(split);

        // 一二列之和
        Integer[] termDataArr12 = new Integer[1];
        termDataArr12[0] = ints[0] + ints[1];

        newTerm.setTermDataArr(ints);
        newTerm.setTermData12(ints[0] + ints[1]);
        newTerm.setTermDataArr12(termDataArr12);
        newTerm.setTermData(termStr);
        return newTerm;
    }

    protected void dealHistory(CarResult history) {
        if (history == null || history.getErrorCode() != 0) {
            log.error("history == null || history.getErrorCode() != 0, {}", history);
            return;
        }
        List<Term> terms = history.getResult().getData().stream().map(this::newTerm).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(terms)) {
            log.error("CollectionUtils.isEmpty(terms), {}", terms);
            return;
        }

        if (!CollectionUtils.isEmpty(termsCache)
            && termsCache.get(0).getTermNum().equals(terms.get(0).getTermNum())) {
            // 没有新数据进来
            log.info("没有新数据进来, TermNum={}", terms.get(0).getTermNum());
            return;
        }

        {
            // cache
            termsCache = history.getResult().getData().stream().map(this::newTerm).collect(Collectors.toList());
            newestNumStr = termsCache.get(0).getTermNum() + "";
        }

        cacheDataWithBigOdd(history.getResult().getData().stream().map(this::newTerm).collect(Collectors.toList()));

        afterDealHistory(termsCacheWithBigOdd);
        afterDealSortHistory(
            termsCacheWithBigOdd.stream()
                .sorted(Comparator.comparingLong(Term::getTermNum))
                .collect(Collectors.toList())
        );
    }

    /**
     *
     * @param termsCacheWithBigOdd 从大到小的数据
     */
    protected abstract void afterDealHistory(List<Term> termsCacheWithBigOdd);

    /**
     *
     * @param sortedTerms 从小到大的数据
     */
    protected abstract void afterDealSortHistory(List<Term> sortedTerms);

    private void cacheDataWithBigOdd(List<Term> terms) {
        log.info("cacheDataWithBigOdd, TermNum={}", terms.get(0).getTermNum());

        termsCacheWithBigOdd = terms.stream().peek(term -> {
            Integer[] termDataArr = term.getTermDataArr();
            int len = termDataArr.length;
            Integer[] newArr = new Integer[len * 3];
            for (int i = 0; i < len; i++) {
                newArr[i * 3] = termDataArr[i];
                // 0:双，1:单
                newArr[i * 3 + 1] = termDataArr[i] % 2 == 0 ? 0 : 1;
                // 0:小，1:大
                newArr[i * 3 + 2] = termDataArr[i] <= 5 ? 0 : 1;
            }
            term.setTermDataArr(newArr);

        }).collect(Collectors.toList());
    }

    @Scheduled(fixedDelay = 2 * 1000, initialDelay = 10 * 1000)
    public void dealAlertTask() {
        log.info("dealAlertTask 2 sec, alertTaskQueue size:{}", alertTaskQueue.size());
        while (!alertTaskQueue.isEmpty()) {
            AlertTask task = alertTaskQueue.poll();
            boolean send = true;
            try {
                send = AlertOverUtils.send(task.title, task.content, task.url);
            } catch (Exception e) {
                log.error("AlertOverUtils.send error, ", e);
                send = false;
            }

            log.info("dealAlertTask,send:{}", send);
            // 添加到重试
            if (!send) {
//                failureList.add(task);
            }
        }

    }

    protected final void dataMajor(final List<Term> sortedTerms, final int countFailAlert, boolean oddEven) {

        log.info("dataMajor, TermNum={},countFailAlert={},oddEven={}",
                sortedTerms.get(0).getTermNum(), countFailAlert, oddEven);

        int pdd = oddEven ? 1 : 2;

        String nn = oddEven ? "单双" : "大小";

        int size = sortedTerms.size();
        int columnLen = sortedTerms.get(0).getTermDataArr().length;
        int cLen = columnLen / 3;

        Term r2Term = sortedTerms.get(2);
        Long r2TermNum = r2Term.getTermNum();
        Integer[] r2TermDataArr = r2Term.getTermDataArr();

        // 遍历每列
        for (int cFirst = 0; cFirst < cLen; cFirst++) {
            // 连续错误的次数

            int countFail = 0;
            Integer termData = r2TermDataArr[cFirst * 3];
            Integer last2TermDataOdd = sortedTerms.get(2 - 2).getTermDataArr()[cFirst * 3 + pdd];
            Integer next1TermDataOdd = sortedTerms.get(2 + 1).getTermDataArr()[cFirst * 3 + pdd];

            if (last2TermDataOdd.equals(next1TermDataOdd)) {
                // 同种，没必要继续，下一个  0:双，1:单 ; 0:小，1:大
                continue;
            }
            countFail++;

            // 跳过前三列
            ff:for (int r = 3; r < size; r++) {
                for (int cThir = 0; cThir < cLen; cThir++) {
                    if (!sortedTerms.get(r).getTermDataArr()[cThir * 3].equals(termData)) {
                        continue;
                    }

                    // 找到和第二列同值的列，然后判断
                    last2TermDataOdd = sortedTerms.get(r - 2).getTermDataArr()[cThir * 3 + pdd];
                    next1TermDataOdd = sortedTerms.get(r + 1).getTermDataArr()[cThir * 3 + pdd];
                    if (last2TermDataOdd.equals(next1TermDataOdd)) {
                        // 同种，没必要继续  0:双，1:单 ; 0:小，1:大
                        break ff;
                    }
                    countFail++;
                    log.info("{} oddEven={},c={},r={},termNum={},countFail={}",
                            getName(), oddEven, cFirst, r, r2TermNum, countFail);
                }

                // 达到预警阈值
                if (countFail >= countFailAlert) {

                    String alertContent = ""
                            + "种类：" + getName() + " " + nn + "\r\n"
                            + "错误：" + countFail + "次\r\n"
                            + "行号：" + (r2TermNum) + "\n"
                            + "列号：" + (cFirst + 1) + "\n"
                            + "时间：" + new Date() + "\n"
                            + "";
                    AlertTask alertTask = AlertTask.builder()
                            .title(getName())
                            .content(alertContent)
                            .url(url)
                            .build();
                    alertTaskQueue.add(alertTask);
                }

            }

        }// 遍历每列
    }

    @Data
    @Builder
    static class AlertTask {

        String title;
        String content;
        String url;
    }

}
