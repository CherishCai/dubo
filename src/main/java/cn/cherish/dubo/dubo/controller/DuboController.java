package cn.cherish.dubo.dubo.controller;

import cn.cherish.dubo.dubo.dto.resp.DuboMsgResp;
import cn.cherish.dubo.dubo.service.DuboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 * @author Cherish
 * @since 2018-04-26
 */
@RestController
@RequestMapping("/dubo")
@CrossOrigin("*")
public class DuboController {

    private final DuboService duboService;

    @Autowired
    public DuboController(DuboService duboService) {
        this.duboService = duboService;
    }

    @GetMapping("/data")
    public ApiResult<DuboMsgResp> data(@RequestParam(required = false) String kk) {
        return new ApiResult<>(duboService.getDuboMsg(kk));
    }

}

