package com.example.cfft.beans.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
@Data
public class CommentVO{
    /**
     * 评论ID
     */

    private Integer commentId;

    /**
     * 评论内容
     */

    private String content;

    /**
     * 发布时间
     */
    private String userImage;
    private Date publishTime;



    private String username;

    /**
     * 帖子ID
     */
    @ApiModelProperty(value = "帖子ID")
    private Integer postId;

    /**
     * 父评论ID
     */
    @ApiModelProperty(value = "父评论ID")
    private Integer parentCommentId;

    /**
     * 点赞数
     */
    @ApiModelProperty(value = "点赞数")
    private Integer likeCount;

    /**
     * 回复数
     */
    @ApiModelProperty(value = "回复数")
    private Integer replyCount;
}
