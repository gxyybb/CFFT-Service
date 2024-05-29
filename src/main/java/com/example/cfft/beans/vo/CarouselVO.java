package com.example.cfft.beans.vo;

import com.example.cfft.beans.Carousel;
import lombok.Data;

import java.util.List;
@Data
public class CarouselVO {
    private Integer num;
    List<Carousel> carouses;
}
