package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;


@Data
public class Category{
    /**
     * ID
     */
    @TableId("category_id")
    private Integer categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类级别
     */
    private Integer categoryLevel;

    /**
     * 上级分类
     */
    private String  categoryParent;

    /**
     * 根分类
     */
    private Integer categoryRoot;

    /**
     * 分类图标
     */
    private String categoryImg;
    private String categoryDesc;
}

