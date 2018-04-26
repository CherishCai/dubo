package cn.cherish.dubo.dubo.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 13:07
 */
public class DuboServiceTest {

    DuboService duboService;

    @Before
    public void setUp() throws Exception {
        duboService = new DuboService();
//        duboService.termService = new TermServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void init() throws Exception {
        duboService.init();

        System.out.println("duboService = " + duboService);
    }

    @Test
    public void dealCache() throws Exception {
        duboService.dealCache();

        System.out.println("duboService = " + duboService);
    }

}