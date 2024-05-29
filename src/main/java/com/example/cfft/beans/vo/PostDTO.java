package com.example.cfft.beans.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
public class PostDTO{
    @ApiModelProperty(value = "帖子ID")
    private Integer postId;

    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    private String title;

    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;

    /**
     * 发布时间
     */
    @ApiModelProperty(value = "发布时间")
    private Date publishTime;

    private List<String> img;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    private Integer userId;
    private String userName;
    private String userImg;
    /**
     * 点赞数
     */
    @ApiModelProperty(value = "点赞数")
    private Integer likeCount;

    /**
     * 浏览数
     */
    @ApiModelProperty(value = "浏览数")
    private Integer viewCount;

    /**
     * 评论数
     */
    @ApiModelProperty(value = "评论数")
    private Integer commentCount;

}
