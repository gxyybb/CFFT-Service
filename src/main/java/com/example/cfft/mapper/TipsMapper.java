package com.example.cfft.mapper;

import com.example.cfft.beans.Tips;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 14847
* @description 针对表【tips】的数据库操作Mapper
* @createDate 2024-05-15 20:20:14
* @Entity generator.domain.Tips
*/
public interface TipsMapper extends BaseMapper<Tips> {
    Tips selectRandomTips(Integer categoryListMapper);
}




