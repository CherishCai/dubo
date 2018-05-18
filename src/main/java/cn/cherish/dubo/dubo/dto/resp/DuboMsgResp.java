package cn.cherish.dubo.dubo.dto.resp;

import cn.cherish.dubo.dubo.entity.Combination;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Cherish
 * @version 1.0
 * @date 2018/4/28 14:12
 */
@ApiModel
@Data
@Builder
public class DuboMsgResp {

    private String newestNumStr;

    private Map<String, List<Combination>> all;

    private Map<String, List<Combination>> sub15;

    private Map<String, List<Combination>> today;

    private Map<String, List<Combination>> todaySub15;

}
