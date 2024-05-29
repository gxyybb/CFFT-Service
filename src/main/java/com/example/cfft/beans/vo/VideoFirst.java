package com.example.cfft.beans.vo;

import lombok.Data;

import java.util.Date;
@Data
public class VideoFirst {
    private Integer videoid;

    /**
     *
     */
    private String title;

    /**
     *
     */
    private String description;
    private Date duration;
    private String coverimage;

    /**
     *
     */
    private Integer views;

    /**
     *
     */
    private Integer likes;

    /**
     *
     */
    private Integer comments;
}
