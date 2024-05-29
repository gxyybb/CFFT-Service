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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/post")
public class PostController{

    private final PostService postService;
    private final PostImgService postImgService;
    private final LikeService likeService;
    private final UserService  userService;;
    @Autowired
    private CommentService commentService;
    @Autowired
    public PostController(PostImgService postImgService, LikeService likeService, PostService postService, UserService userService) {
        this.postImgService = postImgService;
        this.likeService = likeService;
        this.postService = postService;
        this.userService = userService;
    }

    /**
     * 创建帖子
     */
    @CrossOrigin(origins = "*")
    @PostMapping

    public ResultVO createPost(@RequestParam("title") String title,
                               @RequestParam("content") String content,
                               @RequestParam("token") String token,
                               @RequestParam("images") MultipartFile[] images) {
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        List<String> img = new ArrayList<>();
        if (userIdFromToken == null) {
            return ResultVO.error("token is null");
        }

        // 生成一个UUID作为唯一标识
        String uniqueId = UUID.randomUUID().toString();
        String postUrl = Static.POST_IMG + uniqueId; // 使用UUID作为目录名称的一部分



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
                // 确认post对象已成功保存后再获取postId
                Integer postId = post.getPostId();
                for (String s : img) {
                    PostImg postImg = new PostImg();
                    postImg.setPostId(postId); // 设置postImg的postId属性
                    postImg.setUrl(s);
                    boolean save1 = postImgService.save(postImg);
                    if (!save1) {
                        return ResultVO.error("Error saving");
                    }
                }
            }

        return ResultVO.success();
    }

    /**
     * 获取帖子列表
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/list")
    public ResultVO getPostList(@RequestParam(required = false)QueryWrapper<Post> queryWrapper){
        List<Post> list;
        if (queryWrapper ==null) {
            list = postService.list();
        }else {
            list = postService.list(queryWrapper);
        }


        return generatePostDTO(list);
    }



    /**
     * 获取单个帖子信息
     * @param postId
     * @return
     */
    @CrossOrigin(origins = "*")
    @GetMapping
    public ResultVO getPostById(@RequestParam("postId") Integer postId) {
        Post post = postService.getById(postId);
        post.setViewCount(post.getViewCount() + 1);
        postService.updateById(post);
        PostDTO postDTO = postService.convertToDTO(post);

        return ResultVO.success(postDTO);
    }
























    @CrossOrigin("*")
    @PostMapping("saveList")
    public ResultVO saveList(@RequestBody List<Post> posts,@RequestParam("token")String token){
        int x = posts.size();
        List<Post> ps = new ArrayList<>();
        int y = 0;
        for (Post post : posts){

            ResultVO resultVO = createPost(post.getTitle(), post.getContent(), token, null);
            if (resultVO.getCode()==200){
                y++;
            }else {
                ps.add(post);
            }

        }
        return ResultVO.success("创建成功"+y+"条记录\n失败"+(x-y)+"条记录",ps);
    }




    private ResultVO generatePostDTO(List<Post> list) {
        List<PostDTO> postDTOList = new ArrayList<>();
        for (Post post : list) {
            PostDTO postDTO = postService.convertToDTO(post);

            postDTOList.add(postDTO);
        }
        return ResultVO.success(postDTOList);
    }



    /**
     * 查询帖子
     * @param keyword
     * @return
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/search")
    public ResultVO searchPost(@RequestParam("keyword") String keyword){
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("title","%"+ keyword+"%");
        List<Post> list = postService.list(queryWrapper);

        return generatePostDTO(list);
    }

    /**
     * 更新帖子
     * @param id
     * @param title
     * @param content
     * @param token
     * @param images
     * @return
     */
    @CrossOrigin(origins = "*")
    @PutMapping
    @Transactional
    public ResultVO updatePost(@RequestParam("postId") Integer id,
                               @RequestParam("title") String title,
                               @RequestParam("content") String content,
                               @RequestParam("token") String token,
                               @RequestParam("images") MultipartFile[] images){
//        boolean b = postService.removeById(id);
        ResultVO resultVO = deletePost(id);
        boolean b = resultVO.getCode()==200;
        return b ? (createPost(title,  content, token, images)) : ResultVO.error("更新失败");
    }

    /**
     * 删除帖子
     * @param postId
     * @return
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping
    @Transactional
    public ResultVO deletePost(@RequestParam("postId") Integer postId){
        Post byId = postService.getById(postId);
        QueryWrapper<PostImg> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id",postId);

        QueryWrapper<Comment> queryWrapper1 = new QueryWrapper<>();
        postImgService.remove(queryWrapper);
        queryWrapper1.eq("post_id",postId);
        commentService.remove(queryWrapper1);
        if (byId.getImg()!=null){
            FileUtil.deleteFile(byId.getImg());
        }
        postService.removeById(postId);
        return ResultVO.success();
    }

    /**
     * 点赞
     * @param postId
     * @param token
     * @return
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/like")
    public ResultVO likePost(@RequestParam("postId") Integer postId,
                             @RequestParam("token") String token){
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        Like like = new Like();
        like.setUserId(userIdFromToken);
        QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("object_id",postId)
                .eq("object_type","Post")
                .eq("user_id",userIdFromToken);
        List<Like> list = likeService.list(queryWrapper);
        if (list == null || list.isEmpty()){ // Check if list is empty
            like.setObjectType("Post");
            like.setObjectId(postId);
            like.setLikeTime(new Date());
            boolean saved = likeService.save(like); // Save the like
            if (saved){
                Post byId = postService.getById(postId);
                byId.setLikeCount(byId.getLikeCount()+1);
                postService.updateById(byId);
                return ResultVO.success(byId.getLikeCount()); // Return success response with updated like count
            } else {
                return ResultVO.error("点赞失败");
            }
        } else {
            boolean removed = likeService.remove(queryWrapper); // Remove existing like
            if (removed){
                Post byId = postService.getById(postId);
                byId.setLikeCount(byId.getLikeCount()-1); // Decrement like count
                postService.updateById(byId);
                return ResultVO.success(byId.getLikeCount()); // Return success response with updated like count
            } else {
                return ResultVO.error("取消点赞失败");
            }
        }
    }


    /**
     * 取消点赞
     * @param postId
     * @param token
     * @return
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/unlike")
    @Transactional
    public ResultVO unlikePost(@RequestParam("postId") Integer postId,
                             @RequestParam("token") String token){
        QueryWrapper<Like> likeQueryWrapper = new QueryWrapper<>();
        likeQueryWrapper.eq("user_id", TokenUtil.getUserIdFromToken(token));
        likeQueryWrapper.eq("object_id", postId);
        likeQueryWrapper.eq("object_type", "Post");
        boolean b = likeService.remove(likeQueryWrapper);
        if (b){
            Post byId = postService.getById(postId);
            byId.setLikeCount(byId.getLikeCount()-1);
            postService.updateById(byId);
        }else {
            return ResultVO.error("取消点赞失败");
        }
        return ResultVO.success();
    }


}
