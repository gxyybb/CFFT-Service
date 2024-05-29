package com.example.cfft.beans;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("mushroom_img")
public class MushroomImg {

    @TableId(value = "img_id", type = IdType.AUTO)
    private Integer imgId;

    private Integer mushroomId;

    private String imgUrl;
}
