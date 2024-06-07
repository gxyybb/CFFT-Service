package com.example.cfft.beans.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "评论视图对象")
public class CommentVO {
    @Schema(description = "评论ID")
    private Integer commentId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "用户头像URL")
    private String userImage;

    @Schema(description = "发布时间")
    private Date publishTime;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "帖子ID")
    private Integer typeId;

    @Schema(description = "父评论ID")
    private Integer parentCommentId;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "回复数")
    private Integer replyCount;
}
