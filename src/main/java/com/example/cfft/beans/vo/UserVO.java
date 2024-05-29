package com.example.cfft.beans.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
@Data
public class UserVO {
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

    private String userImage;
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



    private String nickName;
}
