package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.cfft.beans.*;
import com.example.cfft.beans.vo.*;
import com.example.cfft.common.utils.*;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.mapper.CommentMapper;
import com.example.cfft.mapper.LikeMapper;
import com.example.cfft.mapper.PostMapper;
import com.example.cfft.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.cfft.common.utils.Static.BASE_URL_FOR_USER;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户管理接口")
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
    @Autowired
    private RestTemplate restTemplate;

    private static final String CREATE_PLAYER_URL = "http://localhost:8082/player/create";

    @Operation(summary = "创建用户Player", description = "根据token创建用户Player")
    @CrossOrigin("*")
    @PostMapping("/createPlayer")
    public ResultVO createUser(@Parameter(description = "用户token") @RequestParam("token") String token) {
        try {
            Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
            return Optional.ofNullable(userService.getById(userIdFromToken))
                    .map(user -> {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Content-Type", "application/json");

                        HttpEntity<User> request = new HttpEntity<>(user, headers);

                        ResponseEntity<ResultVO> response = restTemplate.postForEntity(
                                CREATE_PLAYER_URL,
                                request,
                                ResultVO.class
                        );

                        return response.getBody();
                    })
                    .orElseGet(() -> ResultVO.error("用户不存在"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.error("创建Player失败：" + e.getMessage());
        }
    }

    @Operation(summary = "管理员登录", description = "管理员账户登录")
    @CrossOrigin(origins = "*")
    @PostMapping("/loginMaster")
    public ResultVO loginMaster(@Parameter(description = "用户名") @RequestParam("username") String username,
                                @Parameter(description = "密码") @RequestParam("password") String password) {
        if (username.equals("admin") && password.equals("admin")) {
            return ResultVO.success(TokenUtil.generateToken(String.valueOf(123456789)));
        } else {
            return ResultVO.error("登录失败");
        }
    }

    @Operation(summary = "用户登录", description = "用户登录接口")
    @CrossOrigin(origins = "*")
    @PostMapping("/login")
    public ResultVO login(@Parameter(description = "用户名") @RequestParam("username") String username,
                          @Parameter(description = "密码") @RequestParam("password") String password) {
        return userService.login(username, password);
    }

    @Operation(summary = "更新用户头像", description = "更新用户头像图片")
    @CrossOrigin(origins = "*")
    @PutMapping("/updateImg")
    public ResultVO updateImg(@Parameter(description = "用户头像图片") @RequestParam("img") MultipartFile img,
                              @Parameter(description = "用户token") @RequestParam("token") String token) {
        Integer userId = TokenUtil.getUserIdFromToken(token);
        User byId = userService.getById(userId);

        if (byId.getUserImage() != null) {
            FileUtil.deleteFile(byId.getUserImage());
        }
        String s = FileUtil.saveFile(BASE_URL_FOR_USER + byId.getUserId(), img);
        byId.setUserImage(s);
        userService.updateById(byId);
        return ResultVO.success("头像上传成功");
    }

    @Operation(summary = "更新用户背景图片", description = "更新用户背景图片")
    @CrossOrigin(origins = "*")
    @PutMapping("/updateBackImg")
    public ResultVO updateBackImg(@Parameter(description = "背景图片") @RequestParam("img") MultipartFile img,
                                  @Parameter(description = "用户token") @RequestParam("token") String token) {
        Integer userId = TokenUtil.getUserIdFromToken(token);
        User byId = userService.getById(userId);

        if (byId.getBackImg() != null) {
            FileUtil.deleteFile(byId.getBackImg());
        }
        String s = FileUtil.saveFile(BASE_URL_FOR_USER + byId.getUserId(), img);
        byId.setBackImg(s);
        userService.updateById(byId);
        return ResultVO.success("背景上传成功");
    }

    @Operation(summary = "更新用户信息", description = "更新指定列的用户信息")
    @CrossOrigin(origins = "*")
    @PutMapping("/updateInfo")
    public ResultVO updateInformation(@Parameter(description = "用户token") @RequestParam("token") String token,
                                      @Parameter(description = "要更新的列名") @RequestParam("column") UserInfo column,
                                      @Parameter(description = "更新的信息") @RequestParam("info") String info) {
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userIdFromToken);

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

        boolean update = userService.update(updateWrapper);
        return update ? ResultVO.success() : ResultVO.error();
    }

    @Operation(summary = "更新用户信息", description = "更新用户详细信息")
    @CrossOrigin("*")
    @PutMapping
    @Transactional
    public ResultVO update(@ModelAttribute UserRO userRO, RedirectAttributes redirectAttributes) {
        try {
            Integer userIdFromToken = TokenUtil.getUserIdFromToken(userRO.getToken());
            if (userIdFromToken == null) {
                return ResultVO.failure("Invalid token");
            }

            Optional<User> optionalUser = Optional.ofNullable(userService.getById(userIdFromToken));
            if (optionalUser.isEmpty()) {
                return ResultVO.failure("User not found");
            }

            User byId = optionalUser.get();
            Date birthdate = null;
            if (userRO.getBirthdate() != null) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    birthdate = formatter.parse(userRO.getBirthdate());
                } catch (ParseException e) {
                    return ResultVO.failure("Invalid birthdate format. Please use yyyy-MM-dd.");
                }
            }

            Optional.ofNullable(userRO.getBio()).ifPresent(byId::setBio);
            Optional.ofNullable(userRO.getGender()).ifPresent(byId::setGender);
            Optional.ofNullable(userRO.getNickName()).ifPresent(byId::setNickName);

            Optional.ofNullable(userRO.getBackImgFile()).ifPresent(file -> {
                if (byId.getBackImg() != null && !byId.getBackImg().startsWith(Static.DEFAULT_USER)) {
                    FileUtil.deleteFile(byId.getBackImg());
                }
                String backImgPath = FileUtil.saveFile(BASE_URL_FOR_USER + byId.getUserId(), file);
                byId.setBackImg(backImgPath);
            });

            Optional.ofNullable(userRO.getUserImageFile()).ifPresent(file -> {
                if (byId.getUserImage() != null && !byId.getUserImage().startsWith(Static.DEFAULT_USER)) {
                    FileUtil.deleteFile(byId.getUserImage());
                }
                String userImagePath = FileUtil.saveFile(BASE_URL_FOR_USER + byId.getUserId() + "/back", file);
                byId.setUserImage(userImagePath);
            });

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

    @Operation(summary = "获取用户详细信息", description = "根据token获取用户详细信息")
    @CrossOrigin(origins = "*")
    @GetMapping("/first")
    public ResultVO getUserVOF(@Parameter(description = "用户token") @RequestParam("token") String token) {
        try {
            Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
            User byId = userService.getById(userIdFromToken);
            UserVOF userVOF = new UserVOF();
            Date registrationTime = byId.getRegistrationTime();
            Instant now = Instant.now();
            Instant registrationInstant = registrationTime.toInstant();
            Duration duration = Duration.between(registrationInstant, now);
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

    @Operation(summary = "设置用户文本信息", description = "设置用户的文本信息")
    @CrossOrigin(origins = "*")
    @PostMapping("/setTexts")
    public ResultVO setTexts(@Parameter(description = "用户token") @RequestParam("token") String token,
                             @Parameter(description = "文本信息1") @RequestParam("text1") String text1,
                             @Parameter(description = "文本信息2") @RequestParam("text2") String text2,
                             @Parameter(description = "文本信息3") @RequestParam("text3") String text3) {
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

    @Operation(summary = "获取用户信息", description = "根据token获取用户信息")
    @CrossOrigin("*")
    @GetMapping
    public ResultVO get(@Parameter(description = "用户token") @RequestParam("token") String token) {
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

    @Operation(summary = "获取用户点赞的帖子", description = "获取用户点赞过的帖子列表")
    @GetMapping("/likePost")
    @CrossOrigin("*")
    public ResultVO getLike(@Parameter(description = "用户token") @RequestParam("token") String token) {
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
            return ResultVO.failure("Error getting likes: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户的帖子列表", description = "获取用户发布的帖子列表")
    @GetMapping("/post")
    @CrossOrigin("*")
    public ResultVO getMyPostList(@Parameter(description = "用户token") @RequestParam("token") String token) {
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

    @Operation(summary = "获取给我点赞的用户", description = "获取所有点赞我发布内容的用户列表")
    @GetMapping("/getLikeUser")
    @CrossOrigin
    public ResultVO getLikeUser(@Parameter(description = "用户token") @RequestParam("token") String token) {
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        return Optional.ofNullable(userIdFromToken)
                .map(id -> {
                    List<Integer> distinctUserIdsByPostLikes = likeMapper.getDistinctUserIdsByPostLikes(id);

                    List<UserM> users = distinctUserIdsByPostLikes.stream()
                            .map(userService::getById)
                            .filter(Objects::nonNull)  // 添加过滤步骤
                            .map(this::convertToUserM)  // 调用转换方法
                            .collect(Collectors.toList());

                    return ResultVO.success(users);
                })
                .orElseGet(ResultVO::error);
    }

    @Operation(summary = "获取用户评论列表", description = "获取用户发布的评论列表")
    @GetMapping("/comment")
    @CrossOrigin("*")
    public ResultVO getMyCommentList(@Parameter(description = "用户token") @RequestParam("token") String token) {
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

    @Operation(summary = "用户注册", description = "新用户注册")
    @CrossOrigin(origins = "*")
    @PostMapping("/regist")
    public ResultVO register(@Parameter(description = "用户名") @RequestParam("userName") String username,
                             @Parameter(description = "用户密码") @RequestParam("userPass") String password) {
        return userService.register(username, password);
    }
    private UserM convertToUserM(User user) {
        UserM userM = new UserM();
        BeanUtils.copyProperties(user, userM);
        Optional.ofNullable(userM.getUserImage())
                .ifPresent(image -> userM.setUserImage(PathUtil.convertToHttpUrl(image)));
        return userM;
    }

}
