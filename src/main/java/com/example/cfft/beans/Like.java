package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;


@Data
@TableName("`like`")
public class Like{
    /**
     * 点赞ID
     */
    @TableId(type = IdType.AUTO)
    private Integer likeId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 对象类型( 'Post', 'Comment' )
     */
    private Object objectType;

    /**
     * 对象ID
     */
    private Integer objectId;

    /**
     * 点赞时间
     */
    private Date likeTime;
}
