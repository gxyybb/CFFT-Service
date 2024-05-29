package com.example.cfft.beans.vo;


import lombok.Data;

@Data
public class UserM {

    private String bio;

    /**
     * 用户等级
     */

    private Integer level;
    private String gender;



    private String nickName;

    private String username;
    private Integer userId;


    private String userImage;
}
