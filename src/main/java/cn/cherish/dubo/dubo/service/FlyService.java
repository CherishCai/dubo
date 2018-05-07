package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.constant.DuboMsgType;
import cn.cherish.dubo.dubo.dto.resp.DuboMsgResp;
import cn.cherish.dubo.dubo.entity.Combination;
import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.DuboRuleUtils;
import cn.cherish.dubo.dubo.util.DuboUtils;
import cn.cherish.dubo.dubo.util.rule.DuboRule;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 12:18
 */
@Slf4j
@Service
public class FlyService {

    private static final ScheduledExecutorService sch = new ScheduledThreadPoolExecutor(2);
    private static final ScheduledExecutorService schInLoop = new ScheduledThreadPoolExecutor(2);
    private static final long defaultAwardTimeInterval = 1000L;

    private static volatile long awardTimeInterval = defaultAwardTimeInterval;
    private static volatile int periodNumber = 0;

    private Term newTerm(DuboUtils.History.RowsBean rowsBean) {
        String termNum = rowsBean.getTermNum();
        long termNumLong = Long.parseLong(termNum);

        Term newTerm = new Term();
        BeanUtils.copyProperties(rowsBean, newTerm);
        newTerm.setTermNum(termNumLong);
        newTerm.setCreatedTime(new Date());

        // 处理term data
        StringBuilder sb = new StringBuilder();
        sb.append(rowsBean.getN1()).append(",");
        sb.append(rowsBean.getN2()).append(",");
        sb.append(rowsBean.getN3()).append(",");
        sb.append(rowsBean.getN4()).append(",");
        sb.append(rowsBean.getN5()).append(",");
        sb.append(rowsBean.getN6()).append(",");
        sb.append(rowsBean.getN7()).append(",");
        sb.append(rowsBean.getN8()).append(",");
        sb.append(rowsBean.getN9()).append(",");
        sb.append(rowsBean.getN10());

        newTerm.setTermData(sb.toString());
        return newTerm;
    }

    /**
     * 每十秒
     */
    @Scheduled(fixedDelay = 10 * 1000, initialDelay = 5 * 1000)
    public void dealData() {
        log.info("dealData every 10 sec");
        // 处理全局缓存
        dealCache();
    }

    public static volatile List<Combination> cacheAllList = new LinkedList<>();
    public static volatile Map<String, List<Combination>> allMap = new HashMap<>();
    public static volatile Map<String, List<Combination>> sub15Map = new HashMap<>();

    public static volatile Map<String, List<Combination>> todayMap = new HashMap<>();
    public static volatile Map<String, List<Combination>> todaySub15Map = new HashMap<>();

    public DuboMsgResp getMsg(String kk) {
        DuboMsgResp duboMsgResp = null;
        if (DuboMsgType.all.name().equals(kk)) {
            duboMsgResp =  DuboMsgResp.builder()
                    .all(allMap)
                    .build();
        } else if (DuboMsgType.sub15.name().equals(kk)) {
            duboMsgResp = DuboMsgResp.builder()
                    .sub15(sub15Map)
                    .build();
        } else if (DuboMsgType.today.name().equals(kk)) {
            duboMsgResp = DuboMsgResp.builder()
                    .today(todayMap)
                    .build();
        } else if (DuboMsgType.todaySub15.name().equals(kk)) {
            duboMsgResp = DuboMsgResp.builder()
                    .todaySub15(todaySub15Map)
                    .build();
        } else {
            duboMsgResp = DuboMsgResp.builder()
                    .all(allMap)
                    .sub15(sub15Map)
                    .today(todayMap)
                    .todaySub15(todaySub15Map)
                    .build();
        }
        return duboMsgResp;
    }

