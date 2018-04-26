package cn.cherish.dubo.dubo.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 * @author Cherish
 * @since 2018-04-26
 */
@Data
@TableName("dubo_term")
public class Term implements Serializable {

    private static final long serialVersionUID = -6557593745380183422L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("termNum")
    private Integer termNum;

    @TableField("termData")
    private String termData;

    @TableField("betEndTime")
    private String betEndTime;

    @TableField("lotteryNum")
    private String lotteryNum;
    @TableField("lotteryTime")
    private String lotteryTime;
    @TableField("gameId")
    private Integer gameId;
    @TableField("lotteryDate")
    private String lotteryDate;
    @TableField("lotteryDateStr")
    private String lotteryDateStr;
    @TableField("termNumStr")
    private String termNumStr;

    @TableField("createdTime")
    private Date createdTime;

}
