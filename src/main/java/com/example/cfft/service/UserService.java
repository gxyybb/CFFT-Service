package com.example.cfft.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.cfft.beans.User;
import com.example.cfft.common.vo.ResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService extends IService<User>{
    ResultVO login(String username, String password);

    ResultVO register(String username, String password);

}
