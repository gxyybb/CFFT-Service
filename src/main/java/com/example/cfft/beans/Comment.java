package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;


@Data
public class Comment implements Serializable{
    /**
     * 评论ID
     */
    @TableId(type = IdType.AUTO)
    private Integer commentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Integer typeId;

    /**
     * 父评论ID
     */
    private Integer parentCommentId;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数
     */
    private Integer replyCount;
    private String type;
}
