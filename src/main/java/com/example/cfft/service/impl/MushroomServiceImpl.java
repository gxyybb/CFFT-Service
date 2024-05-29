package com.example.cfft.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cfft.beans.Mushroom;
import com.example.cfft.beans.MushroomImg;

import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.mapper.MushroomMapper;
import com.example.cfft.service.MushroomImgService;
import com.example.cfft.service.MushroomService;
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
import java.util.UUID;


@Service
public class MushroomServiceImpl extends ServiceImpl<MushroomMapper, Mushroom> implements MushroomService{
    @Autowired
    private MushroomMapper mushroomMapper;
    @Autowired
    private MushroomImgService mushroomImgService;
    @Override
    public ResultVO saveImage(Integer mushroomId, MultipartFile imageFile) {
        Mushroom mushroom = mushroomMapper.selectById(mushroomId);

        if (mushroom == null) {
            return ResultVO.error("失败", "未找到对应的蘑菇");
        }

        try {
            String uploadDir = mushroom.getMushroomImage();

            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 使用UUID作为文件名确保唯一性
            String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());

            Path imgFilePathInUploadDir = uploadPath.resolve(fileName);

            // 使用try-with-resources确保InputStream被正确关闭
            try (InputStream inputStream = imageFile.getInputStream()) {
                Files.copy(inputStream, imgFilePathInUploadDir, StandardCopyOption.REPLACE_EXISTING);
            }

            String imageUrl = imgFilePathInUploadDir.toString();
            MushroomImg mushroomImg = new MushroomImg();
            mushroomImg.setMushroomId(mushroomId);
            mushroomImg.setImgUrl(imageUrl);
            mushroomImgService.save(mushroomImg);

            return ResultVO.success();
        } catch (IOException e) {
            // 记录错误日志
            log.error("上传图片失败", e);
            return ResultVO.error("失败", "上传失败，请查看日志获取详细信息");
        }
    }




}
