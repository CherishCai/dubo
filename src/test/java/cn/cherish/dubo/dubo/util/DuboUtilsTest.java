package cn.cherish.dubo.dubo.util;

import org.junit.Test;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 1:23
 */
public class DuboUtilsTest {
    @Test
    public void getHistory() throws Exception {
        DuboUtils.History history = DuboUtils.getHistory(1);

        System.out.println("history = " + history);
    }

    @Test
    public void getCurrent() throws Exception {
        DuboUtils.Current current = DuboUtils.getCurrent();

        System.out.println("current = " + current);
    }

}