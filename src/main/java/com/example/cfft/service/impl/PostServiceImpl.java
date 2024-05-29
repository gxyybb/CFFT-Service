package com.example.cfft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cfft.beans.Post;
import com.example.cfft.beans.PostImg;
import com.example.cfft.beans.User;
import com.example.cfft.beans.vo.PostDTO;
import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.mapper.PostMapper;
import com.example.cfft.service.PostImgService;
import com.example.cfft.service.PostService;
import com.example.cfft.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService{
    @Autowired
    private PostImgService postImgService;
    @Autowired
    private UserService userService;
    public PostDTO convertToDTO(Post post){
        PostDTO postDTO = new PostDTO();
        BeanUtils.copyProperties(post, postDTO);
        List<PostImg> postImgList = postImgService.list(new QueryWrapper<PostImg>().eq("post_id", post.getPostId()));
        List<String> imgList = new ArrayList<>();
        postDTO.setImg(imgList);
        for (PostImg postImg : postImgList) {
            imgList.add(PathUtil.convertToHttpUrl(postImg.getUrl()));
        }
        User byId = userService.getById(postDTO.getUserId());
        postDTO.setUserName(byId.getUsername());
        if (byId.getUserImage() != null && byId.getUserImage().length() > 7) {
            postDTO.setUserImg(PathUtil.convertToHttpUrl(byId.getUserImage()));
        }
        return postDTO;
    }
}
