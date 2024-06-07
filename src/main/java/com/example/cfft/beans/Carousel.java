package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class Carousel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "carousel_id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Integer carouselId;
    @Schema(description = "帖子ID")
    private Integer postId;
    @Schema(description = "标题")
    private String title;

    private String description;

    private String imageUrl;

    private String link;

    private Boolean isDisplayed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;



}
