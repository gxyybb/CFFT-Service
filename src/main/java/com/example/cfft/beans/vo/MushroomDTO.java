package com.example.cfft.beans.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.example.cfft.beans.Location;
import com.example.cfft.beans.MushroomImg;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class MushroomDTO {
    /**
     * ID
     */

    @ApiModelProperty(value = "ID")
    private Integer mushroomId;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String mushroomName;

    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID")
    private String category;

    /**
     * 图片URL
     */
    @ApiModelProperty(value = "图片URL")
    private List<MushroomImg> mushroomImages;

    /**
     * 是否能食用(不可以0，可以1，其他3)
     */
    @ApiModelProperty(value = "是否能食用(不可以0，可以1，其他3)")
    private Integer isEat;

    /**
     * 分布地点
     */
    @ApiModelProperty(value = "分布地点")
    private String mushroomLocation;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String mushroomDesc;

    /**
     * 是否有毒(不含毒0，有毒1，未知2)
     */
    @ApiModelProperty(value = "是否有毒(不含毒0，有毒1，未知2)")
    private Integer isPoison;

    private List<Location> locations;

}
