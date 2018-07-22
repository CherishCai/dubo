package cn.cherish.dubo.dubo.dto.resp;

import cn.cherish.dubo.dubo.entity.Term;
import io.swagger.annotations.ApiModel;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * @author caihongwen@u51.com
 * @date 2018/7/21 15:47
 */
@ApiModel
@Data
@Builder
public class TermCacheResp {

    private String newestNumStr;

    private List<Term> records;

}
