package com.example.cfft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.cfft.beans.User;
import com.example.cfft.common.utils.MD5Utils;
import com.example.cfft.common.utils.Static;
import com.example.cfft.common.utils.TokenUtil;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.mapper.UserMapper;
import com.example.cfft.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;


@Service

public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{
    @Autowired
    private UserMapper userMapper;


    //    @Autowired
//    private UserLoginHistoryServiceImpl userLoginHistoryService;
    @Override
    public ResultVO login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user != null && MD5Utils.passwordIsTrue(password, user.getPassword())) {
            String token = TokenUtil.generateToken(String.valueOf(user.getUserId()));
            //验证成功
            return ResultVO.success(token);
        } else {
            if (user == null) {
                return ResultVO.error("用户名或密码错误");
            }
            //验证失败
            return ResultVO.error("用户名或密码错误");
        }
    }

    @Override
    @Transactional
    public ResultVO register(String username, String password) {
        synchronized (this) {
            // 查询是否被注册
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            User user = userMapper.selectOne(queryWrapper);
            if (user == null) {
                User newUser = new User();
                newUser.setUsername(username);

                String md5Pwd = MD5Utils.string2MD5(password);
                newUser.setPassword(md5Pwd);

                // 设置随机昵称
                newUser.setNickName(generateRandomNickname());

                int i = userMapper.insert(newUser);
                newUser.setAvatar(Static.BASE_URL_FOR_USER + newUser.getUserId() + "/");
                newUser.setBio("这个用户很懒，什么也没有留下");
                newUser.setBirthdate(new Date());
                newUser.setAddress("石家庄");
                userMapper.updateById(newUser);

                if (i > 0) {
                    return ResultVO.success(TokenUtil.generateToken(String.valueOf(newUser.getUserId())));
                } else {
                    return ResultVO.error("注册失败");
                }
            } else {
                return ResultVO.error("用户名已存在");
            }
        }
    }

    private String generateRandomNickname() {
        List<String> adjectives = Arrays.asList("快乐的","勇敢的","机智的","聪明的","温暖的","可爱的","友好的","勤劳的","幽默的","善良的"
        );
        List<String> nouns = Arrays.asList("小猫","小狗","狮子","老虎","熊猫","兔子","鹦鹉","天鹅","海豚","长颈鹿"
        );

        Random random = new Random();
        String adjective = adjectives.get(random.nextInt(adjectives.size()));
        String noun = nouns.get(random.nextInt(nouns.size()));

        return adjective + " " + noun;
    }




}


