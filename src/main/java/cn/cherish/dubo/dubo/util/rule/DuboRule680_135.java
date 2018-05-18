package cn.cherish.dubo.dubo.util.rule;

import java.util.Arrays;
import java.util.List;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 17:31
 */
public class DuboRule680_135 extends DuboRule {

    private static final List<Integer> first = Arrays.asList(6, 8, 10);
    private static final List<Integer> second = Arrays.asList(1, 3, 5);

    @Override
    public List<Integer> getFirst() {
        return first;
    }

    @Override
    public List<Integer> getSecond() {
        return second;
    }
}
