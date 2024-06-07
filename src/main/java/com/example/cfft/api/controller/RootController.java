package com.example.cfft.api.controller;

import com.example.cfft.beans.User;
import com.example.cfft.beans.vo.UserRO;
import com.example.cfft.common.utils.*;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "管理员操作", description = "包含所有管理员相关的用户操作")
@RestController
@RequestMapping("/root")
public class RootController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserController userController;

    @Operation(summary = "获取所有用户")
    @CrossOrigin(origins = "*")
    @GetMapping("all")
    public ResultVO getAllUser(@Parameter(description = "管理员Token", required = true) @RequestParam("token") String token) {
        List<User> list = userService.list();
        List<User> updatedList = list.stream().map(user -> {
            user.setUserImage(PathUtil.convertToHttpUrl(user.getUserImage()));
            user.setBackImg(PathUtil.convertToHttpUrl(user.getBackImg()));
            return user;
        }).collect(Collectors.toList());
        return ResultVO.success(updatedList);
    }

    @Operation(summary = "创建用户")
    @CrossOrigin(origins = "*")
    @PostMapping("createUser")
    public ResultVO createUser(@Parameter(description = "用户名", required = true) @RequestParam("userName") String username,
                               @Parameter(description = "密码", required = true) @RequestParam("password") String password) {
        return userController.register(username, password);
    }

    @Operation(summary = "更新用户信息")
    @CrossOrigin(origins = "*")
    @PutMapping("user")
    public ResultVO updateUser(@ModelAttribute UserRO userRO, RedirectAttributes redirectAttributes) {
        try {
            Optional<Integer> userId = Optional.ofNullable(userRO.getUserId());
            if (userId.isEmpty()) {
                return ResultVO.failure("User ID is required");
            }

            String token = TokenUtil.generateToken(String.valueOf(userId.get()));
            userRO.setToken(token);

            ResultVO result = userController.update(userRO, redirectAttributes);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.failure("Update failed: " + e.getMessage());
        }
    }

    @Operation(summary = "重置用户密码")
    @CrossOrigin(origins = "*")
    @PutMapping("/reset")
    public ResultVO resetPassword(@Parameter(description = "用户ID", required = true) @RequestParam("userId") Integer userId) {
        User user = new User();
        user.setUserId(userId);
        user.setPassword(MD5Utils.string2MD5(Static.DEFAULT_PASSWORD));
        userService.updateById(user);
        return ResultVO.success();
    }

    @Operation(summary = "删除用户")
    @CrossOrigin(origins = "*")
    @DeleteMapping
    public ResultVO delete(@Parameter(description = "用户ID", required = true) @RequestParam("userId") Integer userId) {
        User user = userService.getById(userId);
        Optional<User> optionalUser = Optional.ofNullable(user);

        optionalUser.ifPresent(u -> {
            String avatar = u.getAvatar();
            if (avatar != null && !avatar.isEmpty()) {
                FileUtil.deleteFile(avatar);
            }
        });

        boolean removed = userService.removeById(userId);
        if (removed) {
            return ResultVO.success();
        } else {
            return ResultVO.failure("Failed to delete user");
        }
    }
}
