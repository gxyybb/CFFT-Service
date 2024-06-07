package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;


import java.io.Serializable;
import java.util.Date;
import lombok.Data;


@Data

public class User implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)

    private Integer userId;

    /**
     * 用户名
     */

    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 电子邮件
     */
    private String email;

    /**
     * 注册时间
     */
    private Date registrationTime;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别( 'Male', 'Female', 'Other' )
     */
    private Object gender;

    /**
     * 出生日期
     */
    private Date birthdate;

    /**
     * 地址
     */
    private String address;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 用户等级
     */
    private Integer level;

    private String userImage;

    private String nickName;
    private String backImg;
}
