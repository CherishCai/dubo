package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.constant.DuboMsgType;
import cn.cherish.dubo.dubo.dto.resp.DuboMsgResp;
import cn.cherish.dubo.dubo.dto.resp.TermCacheResp;
import cn.cherish.dubo.dubo.entity.Combination;
import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.DuboRuleUtils;
import cn.cherish.dubo.dubo.util.DuboUtils;
import cn.cherish.dubo.dubo.util.DuboUtils.History;
import cn.cherish.dubo.dubo.util.IntUtils;
import cn.cherish.dubo.dubo.util.SMSUtils;
import cn.cherish.dubo.dubo.util.rule.DuboRule;
import com.google.common.base.Joiner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author caihongwen@u51.com
 * @date 2018/7/14 13:40
 */
@Slf4j
public abstract class AbstractService {

    public volatile String type = "car";
    public volatile String SPILT = ",";
    public volatile String newestNumStr = "";
    public volatile boolean newestNumChange = false;
    public volatile List<Combination> cacheAllList = new LinkedList<>();
    public volatile Map<String, List<Combination>> allMap = new HashMap<>();
    public volatile Map<String, List<Combination>> sub15Map = new HashMap<>();

    public volatile Map<String, List<Combination>> todayMap = new HashMap<>();
    public volatile Map<String, List<Combination>> todaySub15Map = new HashMap<>();
    public volatile List<Term> termsCache = Collections.emptyList();
    public volatile List<Term> termsCachev4 = Collections.emptyList();

    public TermCacheResp getTermsCache(){
        List<Term> list = termsCachev4;
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

        newTerm.setTermDataArr(ints);
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

        if (!CollectionUtils.isEmpty(termsCache)) {
            if (termsCache.get(0).getTermNum().equals(terms.get(0).getTermNum())) {
                return;
            }
        }

        {
            // cache
            termsCache = history.getRows().stream().map(this::newTerm).collect(Collectors.toList());
        }

        v1data(history.getRows().stream().map(this::newTerm).collect(Collectors.toList()));
        v4data(history.getRows().stream().map(this::newTerm).collect(Collectors.toList()));

        afterDealHistory(
            termsCachev4.stream()
                .sorted(Comparator.comparingLong(Term::getTermNum))
                .collect(Collectors.toList())
        );
    }

    protected abstract void afterDealHistory(List<Term> terms);

    private void v4data(List<Term> terms) {

        termsCachev4 = terms.stream().peek(term -> {
            Integer[] termDataArr = term.getTermDataArr();
            int len = termDataArr.length;
            Integer[] newArr = new Integer[len * 3];
            for (int i = 0; i < len; i++) {
                newArr[i * 3] = termDataArr[i];
                newArr[i * 3 + 1] = termDataArr[i] % 2 == 0 ? 0 : 1;
                newArr[i * 3 + 2] = termDataArr[i] <= 5 ? 0 : 1;
            }
            term.setTermDataArr(newArr);

        }).collect(Collectors.toList());
    }

    private void v1data(List<Term> terms) {
        Term newestTerm = terms.get(0);
        String mTermNum = String.valueOf(newestTerm.getTermNum());
        newestNumChange = !StringUtils.equals(mTermNum, newestNumStr);
        newestNumStr = mTermNum;

        if (newestNumChange) {
            checkAndSendSms(newestTerm.getTermData());
        }

        // 从小到大
        terms.sort(Comparator.comparingLong(Term::getTermNum));

        List<int[]> collect = terms.stream()
            .map(term -> Arrays.stream(term.getTermData().split(SPILT))
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
    }

    protected List<Combination> sub15(List<Combination> combinationList) {
        if (CollectionUtils.isEmpty(combinationList)) {
            return combinationList;
        }
        int size = combinationList.size();
        if (size > 15) {
            combinationList = combinationList.subList(size - 15, size);
        }
        return combinationList;
    }


    protected boolean checkAndSendSms(String termData){
        String[] split = termData.split(SPILT);
        int plus12 = Integer.valueOf(split[0]) + Integer.valueOf(split[1]);
        Integer[] ints = new Integer[]{3, 4, 18, 19};
        for (int i : ints) {
            if (plus12 != i) {
                continue;
            }
            try {
                boolean send = SMSUtils.send(SMSUtils.phones, type + Joiner.on("").join(ints));
                log.info("send SMS, result:{}", send);
            } catch (Exception e) {
                log.error("send SMS error", e);
            }
            return true;
        }
        return false;
    }

}
