package cn.cherish.dubo.dubo.util.rule2;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import lombok.experimental.UtilityClass;

/**
 * @author caihongwen@u51.com
 * @date 2019/1/13 15:15
 */
@UtilityClass
public class Rule2Utils {

    static final Set<Integer> r2479 = Sets.newHashSet(2, 4, 7, 9);
    static final Set<Integer> r135 = Sets.newHashSet(1, 3, 5);
    static final Set<Integer> r680  = Sets.newHashSet(6, 8, 10);
    static List<Set<Integer>> rules680 = Lists.newArrayList(r2479, r135, r680, r135, r680);
    static List<Set<Integer>> rules135 = Lists.newArrayList(r2479, r680, r135, r680, r135);

    public static List<Set<Integer>> rules680(){
        return rules680;
    }
    public static List<Set<Integer>> rules135(){
        return rules135;
    }

}
