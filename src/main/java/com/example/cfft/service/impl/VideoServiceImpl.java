package com.example.cfft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cfft.beans.Video;
import com.example.cfft.service.VideoService;
import com.example.cfft.mapper.VideoMapper;
import org.springframework.stereotype.Service;

/**
* @author 14847
* @description 针对表【video】的数据库操作Service实现
* @createDate 2024-05-02 14:59:00
*/
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video>
    implements VideoService{

}