    public void dealCache() {
        DuboUtils.History history = DuboUtils.getFlyHistory(180);
        if (history == null || !history.isSuccess()) {
            return;
        }

        long start = System.currentTimeMillis();

        List<Term> terms = history.getRows().stream().map(this::newTerm).collect(Collectors.toList());

        // 从小到大
        terms.sort(Comparator.comparingLong(Term::getTermNum));

        List<int[]> collect = terms.stream()
                .map(term -> Arrays.stream(term.getTermData().split(","))
                        .mapToInt(Integer::valueOf)
                        .toArray())
                .collect(Collectors.toList());

        int[][] arr = new int[collect.size()][10];
        collect.toArray(arr);

        final List<DuboRule> rules = DuboRuleUtils.getRules();

        final List<Combination> cacheList = new LinkedList<>();
        cacheAllList = cacheList;

        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                int first = arr[i][j];
                int second = arr[i + 1][j];

                for (DuboRule rule : rules) {
                    List<Integer> ruleFirst = rule.getFirst();
                    List<Integer> ruleSecond = rule.getSecond();

                    if (ruleFirst.contains(first) && ruleSecond.contains(second)) {
                        // 获取第三个
                        Integer third = null;
                        if (i + 2 < arr.length) {
                            third = arr[i + 2][j];
                        }

                        Term term = terms.get(i);

                        Long termNum = term.getTermNum();

                        Combination combination = new Combination();

                        combination.setTermNum(termNum);
                        combination.setFirst(first);
                        combination.setSecond(second);
                        combination.setThird(third);

                        if (third != null) {
                            combination.setThirdEven(third % 2 == 0);
                            combination.setThirdBig(third > 5);
                        }

                        combination.setLotteryDateStr(term.getLotteryDateStr());
                        combination.setCreatedTime(new Date());

                        cacheList.add(combination);
                        break;
                    }
                }// for rules

            }
        }// end big for

        final String todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        for (DuboRule rule : rules) {
            final List<Integer> ruleFirst = rule.getFirst();
            final List<Integer> ruleSecond = rule.getSecond();

            for (Integer first : ruleFirst) {
                List<Combination> singleList = cacheAllList.stream()
                        .filter(c -> Objects.equals(first, c.getFirst()) && ruleSecond.contains(c.getSecond()))
                        .collect(Collectors.toList());

                String firstStr = String.valueOf(first);
                String secondStr = Joiner.on("").join(ruleSecond);
                String mapKey = firstStr + "_" + secondStr;

                allMap.put(mapKey, singleList);
                sub15Map.put(mapKey, sub15(singleList));


                // 过滤得到今天的数据
                List<Combination> todayList = singleList.stream()
                        .filter(combination -> StringUtils.equals(todayStr, combination.getLotteryDateStr()))
                        .collect(Collectors.toList());

                todayMap.put(mapKey, todayList);
                todaySub15Map.put(mapKey, sub15(todayList));
            }
        }

        for (DuboRule rule : rules) {
            final List<Integer> ruleFirst = rule.getFirst();
            final List<Integer> ruleSecond = rule.getSecond();

            List<Combination> blendList = cacheAllList.stream()
                    .filter(c -> ruleFirst.contains(c.getFirst()) && ruleSecond.contains(c.getSecond()))
                    .collect(Collectors.toList());
            
            String firstStr = Joiner.on("").join(ruleFirst);
            String secondStr = Joiner.on("").join(ruleSecond);
            String mapKey = firstStr + "_" + secondStr;

            allMap.put(mapKey, blendList);
            sub15Map.put(mapKey, sub15(blendList));


            // 过滤得到今天的数据
            List<Combination> todayList = blendList.stream()
                    .filter(combination -> StringUtils.equals(todayStr, combination.getLotteryDateStr()))
                    .collect(Collectors.toList());

            todayMap.put(mapKey, todayList);
            todaySub15Map.put(mapKey, sub15(todayList));
        }
        long end = System.currentTimeMillis();

        log.info("deal in cache use {}ms", (end - start));

    }

    private List<Combination> sub15(List<Combination> combinationList) {
        if (CollectionUtils.isEmpty(combinationList)) {
            return combinationList;
        }
        int size = combinationList.size();
        if (size > 15) {
            combinationList = combinationList.subList(size - 15, size);
        }
        return combinationList;
    }


}