package com.example.cfft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cfft.beans.VideoComments;
import com.example.cfft.service.VideoCommentsService;
import com.example.cfft.mapper.VideoCommentsMapper;
import org.springframework.stereotype.Service;

/**
* @author 14847
* @description 针对表【video_comments】的数据库操作Service实现
* @createDate 2024-05-09 09:30:16
*/
@Service
public class VideoCommentsServiceImpl extends ServiceImpl<VideoCommentsMapper, VideoComments>
    implements VideoCommentsService{

}




