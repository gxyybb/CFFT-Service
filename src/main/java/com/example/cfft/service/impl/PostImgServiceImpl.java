package com.example.cfft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cfft.beans.PostImg;
import com.example.cfft.mapper.PostImgMapper;
import com.example.cfft.service.PostImgService;
import org.springframework.stereotype.Service;

@Service
public class PostImgServiceImpl extends ServiceImpl<PostImgMapper, PostImg> implements PostImgService{
}
