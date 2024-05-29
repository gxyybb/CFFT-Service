package com.example.cfft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cfft.beans.Category;

import com.example.cfft.common.utils.FileUtil;
import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.utils.Static;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.mapper.CategoryMapper;
import com.example.cfft.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public ResultVO saveImage(Integer categoryId, MultipartFile imageFile) {
        // 使用 Optional 检查类别是否存在
        Optional<Category> categoryOptional = Optional.ofNullable(categoryMapper.selectById(categoryId));

        if (categoryOptional.isEmpty()) {
            return ResultVO.error("失败", "未找到对应的类别");
        }

        // 定义文件上传目录
        String uploadDir = Static.categoryImagePath;

        try {
            // 保存文件并获取文件路径
            String filePath = Optional.ofNullable(FileUtil.saveFile(uploadDir, imageFile))
                    .orElseThrow(() -> new IOException("文件保存失败"));

            // 获取类别并更新图片路径
            Category category = categoryOptional.get();
            category.setCategoryImg(filePath);
            categoryMapper.updateById(category);

            return ResultVO.success();
        } catch (IOException e) {
            // 记录错误日志
            log.error("上传图片失败", e);
            return ResultVO.error("失败", "上传失败，请查看日志获取详细信息");
        }
    }



}
