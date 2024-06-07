package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;


@Data
public class Mushroom{
    /**
     * ID
     */

    @TableId("mushroom_id")
    private Integer mushroomId;

    /**
     * 名称
     */
    private String mushroomName;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 图片URL
     */
    private String mushroomImage;

    /**
     * 是否能食用(不可以0，可以1，其他3)
     */
    private Integer isEat;

    /**
     * 分布地点
     */
    private String mushroomLocation;

    /**
     * 描述
     */
    private String mushroomDesc;

    /**
     * 是否有毒(不含毒0，有毒1，未知2)
     */
    private Integer isPoison;

    /**
     * 3d模型
     */
    private String mushroom3d;
}
