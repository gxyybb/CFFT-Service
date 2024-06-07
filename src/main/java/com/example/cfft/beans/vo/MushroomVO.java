package com.example.cfft.beans.vo;

import com.example.cfft.beans.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "蘑菇视图对象")
public class MushroomVO {
    @Schema(description = "蘑菇ID")
    private Integer mushroomId;

    @Schema(description = "蘑菇名称")
    private String mushroomName;

    @Schema(description = "分类ID")
    private String category;

    @Schema(description = "是否能食用（0：不可以，1：可以，3：其他）")
    private String isEat;

    @Schema(description = "分布地点")
    private String mushroomLocation;

    @Schema(description = "蘑菇描述")
    private String mushroomDesc;

    @Schema(description = "是否有毒（0：不含毒，1：有毒，2：未知）")
    private String isPoison;

    @Schema(description = "蘑菇3D模型URL")
    private String mushroom3d;

    @Schema(description = "分布地点列表")
    private List<Location> locations;
}
