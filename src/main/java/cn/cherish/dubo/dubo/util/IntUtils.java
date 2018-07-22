package cn.cherish.dubo.dubo.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @author caihongwen@u51.com
 * @date 2018/7/21 14:20
 */
@Slf4j
@UtilityClass
public class IntUtils {

    public Integer[] strToInts(String[] str){
        Integer[] ints = new Integer[str.length];
        for (int i = 0; i < str.length; i++) {
            ints[i] = Integer.valueOf(str[i]);
        }
        return ints;
    }


}
