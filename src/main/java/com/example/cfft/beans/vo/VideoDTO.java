package com.example.cfft.beans.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "视频数据传输对象")
public class VideoDTO {
    @Schema(description = "视频ID")
    @TableId(type = IdType.AUTO)
    private Integer videoid;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "文件名")
    private String filename;

    @Schema(description = "文件路径")
    private String filepath;

    @Schema(description = "文件类型")
    private String filetype;

    @Schema(description = "上传时间")
    private Date uploadtime;

    @Schema(description = "时长")
    private Integer duration;

    @Schema(description = "封面图片URL")
    private String coverimage;

    @Schema(description = "观看次数")
    private Integer views;

    @Schema(description = "点赞数")
    private Integer likes;

    @Schema(description = "评论数")
    private Integer comments;

    @Schema(description = "评论列表")
    private List<CommentVO> commentVOS;
}
