package com.example.cfft.beans.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class VideoDTO {
    @TableId(type = IdType.AUTO)
    private Integer videoid;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件路径
     */
    private String filepath;

    /**
     * 文件类型
     */
    private String filetype;

    /**
     * 上传时间
     */
    private Date uploadtime;

    /**
     * 时长
     */
    private Integer duration;

    /**
     * 封面图片
     */
    private String coverimage;

    /**
     * 观看次数
     */
    private Integer views;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 评论数
     */
    private Integer comments;

    private List<CommentVO> commentVOS;


}
