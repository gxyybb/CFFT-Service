package com.example.cfft.beans.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "帖子数据传输对象")
public class PostDTO {
    @Schema(description = "帖子ID")
    private Integer postId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "发布时间")
    private Date publishTime;

    @Schema(description = "图片列表")
    private List<String> img;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户头像URL")
    private String userImg;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "浏览数")
    private Integer viewCount;

    @Schema(description = "评论数")
    private Integer commentCount;
}
