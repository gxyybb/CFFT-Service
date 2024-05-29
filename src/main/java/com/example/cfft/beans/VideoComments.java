package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName video_comments
 */
@TableName(value ="video_comments")
@Data
public class VideoComments implements Serializable {
    /**
     * 评论ID
     */
    @TableId(type = IdType.AUTO)
    private Integer commentId;

    /**
     * 视频ID
     */
    private Integer videoId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 评论内容
     */
    private String commentText;

    /**
     * 评论时间
     */
    private Date commentTime;
    private Integer likeCount;
    private Integer replyCount;

}