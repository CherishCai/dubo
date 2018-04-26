package cn.cherish.dubo.dubo.controller;

import cn.cherish.dubo.dubo.entity.Combination;
import cn.cherish.dubo.dubo.service.DuboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
    public ApiResult<Map<String, List<Combination>>> data() {
        return new ApiResult<>(duboService.getCache());
    }

}

