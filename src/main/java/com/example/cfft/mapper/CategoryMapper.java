package com.example.cfft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cfft.beans.Category;
import org.apache.ibatis.annotations.Select;

public interface CategoryMapper extends BaseMapper<Category>{
    public String getCategorysByCategoryId(Integer mushroomId);
}
