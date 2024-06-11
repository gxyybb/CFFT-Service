package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cfft.beans.*;
import com.example.cfft.beans.vo.PostDTO;
import com.example.cfft.common.utils.FileUtil;
import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.utils.Static;
import com.example.cfft.common.utils.TokenUtil;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Tag(name = "帖子管理", description = "处理与帖子相关的操作")
@RestController
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final PostImgService postImgService;
    private final LikeService likeService;
    private final UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    public PostController(PostImgService postImgService, LikeService likeService, PostService postService, UserService userService) {
        this.postImgService = postImgService;
        this.likeService = likeService;
        this.postService = postService;
        this.userService = userService;
    }

    @Operation(summary = "创建帖子")
    @CrossOrigin(origins = "*")
    @PostMapping
    public ResultVO createPost(
            @Parameter(description = "标题", required = true) @RequestParam("title") String title,
            @Parameter(description = "内容", required = true) @RequestParam("content") String content,
            @Parameter(description = "用户Token", required = true) @RequestParam("token") String token,
            @Parameter(description = "图片文件数组") @RequestParam("images") MultipartFile[] images) {

        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        List<String> img = new ArrayList<>();
        if (userIdFromToken == null) {
            return ResultVO.error("token is null");
        }

        String uniqueId = UUID.randomUUID().toString();
        String postUrl = Static.POST_IMG + uniqueId;

        for (MultipartFile file : images) {
            String s = FileUtil.saveFile(postUrl, file);
            if (s != null) {
                img.add(s);
            } else {
                return ResultVO.error("Error saving");
            }
        }

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setPublishTime(new Date());
        post.setImg(postUrl);
        post.setUserId(userIdFromToken);
        boolean save = postService.save(post);

        if (save) {
            Integer postId = post.getPostId();
            for (String s : img) {
                PostImg postImg = new PostImg();
                postImg.setPostId(postId);
                postImg.setUrl(s);
                boolean save1 = postImgService.save(postImg);
                if (!save1) {
                    return ResultVO.error("Error saving");
                }
            }
        }

        return ResultVO.success();
    }

    @Operation(summary = "获取帖子列表")
    @CrossOrigin(origins = "*")
    @GetMapping("/list")
    public ResultVO getPostList(@Parameter(description = "查询条件") @RequestParam(required = false) QueryWrapper<Post> queryWrapper) {
        List<Post> list;
        if (queryWrapper == null) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("publish_time");
            list = postService.list(queryWrapper);
        } else {
            queryWrapper.orderByDesc("publish_time");
            list = postService.list(queryWrapper);
        }
        return generatePostDTO(list);
    }

    @Operation(summary = "获取单个帖子信息")
    @CrossOrigin(origins = "*")
    @GetMapping
    public ResultVO getPostById(@Parameter(description = "帖子ID", required = true) @RequestParam("postId") Integer postId) {
        Post post = postService.getById(postId);
        post.setViewCount(post.getViewCount() + 1);
        postService.updateById(post);
        PostDTO postDTO = postService.convertToDTO(post);

        return ResultVO.success(postDTO);
    }
    @Operation(summary = "查询是否点赞")
    @CrossOrigin(origins = "*")
    @GetMapping("/likeOrNo")
    public ResultVO likeOrNo(@Parameter(description = "帖子ID", required = true) @RequestParam("postId") Integer postId,@Parameter(description = "用户token", required = true)String token){
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("object_id",postId);
        queryWrapper.eq("object_type","Post");
        queryWrapper.eq("user_id",userIdFromToken);
        boolean exists = likeService.exists(queryWrapper);

        return exists?ResultVO.success("1"):ResultVO.success("0");
    }

    @Operation(summary = "批量保存帖子")
    @CrossOrigin("*")
    @PostMapping("saveList")
    public ResultVO saveList(
            @Parameter(description = "帖子列表", required = true) @RequestBody List<Post> posts,
            @Parameter(description = "用户Token", required = true) @RequestParam("token") String token) {

        int x = posts.size();
        List<Post> ps = new ArrayList<>();
        int y = 0;
        for (Post post : posts) {
            ResultVO resultVO = createPost(post.getTitle(), post.getContent(), token, null);
            if (resultVO.getCode() == 200) {
                y++;
            } else {
                ps.add(post);
            }
        }
        return ResultVO.success("创建成功" + y + "条记录\n失败" + (x - y) + "条记录", ps);
    }

    @Operation(summary = "查询帖子")
    @CrossOrigin(origins = "*")
    @GetMapping("/search")
    public ResultVO searchPost(@Parameter(description = "关键字", required = true) @RequestParam("keyword") String keyword) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("title", "%" + keyword + "%").orderByDesc("publish_time");
        List<Post> list = postService.list(queryWrapper);

        return generatePostDTO(list);
    }

    @Operation(summary = "更新帖子")
    @CrossOrigin(origins = "*")
    @PutMapping
    @Transactional
    public ResultVO updatePost(
            @Parameter(description = "帖子ID", required = true) @RequestParam("postId") Integer id,
            @Parameter(description = "标题", required = true) @RequestParam("title") String title,
            @Parameter(description = "内容", required = true) @RequestParam("content") String content,
            @Parameter(description = "用户Token", required = true) @RequestParam("token") String token,
            @Parameter(description = "图片文件数组") @RequestParam("images") MultipartFile[] images) {

        ResultVO resultVO = deletePost(id);
        boolean b = resultVO.getCode() == 200;
        return b ? createPost(title, content, token, images) : ResultVO.error("更新失败");
    }

    @Operation(summary = "删除帖子")
    @CrossOrigin(origins = "*")
    @DeleteMapping
    @Transactional
    public ResultVO deletePost(@Parameter(description = "帖子ID", required = true) @RequestParam("postId") Integer postId) {
        Post byId = postService.getById(postId);
        QueryWrapper<PostImg> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId);

        QueryWrapper<Comment> queryWrapper1 = new QueryWrapper<>();
        postImgService.remove(queryWrapper);
        queryWrapper1.eq("post_id", postId)
                .eq("type", "post");
        commentService.remove(queryWrapper1);
        if (byId.getImg() != null) {
            FileUtil.deleteFile(byId.getImg());
        }
        postService.removeById(postId);
        return ResultVO.success();
    }

    @Operation(summary = "点赞帖子")
    @CrossOrigin(origins = "*")
    @PostMapping("/like")
    public ResultVO likePost(
            @Parameter(description = "帖子ID", required = true) @RequestParam("postId") Integer postId,
            @Parameter(description = "用户Token", required = true) @RequestParam("token") String token) {

        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        Like like = new Like();
        like.setUserId(userIdFromToken);
        QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("object_id", postId)
                .eq("object_type", "Post")
                .eq("user_id", userIdFromToken);
        List<Like> list = likeService.list(queryWrapper);
        if (list == null || list.isEmpty()) {
            like.setObjectType("Post");
            like.setObjectId(postId);
            like.setLikeTime(new Date());
            boolean saved = likeService.save(like);
            if (saved) {
                Post byId = postService.getById(postId);
                byId.setLikeCount(byId.getLikeCount() + 1);
                postService.updateById(byId);
                return ResultVO.success(byId.getLikeCount());
            } else {
                return ResultVO.error("点赞失败");
            }
        } else {
            boolean removed = likeService.remove(queryWrapper);
            if (removed) {
                Post byId = postService.getById(postId);
                byId.setLikeCount(byId.getLikeCount() - 1);
                postService.updateById(byId);
                return ResultVO.success(byId.getLikeCount());
            } else {
                return ResultVO.error("取消点赞失败");
            }
        }
    }

    @Operation(summary = "取消点赞帖子")
    @CrossOrigin(origins = "*")
    @PostMapping("/unlike")
    @Transactional
    public ResultVO unlikePost(
            @Parameter(description = "帖子ID", required = true) @RequestParam("postId") Integer postId,
            @Parameter(description = "用户Token", required = true) @RequestParam("token") String token) {

        QueryWrapper<Like> likeQueryWrapper = new QueryWrapper<>();
        likeQueryWrapper.eq("user_id", TokenUtil.getUserIdFromToken(token));
        likeQueryWrapper.eq("object_id", postId);
        likeQueryWrapper.eq("object_type", "Post");
        boolean b = likeService.remove(likeQueryWrapper);
        if (b) {
            Post byId = postService.getById(postId);
            byId.setLikeCount(byId.getLikeCount() - 1);
            postService.updateById(byId);
        } else {
            return ResultVO.error("取消点赞失败");
        }
        return ResultVO.success();
    }

    private ResultVO generatePostDTO(List<Post> list) {
        List<PostDTO> postDTOList = new ArrayList<>();
        for (Post post : list) {
            PostDTO postDTO = postService.convertToDTO(post);
            postDTOList.add(postDTO);
        }
        return ResultVO.success(postDTOList);
    }
}
