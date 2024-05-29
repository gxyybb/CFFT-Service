package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class Category{
    /**
     * ID
     */
    @ApiModelProperty(value = "ID")
    @TableId("category_id")
    private Integer categoryId;

    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    /**
     * 分类级别
     */
    @ApiModelProperty(value = "分类级别")
    private Integer categoryLevel;

    /**
     * 上级分类
     */
    @ApiModelProperty(value = "上级分类")
    private String  categoryParent;

    /**
     * 根分类
     */
    @ApiModelProperty(value = "根分类")
    private Integer categoryRoot;

    /**
     * 分类图标
     */
    @ApiModelProperty(value = "分类图标")
    private String categoryImg;
    private String categoryDesc;
}

