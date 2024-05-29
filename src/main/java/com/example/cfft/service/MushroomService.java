package com.example.cfft.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.cfft.beans.Mushroom;
import com.example.cfft.common.vo.ResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface MushroomService extends IService<Mushroom>{
    public ResultVO saveImage(Integer mushroomId, MultipartFile imageFile);


}
