package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.Data;


@Data
@ApiModel(description = "点赞实体类")
@TableName("`like`")
public class Like{
    /**
     * 点赞ID
     */
    @ApiModelProperty(value = "点赞ID")
    @TableId(type = IdType.AUTO)
    private Integer likeId;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    /**
     * 对象类型( 'Post', 'Comment' )
     */
    @ApiModelProperty(value = "对象类型")
    private Object objectType;

    /**
     * 对象ID
     */
    @ApiModelProperty(value = "对象ID")
    private Integer objectId;

    /**
     * 点赞时间
     */
    @ApiModelProperty(value = "点赞时间")
    private Date likeTime;
}
