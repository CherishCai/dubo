package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.constant.DuboMsgType;
import cn.cherish.dubo.dubo.dto.resp.DuboMsgResp;
import cn.cherish.dubo.dubo.dto.resp.TermCacheResp;
import cn.cherish.dubo.dubo.entity.Combination;
import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.util.AlertOverUtils;
import cn.cherish.dubo.dubo.util.DuboRuleUtils;
import cn.cherish.dubo.dubo.util.DuboUtils;
import cn.cherish.dubo.dubo.util.DuboUtils.History;
import cn.cherish.dubo.dubo.util.IntUtils;
import cn.cherish.dubo.dubo.util.MailUtils;
import cn.cherish.dubo.dubo.util.SMSUtils;
import cn.cherish.dubo.dubo.util.rule.DuboRule;
import com.google.common.base.Joiner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    public volatile String PRE_URL = HOST + "/v4";
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

    protected String mailSubject = "飞艇";
    protected String mailContent = "";

    protected String url = "http://ft.zzj321.com";
    protected int evenOddTickNum = 7;

    protected static final Queue<SmsTask> smsTaskQueue = new ConcurrentLinkedQueue<>();
    protected static final Queue<MailTask> mailTaskQueue = new ConcurrentLinkedQueue<>();
    protected static final Queue<AlertTask> alertTaskQueue = new ConcurrentLinkedQueue<>();

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

            Integer[] termDataArr12 = term.getTermDataArr12();
            int len2 = termDataArr12.length;
            Integer[] newArr2 = new Integer[len * 3];
            for (int i = 0; i < len2; i++) {
                newArr2[i * 3] = termDataArr12[i];
                newArr2[i * 3 + 1] = termDataArr12[i] % 2 == 0 ? 0 : 1;
                newArr2[i * 3 + 2] = termDataArr12[i] <= 11 ? 0 : 1;
            }
            term.setTermDataArr12(newArr2);

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

    @Scheduled(fixedDelay = 2 * 1000, initialDelay = 10 * 1000)
    public void dealSMSTask() {
        log.info("dealSMSTask 2 sec, smsTaskQueue size:{}", smsTaskQueue.size());
        List<SmsTask> failureList = new ArrayList<>();
        while (!smsTaskQueue.isEmpty()) {
            SmsTask task = smsTaskQueue.poll();
            boolean sendSMS = true;
            try {
                sendSMS = SMSUtils.send(task.phoneNums, task.smsCode);
            } catch (Exception e) {
                log.error("SMSUtils.send error, ", e);
                sendSMS = false;
            }

            log.info("dealSMSTask,sendSMS:{}", sendSMS);
            // 添加到重试
            if (!sendSMS) {
//                failureList.add(task);
            }
        }

        smsTaskQueue.addAll(failureList);
    }

    @Scheduled(fixedDelay = 2 * 1000, initialDelay = 10 * 1000)
    public void dealMailTask() {
        log.info("dealMailTask 2 sec, mailTaskQueue size:{}", mailTaskQueue.size());
//        List<MailTask> failureList = new ArrayList<>();
        while (!mailTaskQueue.isEmpty()) {
            MailTask task = mailTaskQueue.poll();
            boolean sendMail = true;
            try {
                sendMail = MailUtils.htmlMail(task.mailTargets, task.mailSubject, task.mailContent);
            } catch (Exception e) {
                log.error("MailUtils.send error, ", e);
                sendMail = false;
            }

            log.info("dealMailTask,sendMail:{}", sendMail);
            // 添加到重试
            if (!sendMail) {
//                failureList.add(task);
            }
        }

//        mailTaskQueue.addAll(failureList);
    }

    @Scheduled(fixedDelay = 2 * 1000, initialDelay = 10 * 1000)
    public void dealAlertTask() {
        log.info("dealAlertTask 2 sec, alertTaskQueue size:{}", alertTaskQueue.size());
//        List<MailTask> failureList = new ArrayList<>();
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


    /**
     * 单双 ❌ 五次
     */
    protected void evenOddTick(List<Term> terms, int skip, int evenOddTickNum) {
        List<Term> list = terms;
        if (CollectionUtils.isEmpty(list)) {
            return;
        }


        int size = list.size();
        Term term = list.get(size - skip - 1);
        Long termNum = term.getTermNum();

        int len = term.getTermDataArr().length / 3;
        boolean[][] evenOddBool = new boolean[evenOddTickNum][len];

        for (int k = 0; k < evenOddTickNum; k++) {
            int cur = size - skip - evenOddTickNum + k;

            Term curTerm = list.get(cur);
            Integer[] curTermDataArr = curTerm.getTermDataArr();

            Term nextTerm = list.get(cur + skip);
            Integer[] nextTermDataArr = nextTerm.getTermDataArr();

            for (int i = 0; i < len; i++) {
                Integer curI = curTermDataArr[i*3];

                // 寻找到下 skip 列中与当前列相等的值，取得下一列的与前一列值
                for (int j = 0; j < len; j++) {

                    Integer nextI = nextTermDataArr[j*3];
                    if (Objects.equals(nextI, curI)) {
                        Integer nextEvenOdd = nextTermDataArr[j*3+1];
                        Integer curEvenOdd = list.get(cur + skip - 1).getTermDataArr()[j * 3 + 1];

                        // 赋值给当前位置
                        evenOddBool[k][i] = Objects.equals(curEvenOdd, nextEvenOdd);
                        break;
                    }

                }

            }
        }

        printArr(len, evenOddBool, evenOddTickNum);
        // 计算false
        for (int i = 0; i < len; i++) {
            boolean isFalseTick = false;
            for (int j = 0; j < evenOddTickNum; j++) {
                if (evenOddBool[j][i]) {
                    isFalseTick = true;
                    break;
                }
            }

            // 这一列满足全部false
            if (!isFalseTick) {
                // 列号
                int c = i + 1;

                String content = "<p style='font-size:36px'>"
                    + "<a href='"+url+"'>间隔："+skip+" 连续打X：" + evenOddTickNum + "次</a><br>"
                    + "<b style='color:red'>种类：单双</b><br>"
                    + "期号：" + termNum + "<br>"
                    + "列号：" + c + "<br>"
                    + "时间：" + new Date() + "<br>"
                    + "</p>";

                String alertContent = ""
                    + "间隔："+skip+" 连续打X：" + evenOddTickNum + "次\r\n"
                    + "种类：单双\r\n"
                    + "期号：" + termNum + "\r\n"
                    + "列号：" + c + "\n"
                    + "时间：" + new Date() + "\n"
                    + "";

                SmsTask smsTask = SmsTask.builder()
                    .phoneNums(SMSUtils.phones2)
                    .smsCode("X" + c + SMSUtils.randomCode())
                    .build();
                smsTaskQueue.add(smsTask);

                MailTask mailTask = MailTask.builder()
                    .mailTargets(MailUtils.targets)
                    .mailSubject(mailSubject+skip)
                    .mailContent(content)
                    .build();
                mailTaskQueue.add(mailTask);

                AlertTask alertTask = AlertTask.builder()
                    .title(mailSubject+skip)
                    .content(alertContent)
                    .url(url)
                    .build();
                alertTaskQueue.add(alertTask);
            }
        }
    }

    /**
     * 大小 ❌ 五次
     */
    protected void bigSmallTick(List<Term> terms, int skip, int evenOddTickNum) {
        List<Term> list = terms;
        if (CollectionUtils.isEmpty(list)) {
            return;
        }


        int size = list.size();
        Term term = list.get(size - skip - 1);
        Long termNum = term.getTermNum();

        int len = term.getTermDataArr().length / 3;
        boolean[][] keepBool = new boolean[evenOddTickNum][len];

        for (int k = 0; k < evenOddTickNum; k++) {
            int cur = size - skip - evenOddTickNum + k;

            Term curTerm = list.get(cur);
            Integer[] curTermDataArr = curTerm.getTermDataArr();

            Term nextTerm = list.get(cur + skip);
            Integer[] nextTermDataArr = nextTerm.getTermDataArr();

            for (int i = 0; i < len; i++) {
                Integer curI = curTermDataArr[i*3];

                // 寻找到下一列中与当前列相等的值，取得下一列的与前一列值
                for (int j = 0; j < len; j++) {

                    Integer nextI = nextTermDataArr[j*3];
                    if (Objects.equals(nextI, curI)) {
                        Integer nextEvenOdd = nextTermDataArr[j*3+2];
                        Integer curEvenOdd = list.get(cur + skip - 1).getTermDataArr()[j * 3 + 2];

                        // 赋值给当前位置
                        keepBool[k][i] = Objects.equals(curEvenOdd, nextEvenOdd);
                        break;
                    }

                }

            }
        }

        printArr(len, keepBool, evenOddTickNum);
        // 计算false
        for (int i = 0; i < len; i++) {
            boolean isFalseTick = false;
            for (int j = 0; j < evenOddTickNum; j++) {
                if (keepBool[j][i]) {
                    isFalseTick = true;
                    break;
                }
            }

            // 这一列满足全部false
            if (!isFalseTick) {
                // 列号
                int c = i + 1;

                String content = "<p style='font-size:36px'>"
                    + "<a href='"+url+"'>间隔："+skip+" 连续打X：" + evenOddTickNum + "次</a><br>"
                    + "<b style='color:red'>种类：大小</b><br>"
                    + "期号：" + termNum + "<br>"
                    + "列号：" + c + "<br>"
                    + "时间：" + new Date() + "<br>"
                    + "</p>";

                String alertContent = ""
                    + "间隔："+skip+" 连续打X：" + evenOddTickNum + "次\r\n"
                    + "种类：大小\r\n"
                    + "期号：" + termNum + "\r\n"
                    + "列号：" + c + "\n"
                    + "时间：" + new Date() + "\n"
                    + "";

                SmsTask smsTask = SmsTask.builder()
                    .phoneNums(SMSUtils.phones2)
                    .smsCode("X" + c + SMSUtils.randomCode())
                    .build();
                smsTaskQueue.add(smsTask);

                MailTask mailTask = MailTask.builder()
                    .mailTargets(MailUtils.targets)
                    .mailSubject(mailSubject+skip)
                    .mailContent(content)
                    .build();
                mailTaskQueue.add(mailTask);

                AlertTask alertTask = AlertTask.builder()
                    .title(mailSubject+skip)
                    .content(alertContent)
                    .url(url)
                    .build();
                alertTaskQueue.add(alertTask);
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
    static class SmsTask {

        String smsCode;
        Set<String> phoneNums;
    }

    @Data
    @Builder
    static class MailTask {

        String[] mailTargets;
        String mailSubject;
        String mailContent;
    }

    @Data
    @Builder
    static class AlertTask {

        String title;
        String content;
        String url;
    }

}
