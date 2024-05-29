package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author gxy
 * @since 2024-03-28
 */
@Data
@ApiModel(value = "Carousel对象", description = "")
public class Carousel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "carousel_id", type = IdType.AUTO)
    private Integer carouselId;
    private Integer postId;
    private String title;

    private String description;

    private String imageUrl;

    private String link;

    private Boolean isDisplayed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    @Override
    public String toString() {
        return "Carousel{" +
            "carouselId = " + carouselId +
            ", title = " + title +
            ", description = " + description +
            ", imageUrl = " + imageUrl +
            ", link = " + link +
            ", isDisplayed = " + isDisplayed +
            ", createdAt = " + createdAt +
            ", updatedAt = " + updatedAt +
        "}";
    }
}
