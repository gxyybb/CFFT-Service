package com.example.cfft.api.controller;

import com.example.cfft.beans.User;
import com.example.cfft.beans.vo.UserRO;
import com.example.cfft.common.utils.*;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.UserService;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/root")
public class RootController {
    @Autowired
    public UserService userService;
    @Autowired
    public UserController userController;



    @CrossOrigin("*")
    @GetMapping("all")
    public ResultVO getAllUser(@RequestParam("token")String token) {
        List<User> list = userService.list();
        List<User> updatedList = list.stream().map(user -> {
            // Convert image paths to HTTP URLs
            user.setUserImage(PathUtil.convertToHttpUrl(user.getUserImage()));
            user.setBackImg(PathUtil.convertToHttpUrl(user.getBackImg()));
            return user;
        }).collect(Collectors.toList());
        return ResultVO.success(updatedList);
    }

    @CrossOrigin("*")
    @PostMapping("createUser")
    public ResultVO createUser(@RequestParam("userName") String username,@RequestParam("password")String password){
        return userController.register(username,password);
    }
    @CrossOrigin("*")
    @PutMapping("user")
    public ResultVO updateUser(@ModelAttribute UserRO userRO, RedirectAttributes redirectAttributes) {
        try {
            // 获取用户ID并生成Token
            Optional<Integer> userId = Optional.ofNullable(userRO.getUserId());
            if (userId.isEmpty()) {
                return ResultVO.failure("User ID is required");
            }

            String token = TokenUtil.generateToken(String.valueOf(userId.get()));
            userRO.setToken(token);

            // 调用 update 方法更新用户信息
            ResultVO result = userController.update(userRO, redirectAttributes);

            // 返回 update 方法的结果
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.failure("Update failed: " + e.getMessage());
        }
    }
    @CrossOrigin("*")
    @PutMapping("/reset")
    public ResultVO resetPassword(@RequestParam("userId")Integer userId){
        User user = new User();
        user.setPassword(MD5Utils.string2MD5(Static.DEFAULT_PASSWORD));
        userService.updateById(user);
        return ResultVO.success();
    }
    @DeleteMapping
    @CrossOrigin("*")
    public ResultVO delete(@RequestParam("userId") Integer userId) {
        // 获取用户信息
        User user = userService.getById(userId);
        Optional<User> optionalUser = Optional.ofNullable(user);

        // 如果用户存在，则删除头像文件
        optionalUser.ifPresent(u -> {
            String avatar = u.getAvatar();
            if (avatar != null && !avatar.isEmpty()) {
                FileUtil.deleteFile(avatar);
            }
        });

        // 删除用户记录
        boolean removed = userService.removeById(userId);
        if (removed) {
            return ResultVO.success();
        } else {
            return ResultVO.failure("Failed to delete user");
        }
    }


}
