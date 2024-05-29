package com.example.cfft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cfft.beans.Comment;
import com.example.cfft.mapper.CommentMapper;
import com.example.cfft.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService{
}
