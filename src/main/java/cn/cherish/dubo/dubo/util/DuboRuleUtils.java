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
    }

    public static List<DuboRule> getRules() {
        return rules;
    }


}
