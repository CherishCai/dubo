package cn.cherish.dubo.dubo.controller;


import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.service.DuboService;
import cn.cherish.dubo.dubo.service.TermService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Cherish
 * @since 2018-04-26
 */
@RestController
@RequestMapping("/dubo/term")
public class TermController {

    private final DuboService duboService;
    private final TermService termService;

    @Autowired
    public TermController(DuboService duboService, TermService termService) {
        this.duboService = duboService;
        this.termService = termService;
    }

    @GetMapping("/test")
    public void test(){
        Term term = new Term();
        term.setTermNum(666);
        term.setTermData("666");
        boolean insert = termService.insert(term);
        System.out.println("insert = " + insert);
    }

}

