/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

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
//@TableName("dubo_term")
public class Term implements Serializable {

    private static final long serialVersionUID = -6557593745380183422L;

//    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

//    @TableField("termNum")
    private Integer termNum;

//    @TableField("termData")
    private String termData;

//    @TableField("betEndTime")
    private String betEndTime;

//    @TableField("lotteryNum")
    private String lotteryNum;
//    @TableField("lotteryTime")
    private String lotteryTime;
//    @TableField("gameId")
    private Integer gameId;
//    @TableField("lotteryDate")
    private String lotteryDate;
//    @TableField("lotteryDateStr")
    private String lotteryDateStr;
//    @TableField("termNumStr")
    private String termNumStr;

//    @TableField("createdTime")
    private Date createdTime;

}
