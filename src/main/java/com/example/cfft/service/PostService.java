package com.example.cfft.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.cfft.beans.Post;
import com.example.cfft.beans.PostImg;
import com.example.cfft.beans.vo.PostDTO;
import com.example.cfft.common.utils.PathUtil;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public interface PostService extends IService<Post>{
    public PostDTO convertToDTO(Post post);
}
