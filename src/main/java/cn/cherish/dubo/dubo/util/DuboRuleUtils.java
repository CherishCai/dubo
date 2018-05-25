package cn.cherish.dubo.dubo.util;

import cn.cherish.dubo.dubo.util.rule.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 17:35
 */
public class DuboRuleUtils {

    private DuboRuleUtils() {}

    private static final List<DuboRule> rules;
    static {
        rules = new ArrayList<>();
        rules.add(new DuboRule24_79());
        rules.add(new DuboRule24_135());
        rules.add(new DuboRule79_24());
        rules.add(new DuboRule79_680());
        rules.add(new DuboRule135_680());
        rules.add(new DuboRule680_135());
        // 1_4 7_2 9_4 9_2 2_9 2_7 4_9 4_7 10_3
        rules.add(new DuboRule1_4());
        rules.add(new DuboRule7_2());
        rules.add(new DuboRule7_4());
        rules.add(new DuboRule9_4());
        rules.add(new DuboRule9_2());
        rules.add(new DuboRule2_9());
        rules.add(new DuboRule2_7());
        rules.add(new DuboRule4_9());
        rules.add(new DuboRule4_7());
        rules.add(new DuboRule0_3());
    }

    public static List<DuboRule> getRules() {
        return rules;
    }


}
