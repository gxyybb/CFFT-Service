package com.example.cfft.beans.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
@Data
public class UserRO {
    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户token")
    private String token;

    private Integer userId;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String username;

    /**
     * 电子邮件
     */
    @ApiModelProperty(value = "电子邮件")
    private String email;

    /**
     * 性别( 'Male', 'Female', 'Other' )
     */
    @ApiModelProperty(value = "性别")
    private String gender;

    /**
     * 出生日期
     */
    @ApiModelProperty(value = "出生日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String birthdate;

    /**
     * 地址
     */
    @ApiModelProperty(value = "地址")
    private String address;

    /**
     * 个人简介
     */
    @ApiModelProperty(value = "个人简介")
    private String bio;

    /**
     * 用户等级
     */
    @ApiModelProperty(value = "用户等级")
    private Integer level;

    @ApiModelProperty(value = "用户图片")
    private MultipartFile userImageFile;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "背景图片")
    private MultipartFile backImgFile;

    // Getters and Setters
    // ...
}
