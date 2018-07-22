package cn.cherish.dubo.dubo.controller;

import cn.cherish.dubo.dubo.dto.resp.DuboMsgResp;
import cn.cherish.dubo.dubo.service.DuboService;
import cn.cherish.dubo.dubo.service.FlyService;
import cn.cherish.dubo.dubo.util.SMSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 * @author Cherish
 * @since 2018-04-26
 */
@Slf4j
@RestController
@RequestMapping("/dubo")
@CrossOrigin("*")
public class DuboController {

    private final DuboService duboService;
    private final FlyService flyService;

    @Autowired
    public DuboController(DuboService duboService, FlyService flyService) {
        this.duboService = duboService;
        this.flyService = flyService;
    }

    @GetMapping("/data")
    public ApiResult<DuboMsgResp> data(@RequestParam(required = false) String kk) {
        return new ApiResult<>(duboService.getMsg(kk));
    }

    @GetMapping("/fly")
    public ApiResult<DuboMsgResp> fly(@RequestParam(required = false) String kk) {
        return new ApiResult<>(flyService.getMsg(kk));
    }

    @GetMapping("/cars/term/cache")
    public ApiResult<?> termCache() {
        return new ApiResult<>(duboService.getTermsCache());
    }

    @GetMapping("/flys/term/cache")
    public ApiResult<?> flyTermCache() {
        return new ApiResult<>(flyService.getTermsCache());
    }

    @GetMapping("/sendSMS")
    public void sendSMS(@RequestParam(required = false) String kk) {
        try {
            boolean send = SMSUtils.send(SMSUtils.phones, kk + SMSUtils.randomCode());
            log.info("send SMS by req, result:{}", send);
        } catch (Exception e) {
            log.error("send SMS by req error", e);
        }
    }

}

