package com.example.cfft.beans.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserVOF {
    private String username;

    private String userImage;

    private String address;

    private List<String> texts;
    private Integer time;

}
