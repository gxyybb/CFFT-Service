package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;


@Data
@ApiModel(description = "用户实体类")
public class User implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String username;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String password;

    /**
     * 电子邮件
     */
    @ApiModelProperty(value = "电子邮件")
    private String email;

    /**
     * 注册时间
     */
    @ApiModelProperty(value = "注册时间")
    private Date registrationTime;

    /**
     * 头像
     */
    @ApiModelProperty(value = "头像url")
    private String avatar;

    /**
     * 性别( 'Male', 'Female', 'Other' )
     */
    @ApiModelProperty(value = "性别")
    private Object gender;

    /**
     * 出生日期
     */
    @ApiModelProperty(value = "出生日期")
    private Date birthdate;

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

    private String userImage;

    private String nickName;
    private String backImg;
}
