package com.example.cfft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cfft.beans.CategoryList;
import com.example.cfft.service.CategoryListService;
import com.example.cfft.mapper.CategoryListMapper;
import org.springframework.stereotype.Service;

/**
* @author 14847
* @description 针对表【category_list】的数据库操作Service实现
* @createDate 2024-05-15 20:20:06
*/
@Service
public class CategoryListServiceImpl extends ServiceImpl<CategoryListMapper, CategoryList>
    implements CategoryListService{

}




