package com.example.cfft.api.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.cfft.beans.*;
import com.example.cfft.beans.vo.*;
import com.example.cfft.common.utils.FileUtil;
import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.utils.Static;
import com.example.cfft.common.utils.TokenUtil;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.mapper.CommentMapper;
import com.example.cfft.mapper.LikeMapper;
import com.example.cfft.mapper.PostMapper;
import com.example.cfft.service.*;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.example.cfft.common.utils.Static.BASE_URL_FOR_USER;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TextService textService;

    @Autowired
    private LikeService likeService;
    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostController postController;

    @Autowired
    private CommentService commentService;

    /**
     * 管理员登录
     */

    @CrossOrigin(origins = "*")
    @PostMapping("/loginMaster")
    public ResultVO loginMaster(@RequestParam("username") String username,
                                @RequestParam("password") String password) {
        if (username.equals("admin") && password.equals("admin")) {
            return ResultVO.success(TokenUtil.generateToken(String.valueOf(123456789)));
        } else {
            return ResultVO.error("登录失败");
        }
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return ResultVO
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/login")
    public ResultVO login(@RequestParam("username") String username,
                          @RequestParam("password") String password) {
        return userService.login(username, password);
    }

    /**
     * 更新用户头像
     * @param img 用户上传的头像图片
     * @param token 用户的身份令牌
     * @return ResultVO
     */
    @CrossOrigin(origins = "*")
    @PutMapping("/updateImg")
    public ResultVO updateImg(@RequestParam("img") MultipartFile img,
                              @RequestParam("token") String token) {
        Integer userId = TokenUtil.getUserIdFromToken(token);
        User byId = userService.getById(userId);

        if (byId.getUserImage() != null) {
            FileUtil.deleteFile(byId.getUserImage());
        }
        String s = FileUtil.saveFile(byId.getAvatar(), img);
        byId.setUserImage(s);
        userService.updateById(byId);
        return ResultVO.success("头像上传成功");
    }
    @CrossOrigin(origins = "*")
    @PutMapping("/updateBackImg")
    public ResultVO updateBackImg(@RequestParam("img") MultipartFile img,
                              @RequestParam("token") String token) {
        Integer userId = TokenUtil.getUserIdFromToken(token);
        User byId = userService.getById(userId);

        if (byId.getUserImage() != null) {
            FileUtil.deleteFile(byId.getUserImage());
        }
        String s = FileUtil.saveFile(byId.getAvatar(), img);
        byId.setBackImg(s);
        userService.updateById(byId);
        return ResultVO.success("背景上传成功");
    }

    /**
     * 更新用户信息
     * @param token 用户的身份令牌
     * @param column 要更新的用户信息列
     * @param info 更新的信息
     * @return ResultVO
     */
    @CrossOrigin(origins = "*")
    @PutMapping("/updateInfo")
    public ResultVO updateInformation(@RequestParam("token") String token,
                                      @RequestParam("column") UserInfo column,
                                      @RequestParam("info") String info) {
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);

        // 构建更新条件
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userIdFromToken);

        // 设置更新的列和值
        switch (column) {
            case username:
                updateWrapper.set("username", info);
                break;
            case email:
                updateWrapper.set("email", info);
                break;
            case gender:
                updateWrapper.set("gender", info);
                break;
            case birthdate:
                // 假设日期格式为 "yyyy-MM-dd"
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = dateFormat.parse(info);
                    updateWrapper.set("birthdate", date);
                } catch (ParseException e) {
                    return ResultVO.error("请按照yyyy-mm-dd的格式传日期");
                }
                break;
            case address:
                updateWrapper.set("address", info);
                break;
            case bio:
                updateWrapper.set("bio", info);
                break;
            case nickName:
                updateWrapper.set("nick_name", info);
                break;
            default:
                return ResultVO.error("无效的列名");
        }

        // 执行更新操作
        boolean update = userService.update(updateWrapper);
        return update ? ResultVO.success() : ResultVO.error();
    }
    @CrossOrigin("*")
    @PutMapping
    @Transactional

    public ResultVO update(@ModelAttribute UserRO userRO, RedirectAttributes redirectAttributes) {
        try {
            System.out.println(userRO.toString());
            Integer userIdFromToken = TokenUtil.getUserIdFromToken(userRO.getToken());
            if (userIdFromToken == null) {
                return ResultVO.failure("Invalid token");
            }

            Optional<User> optionalUser = Optional.ofNullable(userService.getById(userIdFromToken));
            if (optionalUser.isEmpty()) {
                return ResultVO.failure("User not found");
            }

            User byId = optionalUser.get();

            // 解析和验证 birthdate
            Date birthdate = null;
            if (userRO.getBirthdate() != null) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    birthdate = formatter.parse(userRO.getBirthdate());
                } catch (ParseException e) {
                    return ResultVO.failure("Invalid birthdate format. Please use yyyy-MM-dd.");
                }
            }

            // 复制属性到现有的 User 对象
            Optional.ofNullable(userRO.getBio()).ifPresent(byId::setBio);
            Optional.ofNullable(userRO.getGender()).ifPresent(byId::setGender);
            Optional.ofNullable(userRO.getNickName()).ifPresent(byId::setNickName);

            // 处理背景图片文件
            Optional.ofNullable(userRO.getBackImgFile()).ifPresent(file -> {
                if (byId.getBackImg() != null && !byId.getBackImg().startsWith(Static.DEFAULT_USER)) {
                    FileUtil.deleteFile(byId.getBackImg());
                }
                String backImgPath = FileUtil.saveFile(BASE_URL_FOR_USER + byId.getUserId(), file);
                byId.setBackImg(backImgPath);
            });

            // 处理用户头像文件
            Optional.ofNullable(userRO.getUserImageFile()).ifPresent(file -> {
                if (byId.getUserImage() != null && !byId.getUserImage().startsWith(Static.DEFAULT_USER)) {
                    FileUtil.deleteFile(byId.getUserImage());
                }
                String userImagePath = FileUtil.saveFile(BASE_URL_FOR_USER + byId.getUserId() + "/back", file);
                byId.setUserImage(userImagePath);
            });

            // 更新用户出生日期
            byId.setBirthdate(birthdate);
            userService.updateById(byId);

            return ResultVO.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.failure("Update failed: " + e.getMessage());
        }
    }

    @ExceptionHandler(MultipartException.class)
    public ResultVO handleMultipartException(MultipartException ex) {
        ex.printStackTrace();
        return ResultVO.failure("File upload error: " + ex.getMessage());
    }

    /**
     * 获取用户的详细信息
     * @param token 用户的身份令牌
     * @return ResultVO
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/first")
    public ResultVO getUserVOF(@RequestParam("token") String token) {
        try {

            Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
            User byId = userService.getById(userIdFromToken);
            UserVOF userVOF = new UserVOF();
            Date registrationTime = byId.getRegistrationTime(); // 从数据库获取时间
            // 获取当前时间
            Instant now = Instant.now();

            // 将Date转换为Instant
            Instant registrationInstant = registrationTime.toInstant();

            // 计算两个时间之间的差值
            Duration duration = Duration.between(registrationInstant, now);

            // 将Duration转换为天数
            int days = Math.toIntExact(duration.toDays());
            userVOF.setTime(days);

            BeanUtils.copyProperties(byId, userVOF);
            QueryWrapper<Text> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userIdFromToken);
            if (textService.exists(queryWrapper)) {
                Text texts = textService.getOne(queryWrapper);
                List<String> strs = new ArrayList<>();
                strs.add(texts.getText1());
                strs.add(texts.getText2());
                strs.add(texts.getText3());
                userVOF.setTexts(strs);
            }

            String userImage = userVOF.getUserImage();
            if (userImage != null) {
                String s = PathUtil.convertToHttpUrl(userImage);
                userVOF.setUserImage(s);
            }

            return ResultVO.success(userVOF);
        } catch (Exception e) {
            if (token.length() < 10) {
                System.out.println("token不合法");
                return ResultVO.error("token不合法");
            }
            return ResultVO.error("请求出错");
        }
    }

    /**
     * 设置用户文本信息
     * @param token 用户的身份令牌
     * @param text1 文本1
     * @param text2 文本2
     * @param text3 文本3
     * @return ResultVO
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/setTexts")
    public ResultVO setTexts(@RequestParam("token") String token,
                             @RequestParam("text1") String text1,
                             @RequestParam("text2") String text2,
                             @RequestParam("text3") String text3) {
        QueryWrapper<Text> textQueryWrapper = new QueryWrapper<>();
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        textQueryWrapper.eq("user_id", userIdFromToken);
        Text text = new Text();
        text.setUserId(userIdFromToken);
        text.setText1(text1);
        text.setText2(text2);
        text.setText3(text3);
        boolean exists = textService.exists(textQueryWrapper);
        if (exists) {
            UpdateWrapper<Text> textUpdateWrapper = new UpdateWrapper<>();
            textUpdateWrapper.eq("user_id", userIdFromToken);
            textService.update(text, textUpdateWrapper);
        } else {
            textService.save(text);
        }
        return ResultVO.success();
    }

    /**
     * 获取用户信息
     * @param token 用户的身份令牌
     * @return ResultVO
     */
    @CrossOrigin("*")
    @GetMapping
    public ResultVO get(@RequestParam("token") String token) {
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        if (userIdFromToken != null) {
            User byId = userService.getById(userIdFromToken);
            if (byId.getUserImage() != null) {
                byId.setUserImage(PathUtil.convertToHttpUrl(byId.getUserImage()));
            }
            if (byId.getBackImg() != null) {
                byId.setBackImg(PathUtil.convertToHttpUrl(byId.getBackImg()));
            }
            UserVOForAndroid userVOForAndroid = new UserVOForAndroid();
            BeanUtils.copyProperties(byId, userVOForAndroid);
            QueryWrapper<Like> likeQueryWrapper = new QueryWrapper<>();
            likeQueryWrapper.eq("user_id", userIdFromToken)
                    .eq("object_type", "Post");
            long count = likeService.count(likeQueryWrapper);
            userVOForAndroid.setLikeCount(count);
            Integer postZan = postMapper.getTotalLikesByUserId(userIdFromToken);
            userVOForAndroid.setPostZanCount(postZan);
            Integer comments = commentMapper.getTotalCommentsByUserId(userIdFromToken);
            userVOForAndroid.setCommentCommentCount(comments);
            Integer commentZan = commentMapper.getTotalLikesByUserId(userIdFromToken);
            userVOForAndroid.setCommentZanCount(commentZan);
            return ResultVO.success(userVOForAndroid);
        } else {
            return ResultVO.error("用户不存在");
        }
    }

    /**
     * 获取用户点赞的帖子
     * @param token 用户的身份令牌
     * @return ResultVO
     */
    @GetMapping("/likePost")
    @CrossOrigin("*")
    public ResultVO getLike(@RequestParam("token") String token) {
        try {

            Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
            QueryWrapper<Like> likeQueryWrapper = new QueryWrapper<>();
            likeQueryWrapper.eq("user_id", userIdFromToken)
                    .eq("object_type", "Post");

            List<Like> likes = likeService.list(likeQueryWrapper);
            List<PostDTO> postDTOList = new ArrayList<>();

            for (Like like : likes) {
                Integer objectId = like.getObjectId();
                Post post = postService.getById(objectId);
                if (post != null) {
                    PostDTO postDTO = postService.convertToDTO(post);
                    postDTOList.add(postDTO);
                }
            }

            return ResultVO.success(postDTOList);

        } catch (Exception e) {
            // 添加日志记录
            return ResultVO.failure("Error getting likes: " + e.getMessage());
        }
    }

    /**
     * 获取用户的帖子列表
     * @param token 用户的身份令牌
     * @return ResultVO
     */
    @GetMapping("/post")
    @CrossOrigin("*")
    public ResultVO getMyPostList(@RequestParam("token") String token) {
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        if (userIdFromToken != null) {
            QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userIdFromToken);
            ResultVO postList = postController.getPostList(queryWrapper);
            return ResultVO.success(postList);
        } else {
            return ResultVO.error("用户不存在");
        }
    }

    /**
     * 获取给我点赞的用户
     * @param token
     * @return
     */
    @GetMapping("/getLikeUser")
    @CrossOrigin
    public ResultVO getLikeUser(@RequestParam("token") String token) {
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        return Optional.ofNullable(userIdFromToken)
                .map(id -> {
                    List<Integer> distinctUserIdsByPostLikes = likeMapper.getDistinctUserIdsByPostLikes(id);

                    List<UserM> users = distinctUserIdsByPostLikes.stream()
                            .map(userService::getById)
                            .map(this::convertToUserM)
                            .collect(Collectors.toList());

                    return ResultVO.success(users);
                })
                .orElseGet(ResultVO::error);
    }
    private UserM convertToUserM(User user) {
        UserM userM = new UserM();
        BeanUtils.copyProperties(user, userM);

        Optional.ofNullable(userM.getUserImage())
                .ifPresent(image -> userM.setUserImage(PathUtil.convertToHttpUrl(image)));

        return userM;
    }

    /**
     * 获取用户的评论列表
     * @param token 用户的身份令牌
     * @return ResultVO
     */
    @GetMapping("/comment")
    @CrossOrigin("*")
    public ResultVO getMyCommentList(@RequestParam("token") String token) {
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        if (userIdFromToken != null) {
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userIdFromToken);
            List<Comment> list = commentService.list(queryWrapper);
            return ResultVO.success(list);
        } else {
            return ResultVO.error("用户不存在");
        }
    }

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @return ResultVO
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/regist")
    public ResultVO register(@RequestParam("userName") String username,
                             @RequestParam("userPass") String password) {
        return userService.register(username, password);
    }
}
