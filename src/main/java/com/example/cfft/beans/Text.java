package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * (Text)实体类
 *
 * @author makejava
 * @since 2024-04-08 19:53:30
 */
@Data
public class Text implements Serializable {
    @TableId
    private Integer textId;
    private Integer userId;

    private String text1;

    private String text2;

    private String text3;



}

