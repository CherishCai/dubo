package cn.cherish.dubo.dubo.entity;

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
//@TableName("dubo_combination")
public class Combination implements Serializable {

    private static final long serialVersionUID = 6098103423384200506L;

//    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

//    @TableField("termNum")
    private Integer termNum;

//    @TableField("first")
    private Integer first;

//    @TableField("second")
    private Integer second;

//    @TableField("third")
    private Integer third;

//    @TableField("createdTime")
    private Date createdTime;

}
