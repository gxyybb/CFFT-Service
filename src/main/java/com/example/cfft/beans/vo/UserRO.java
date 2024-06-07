package com.example.cfft.beans.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "用户请求对象")
public class UserRO {

    @Schema(description = "用户令牌")
    private String token;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "电子邮件")
    private String email;

    @Schema(description = "性别（'Male', 'Female', 'Other'）")
    private String gender;

    @Schema(description = "出生日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String birthdate;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "用户等级")
    private Integer level;

    @Schema(description = "用户头像文件")
    private MultipartFile userImageFile;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "背景图片文件")
    private MultipartFile backImgFile;

}
