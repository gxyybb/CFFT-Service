package com.example.cfft.beans.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户信息")
public class UserM {

    @Schema(description = "用户简介")
    private String bio;

    @Schema(description = "用户等级")
    private Integer level;

    @Schema(description = "用户性别")
    private String gender;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户头像URL")
    private String userImage;
}
