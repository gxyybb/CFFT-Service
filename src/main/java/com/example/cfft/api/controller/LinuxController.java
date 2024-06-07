package com.example.cfft.api.controller;

import com.example.cfft.beans.*;
import com.example.cfft.common.utils.CheckPath;
import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "路径转换", description = "处理Windows和Linux路径转换")
@RestController
@RequestMapping("gxy")
public class LinuxController {
    private final CarouselService carouselService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final MushroomService mushroomService;
    private final MushroomImgService mushroomImgService;
    private final PostService postService;
    private final PostImgService postImgService;
    private final TextService textService;
    private final UserService userService;

    public LinuxController(CarouselService carouselService, CategoryService categoryService, CommentService commentService, LikeService likeService, MushroomService mushroomService, MushroomImgService mushroomImgService, PostService postService, PostImgService postImgService, TextService textService, UserService userService) {
        this.carouselService = carouselService;
        this.categoryService = categoryService;
        this.commentService = commentService;
        this.likeService = likeService;
        this.mushroomService = mushroomService;
        this.mushroomImgService = mushroomImgService;
        this.postService = postService;
        this.postImgService = postImgService;
        this.textService = textService;
        this.userService = userService;
    }

    @Operation(summary = "转换路径到Linux格式", description = "将系统中的所有路径转换为Linux格式")
    @PostMapping("/toLinux")
    @CrossOrigin(origins = "*")
    public ResultVO toLinux(
            @Parameter(description = "验证密钥", required = true) @RequestParam("key") String key) {
        if (!"郭炫烨最帅666".equals(key)) {
            return ResultVO.error("你真是个出生");
        }

        try {
            updatePathsToLinux();
            return ResultVO.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.error("更新路径时发生错误: " + e.getMessage());
        }
    }

    // 转换路径到Windows格式
    @Operation(summary = "转换路径到Windows格式", description = "将系统中的所有路径转换为Windows格式")
    @PostMapping("/toWindows")
    @CrossOrigin(origins = "*")
    public ResultVO toWindows(
            @Parameter(description = "验证密钥", required = true) @RequestParam("key") String key) {
        if (!"郭炫烨最帅666".equals(key)) {
            return ResultVO.error("你真是个出生");
        }

        try {
            updatePathsToWindows();
            return ResultVO.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.error("更新路径时发生错误: " + e.getMessage());
        }
    }
    private void updatePathsToLinux() {
        updateCarouselPathsToLinux();
        updateCategoryPathsToLinux();
        updateUserPathsToLinux();
        updatePostImgPathsToLinux();
    }

    private void updatePathsToWindows() {
        updateCarouselPathsToWindows();
        updateCategoryPathsToWindows();
        updateUserPathsToWindows();
        updatePostImgPathsToWindows();
    }

    private void updateCarouselPathsToLinux() {
        List<Carousel> list = carouselService.list();
        list.forEach(carousel -> {
            if (CheckPath.startsWithCColonBackslash(carousel.getImageUrl())) {
                carousel.setImageUrl(PathUtil.covertToLinuxUrl(carousel.getImageUrl()));
                carouselService.updateById(carousel);
            }
        });
    }

    private void updateCarouselPathsToWindows() {
        List<Carousel> list = carouselService.list();
        list.forEach(carousel -> {
            if (CheckPath.startsWithRoot(carousel.getImageUrl())) {
                carousel.setImageUrl(PathUtil.covertToWindowsUrl(carousel.getImageUrl()));
                carouselService.updateById(carousel);
            }
        });
    }

    private void updateCategoryPathsToLinux() {
        List<Category> list = categoryService.list();
        list.forEach(category -> {
            if (CheckPath.startsWithCColonBackslash(category.getCategoryImg())) {
                category.setCategoryImg(PathUtil.covertToLinuxUrl(category.getCategoryImg()));
                categoryService.updateById(category);
            }
        });
    }

    private void updateCategoryPathsToWindows() {
        List<Category> list = categoryService.list();
        list.forEach(category -> {
            if (CheckPath.startsWithRoot(category.getCategoryImg())) {
                category.setCategoryImg(PathUtil.covertToWindowsUrl(category.getCategoryImg()));
                categoryService.updateById(category);
            }
        });
    }

    private void updateUserPathsToLinux() {
        List<User> list = userService.list();
        list.forEach(user -> {
            if (CheckPath.startsWithCColonBackslash(user.getUserImage())) {
                user.setUserImage(PathUtil.covertToLinuxUrl(user.getUserImage()));
                userService.updateById(user);
            }
            if (CheckPath.startsWithCColonBackslash(user.getAvatar())) {
                user.setAvatar(PathUtil.covertToLinuxUrl(user.getAvatar()));
                userService.updateById(user);
            }
        });
    }

    private void updateUserPathsToWindows() {
        List<User> list = userService.list();
        list.forEach(user -> {
            if (CheckPath.startsWithRoot(user.getUserImage())) {
                user.setUserImage(PathUtil.covertToWindowsUrl(user.getUserImage()));
                userService.updateById(user);
            }
            if (CheckPath.startsWithRoot(user.getAvatar())) {
                user.setAvatar(PathUtil.covertToWindowsUrl(user.getAvatar()));
                userService.updateById(user);
            }
        });
    }

    private void updatePostImgPathsToLinux() {
        List<PostImg> list = postImgService.list();
        list.forEach(postImg -> {
            if (CheckPath.startsWithCColonBackslash(postImg.getUrl())) {
                postImg.setUrl(PathUtil.covertToLinuxUrl(postImg.getUrl()));
                postImgService.updateById(postImg);
            }
        });
    }

    private void updatePostImgPathsToWindows() {
        List<PostImg> list = postImgService.list();
        list.forEach(postImg -> {
            if (CheckPath.startsWithRoot(postImg.getUrl())) {
                postImg.setUrl(PathUtil.covertToWindowsUrl(postImg.getUrl()));
                postImgService.updateById(postImg);
            }
        });
    }
}
