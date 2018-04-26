package cn.cherish.dubo.dubo.service;

import cn.cherish.dubo.dubo.entity.Term;
import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 * @author Cherish
 * @since 2018-04-26
 */
public interface TermService extends IService<Term> {

    Term findLargeTerm();

    Term findOneByTermNum(int termNum);


}
