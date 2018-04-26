package cn.cherish.dubo.dubo.service.impl;

import cn.cherish.dubo.dubo.entity.Term;
import cn.cherish.dubo.dubo.mapper.TermMapper;
import cn.cherish.dubo.dubo.service.TermService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author Cherish
 * @since 2018-04-26
 */
@Service
public class TermServiceImpl extends ServiceImpl<TermMapper, Term> implements TermService {


    @Override
    public Term findLargeTerm() {
        EntityWrapper<Term> wrapper = new EntityWrapper<>();
        wrapper.orderDesc(Collections.singletonList("termNum"));
        wrapper.last("limit 1");
       return this.selectOne(wrapper);
    }

    @Override
    public Term findOneByTermNum(int termNum) {
        EntityWrapper<Term> wrapper = new EntityWrapper<>();
        wrapper.eq("termNum", termNum);
        return this.selectOne(wrapper);
    }



}
