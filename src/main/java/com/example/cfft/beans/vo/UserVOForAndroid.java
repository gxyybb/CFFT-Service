package com.example.cfft.beans.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "用户视图对象用于Android")
public class UserVOForAndroid {
    @Schema(description = "用户ID")
    @TableId(type = IdType.AUTO)
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "电子邮件")
    private String email;

    @Schema(description = "注册时间")
    private Date registrationTime;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "性别 ('Male', 'Female', 'Other')")
    private Object gender;

    @Schema(description = "出生日期")
    private Date birthdate;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "用户等级")
    private Integer level;

    @Schema(description = "用户头像URL")
    private String userImage;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "背景图片URL")
    private String backImg;

    @Schema(description = "点赞数量")
    private long likeCount;

    @Schema(description = "帖子赞数量")
    private Integer postZanCount;

    @Schema(description = "评论赞数量")
    private Integer commentZanCount;

    @Schema(description = "评论回复数量")
    private Integer commentCommentCount;
}
