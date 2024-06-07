package com.example.cfft.beans.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "视频第一级数据传输对象")
public class VideoFirst {
    @Schema(description = "视频ID")
    private Integer videoid;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "时长")
    private Date duration;

    @Schema(description = "封面图片URL")
    private String coverimage;

    @Schema(description = "观看次数")
    private Integer views;

    @Schema(description = "点赞数")
    private Integer likes;

    @Schema(description = "评论数")
    private Integer comments;
}
