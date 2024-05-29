package com.example.cfft.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.cfft.beans.Category;
import com.example.cfft.common.vo.ResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface CategoryService extends IService<Category>{
    ResultVO saveImage(Integer categoryId, MultipartFile imageFile);
}
