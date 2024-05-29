package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;

/**
 * (PostImg)表实体类
 *
 * @author makejava
 * @since 2024-02-29 21:28:52
 */
@Data
public class PostImg {
    @TableId(type = IdType.AUTO)
    private Integer imageId;

    private Integer postId;

    private String url;



}

