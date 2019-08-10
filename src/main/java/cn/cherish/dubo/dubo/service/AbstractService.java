package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.constant.DuboMsgType;
import cn.cherish.dubo.dubo.dto.resp.DuboMsgResp;
import cn.cherish.dubo.dubo.dto.resp.TermCacheResp;
import cn.cherish.dubo.dubo.entity.Combination;
import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.AlertOverUtils;
import cn.cherish.dubo.dubo.util.DuboUtils;
import cn.cherish.dubo.dubo.util.DuboUtils.History;
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

    public volatile String HOST = "http://47.75.78.66:8080";
    public volatile String type = "car";
    public volatile String name = "赛车";
    public volatile String SPILT = ",";
    public volatile String newestNumStr = "";
    public volatile Map<String, List<Combination>> allMap = new HashMap<>();
    public volatile Map<String, List<Combination>> sub15Map = new HashMap<>();

    public volatile Map<String, List<Combination>> todayMap = new HashMap<>();
    public volatile Map<String, List<Combination>> todaySub15Map = new HashMap<>();
    public volatile List<Term> termsCache = Collections.emptyList();
    public volatile List<Term> termsCacheWithBigOdd = Collections.emptyList();

    protected String url = "http://ft.zzj321.com";

    protected static final Queue<AlertTask> alertTaskQueue = new ConcurrentLinkedQueue<>();

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

    protected Term newTerm(DuboUtils.History.RowsBean rowsBean) {
        String termNum = rowsBean.getTermNum();
        if (termNum.length() > 6) {
            termNum = termNum.substring(6);
        }
        long termNumLong = Long.parseLong(termNum);

        Term newTerm = new Term();
        BeanUtils.copyProperties(rowsBean, newTerm);
        newTerm.setTermNum(termNumLong);
        newTerm.setCreatedTime(new Date());

        // 处理term data
        StringBuilder sb = new StringBuilder();
        sb.append(rowsBean.getN1()).append(SPILT);
        sb.append(rowsBean.getN2()).append(SPILT);
        sb.append(rowsBean.getN3()).append(SPILT);
        sb.append(rowsBean.getN4()).append(SPILT);
        sb.append(rowsBean.getN5()).append(SPILT);
        sb.append(rowsBean.getN6()).append(SPILT);
        sb.append(rowsBean.getN7()).append(SPILT);
        sb.append(rowsBean.getN8()).append(SPILT);
        sb.append(rowsBean.getN9()).append(SPILT);
        sb.append(rowsBean.getN10());

        String termStr = sb.toString();
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

    protected void dealHistory(History history) {
        if (history == null || !history.isSuccess()) {
            return;
        }
        List<Term> terms = history.getRows().stream().map(this::newTerm).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(terms)) {
            return;
        }

        if (!CollectionUtils.isEmpty(termsCache)
            && termsCache.get(0).getTermNum().equals(terms.get(0).getTermNum())) {
            // 没有新数据进来
            return;
        }

        {
            // cache
            termsCache = history.getRows().stream().map(this::newTerm).collect(Collectors.toList());
            newestNumStr = termsCache.get(0).getTermNum() + "";

        }

        cacheDataWithBigOdd(history.getRows().stream().map(this::newTerm).collect(Collectors.toList()));

        afterDealHistory(
            termsCacheWithBigOdd.stream()
                .sorted(Comparator.comparingLong(Term::getTermNum))
                .collect(Collectors.toList())
        );
    }

    /**
     *
     * @param sortedTerms 从小到大的数据
     */
    protected abstract void afterDealHistory(List<Term> sortedTerms);

    private void cacheDataWithBigOdd(List<Term> terms) {

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

            Integer[] termDataArr12 = term.getTermDataArr12();
            int len2 = termDataArr12.length;
            Integer[] newArr2 = new Integer[len * 3];
            for (int i = 0; i < len2; i++) {
                newArr2[i * 3] = termDataArr12[i];
                // 0:双，1:单
                newArr2[i * 3 + 1] = termDataArr12[i] % 2 == 0 ? 0 : 1;
                // 0:小，1:大
                newArr2[i * 3 + 2] = termDataArr12[i] <= 11 ? 0 : 1;
            }
            term.setTermDataArr12(newArr2);

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

    protected void printArr(int len, boolean[][] evenOddBool, int evenOddTickNum) {
        for (int k = 0; k < evenOddTickNum; k++) {
            for (int i = 0; i < len; i++) {
                System.out.print((evenOddBool[k][i] ? 1 : 0) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    @Data
    @Builder
    static class AlertTask {

        String title;
        String content;
        String url;
    }

}
