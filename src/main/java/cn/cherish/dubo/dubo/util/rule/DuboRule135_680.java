package cn.cherish.dubo.dubo.util.rule;

import java.util.Arrays;
import java.util.List;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/5/18 9:55
 */
public class DuboRule135_680 extends DuboRule {

    private static final List<Integer> first = Arrays.asList(1, 3, 5);
    private static final List<Integer> second = Arrays.asList(6, 8, 10);

    @Override
    public List<Integer> getFirst() {
        return first;
    }

    @Override
    public List<Integer> getSecond() {
        return second;
    }
}
