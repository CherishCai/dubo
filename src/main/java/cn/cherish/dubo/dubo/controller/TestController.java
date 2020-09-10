package cn.cherish.dubo.dubo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @author hongwen.chw@antfin.com
 * @version Id: TestController, v0.1 2020-06-03 15:21 mengyuan Exp $
 */
@Slf4j
@RestController
@RequestMapping("/test")
@CrossOrigin("*")
public class TestController {

    @GetMapping("/long-polling")
    public DeferredResult<String> getData(){
        DeferredResult<String> result = new DeferredResult<>();

        Thread thread = new Thread(() ->{
            try {
                //与客户端建立长连接之后 5秒之后返回结果
                Thread.sleep(5000);
                result.setResult("hello world");
            } catch (Exception ignored){
            }
        });
        thread.start();
        return result;
    }

}
