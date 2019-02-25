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

    static final Set<Integer> r24 = Sets.newHashSet(2, 4);
    static final Set<Integer> r2479 = Sets.newHashSet(2, 4, 7, 9);
    static final Set<Integer> r135 = Sets.newHashSet(1, 3, 5);
    static final Set<Integer> r680  = Sets.newHashSet(6, 8, 10);

    static List<Set<Integer>> rules24_680 = Lists.newArrayList(r24, r135, r24, r680);

    static List<Set<Integer>> rules680 = Lists.newArrayList(r2479, r135, r680, r135, r680);
    static List<Set<Integer>> rules135 = Lists.newArrayList(r2479, r680, r135, r680, r135);

    static List<Set<Integer>> rules135_2479 = Lists.newArrayList(r135, r680, r135, r680, r2479);
    static List<Set<Integer>> rules680_2479 = Lists.newArrayList(r680, r135, r680, r135, r2479);


    public static List<Set<Integer>> rules24_680(){
        return rules24_680;
    }

    public static List<Set<Integer>> rules680(){
        return rules680;
    }
    public static List<Set<Integer>> rules135(){
        return rules135;
    }

    public static List<Set<Integer>> rules680_2479(){
        return rules680_2479;
    }
    public static List<Set<Integer>> rules135_2479(){
        return rules135_2479;
    }


}
