package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cfft.beans.Carousel;
import com.example.cfft.beans.Post;
import com.example.cfft.beans.PostImg;
import com.example.cfft.beans.vo.CarouselVO;
import com.example.cfft.common.utils.FileUtil;

import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.utils.Static;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.CarouselService;
import com.example.cfft.service.PostImgService;
import com.example.cfft.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author gxy
 * @since 2024-03-28
 */
@RestController
@Tag(name = "轮播图管理", description = "处理轮播图相关操作")
@RequestMapping("/carousel")
public class CarouselController {
    private final CarouselService carouselService;
    private final PostController postController;
    private final PostService postService;
    private final PostImgService postImgService;
    @Autowired
    public CarouselController(CarouselService carouselService, PostController postController, PostService postService, PostImgService postImgService) {
        this.carouselService = carouselService;
        this.postController = postController;
        this.postService = postService;
        this.postImgService = postImgService;
    }

    @Operation(summary = "添加轮播图", description = "上传图片并添加轮播图信息")
    @CrossOrigin(origins = "*")
    @PostMapping
    public ResultVO save(
            @Parameter(description = "上传的图片文件", required = true) @RequestParam("image") MultipartFile image,
            @Parameter(description = "轮播图标题", required = true) @RequestParam("title") String title,
            @Parameter(description = "轮播图描述", required = true) @RequestParam("desc") String desc,
            @Parameter(description = "轮播图链接", required = true) @RequestParam("url") String url) {
        Carousel carousel = new Carousel();
        Optional<MultipartFile> image1 = Optional.ofNullable(image);
        image1.ifPresent(img->{
            String s = FileUtil.saveFile(Static.CAROUSEL_IMG, image);
            carousel.setImageUrl(s);
            carousel.setLink(url);
        });
        Optional.ofNullable(title).ifPresent(t->{
            carousel.setTitle(title);
        });
        Optional.ofNullable(desc).ifPresent(d->{
            carousel.setDescription(desc);
        });

        boolean save = carouselService.save(carousel);
        return save?ResultVO.success():ResultVO.error();
    }
    @Operation(summary = "通过帖子设置轮播图", description = "根据帖子ID和图片ID设置轮播图")
    @CrossOrigin(origins = "*")
    @PostMapping("setPost")
    public ResultVO saveByPost(
            @Parameter(description = "帖子ID", required = true) @RequestParam("postId") Integer postId,
            @Parameter(description = "图片ID", required = true) @RequestParam("postImg") Integer postImg) {
        Carousel carousel = new Carousel();

        // 检查 postId 是否存在
        Optional<Post> optionalPost = Optional.ofNullable(postService.getById(postId));
        if (optionalPost.isEmpty()) {
            return ResultVO.error("帖子不存在");
        }
        Post post = optionalPost.get();
        // 获取 postImg 并设置默认图片
        PostImg byId = postImgService.getById(postImg);
        String imageUrl = Optional.ofNullable(byId.getUrl()).orElseGet(() -> Static.DefaultImage);
        carousel.setImageUrl(imageUrl);

        // 设置标题，内容和postId
        String title = Optional.ofNullable(post.getTitle()).orElse("默认标题");
        carousel.setTitle(title);

        String description = Optional.ofNullable(post.getContent()).orElse("默认内容");
        carousel.setDescription(description);

        carousel.setPostId(postId);

        // 保存 carousel 并返回结果
        boolean save = carouselService.save(carousel);
        return save ? ResultVO.success() : ResultVO.error();
    }

    @Operation(summary = "获取所有轮播图", description = "获取所有显示的轮播图信息")
    @CrossOrigin(origins = "*")
    @GetMapping("all")
    public ResultVO list() {
        // 创建查询条件，只查询 is_displayed 为 1 的 Carousel
        QueryWrapper<Carousel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_displayed", 1);

        // 获取符合条件的 Carousel 列表
        List<Carousel> list = carouselService.list(queryWrapper);

        // 去重并转换图片 URL
        List<Carousel> distinctList = list.stream()
                .distinct()
                .peek(carousel -> carousel.setImageUrl(PathUtil.convertToHttpUrl(carousel.getImageUrl())))
                .collect(Collectors.toList());

        // 创建 CarouselVO 并设置属性
        CarouselVO carouselVO = new CarouselVO();
        carouselVO.setNum(distinctList.size());
        carouselVO.setCarouses(distinctList);

        // 返回封装好的结果
        return ResultVO.success(carouselVO);
    }

    @Operation(summary = "获取单个轮播图", description = "根据轮播图ID获取详细信息")
    @CrossOrigin(origins = "*")
    @GetMapping
    public ResultVO getOne(
            @Parameter(description = "轮播图ID", required = true) @RequestParam("carouselId") Integer carouseId) {
        Optional<Carousel> optionalCarousel = Optional.ofNullable(carouselService.getById(carouseId));
        if (optionalCarousel.isEmpty()) {
            return ResultVO.error();
        }
        Carousel carousel = optionalCarousel.get();
        // 使用 Optional 处理 postId 逻辑
        return Optional.ofNullable(carousel.getPostId())
                .map(postController::getPostById)
                .orElseGet(() -> {
                    String imageUrl = carousel.getImageUrl();
                    String convertedImageUrl = PathUtil.convertToHttpUrl(imageUrl);
                    carousel.setImageUrl(convertedImageUrl);
                    return ResultVO.successYY(carousel);
                });
    }
    @Operation(summary = "删除轮播图", description = "根据轮播图ID删除轮播图")
    @CrossOrigin(origins = "*")
    @DeleteMapping
    public ResultVO delete(
            @Parameter(description = "轮播图ID", required = true) @RequestParam("carouselId") Integer carouselId) {
        AtomicBoolean success = new AtomicBoolean(false);

        Optional.ofNullable(carouselId).ifPresent(id -> {
            Carousel carousel = new Carousel();
            carousel.setCarouselId(id);
            carousel.setIsDisplayed(false);
            carouselService.updateById(carousel);
            success.set(true);
        });

        return success.get() ? ResultVO.success() : ResultVO.error();
    }

}
