package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.util.Date;
import lombok.Data;


@Data
public class Post{
    /**
     * 帖子ID
     */
    @TableId(type = IdType.AUTO)
    private Integer postId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 发布时间
     */
    private Date publishTime;

    private String img;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 评论数
     */
    private Integer commentCount;
}
