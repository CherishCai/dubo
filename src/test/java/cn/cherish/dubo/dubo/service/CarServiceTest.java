package cn.cherish.dubo.dubo.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/26 13:07
 */
public class CarServiceTest {

    CarService carService;

    @Before
    public void setUp() throws Exception {
        carService = new CarService();
//        duboService.termService = new TermServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void init() throws Exception {

        System.out.println("duboService = " + carService);
    }

    @Test
    public void dealCache() throws Exception {
        carService.dealCache();

        System.out.println("duboService = " + carService);
    }

}