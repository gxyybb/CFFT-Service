package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cfft.beans.CategoryList;
import com.example.cfft.beans.Tips;
import com.example.cfft.beans.vo.CategoryListVO;
import com.example.cfft.common.utils.FileUtil;
import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.utils.Static;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.mapper.TipsMapper;
import com.example.cfft.service.CategoryListService;
import com.example.cfft.service.TipsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Category List API")
@RestController
@RequestMapping("/categoryList")
public class CategoryListController {
    @Autowired
    private CategoryListService categoryListService;
    @Autowired
    private TipsMapper tipsMapper;
    @Autowired
    private TipsService tipsService;

    @Operation( summary = "保存分类列表")
    @PostMapping
    @CrossOrigin("*")
    public ResultVO saveCategoryList(
            @Parameter(description = "分类名称", required = true) @RequestParam("categoryName") String categoryListName,
            @Parameter(description = "分类图片", required = true) @RequestParam("image") MultipartFile image) {

        CategoryList categoryList = new CategoryList();
        categoryList.setCategoryName(Optional.ofNullable(categoryListName).orElse("默认分类"));
        String s = FileUtil.saveFile(Static.CATEGORYLIST + categoryListName, image);
        categoryList.setImage(s);
        categoryListService.save(categoryList);
        return ResultVO.success();
    }

    @Operation( summary = "删除分类")
    @DeleteMapping
    @CrossOrigin("*")
    public ResultVO delete(
            @Parameter(description = "分类名称") @RequestParam(value = "categoryName", required = false) String categoryName,
            @Parameter(description = "分类ID") @RequestParam(value = "categoryId", required = false) Integer categoryId) {
        if ((categoryName == null && categoryId == null) || (categoryName != null && categoryId != null)) {
            return ResultVO.error("必须且只能传入 categoryName 或 categoryId 中的一个");
        }

        Optional<CategoryList> categoryOptional = Optional.ofNullable(categoryId)
                .map(categoryListService::getById)
                .or(() -> Optional.ofNullable(categoryName).map(name -> {
                    QueryWrapper<CategoryList> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("category_name", name);
                    return categoryListService.getOne(queryWrapper);
                }));

        return categoryOptional.map(category -> {
            FileUtil.deleteFile(category.getImage());
            boolean isRemoved = categoryListService.removeById(category.getId());
            return isRemoved ? ResultVO.success() : ResultVO.error("删除分类失败");
        }).orElseGet(() -> ResultVO.error("未找到对应的类别"));
    }

    @Operation( summary = "获取所有分类信息")
    @GetMapping
    @CrossOrigin("*")
    public ResultVO getAllInformation() {
        List<CategoryList> list = categoryListService.list();

        List<CategoryListVO> collect = list.stream().distinct().peek(
                categoryList -> {
                    Optional.ofNullable(categoryList.getImage()).ifPresent(img -> {
                        categoryList.setImage(PathUtil.convertToHttpUrl(img));
                    });
                }
        ).map(categoryList -> {
            CategoryListVO categoryListVO = new CategoryListVO();
            BeanUtils.copyProperties(categoryList, categoryListVO);
            Tips tips = tipsMapper.selectRandomTips(categoryList.getId());
            if (tips != null) {
                categoryListVO.setTips(tips.getTips());
            }
            return categoryListVO;
        }).collect(Collectors.toList());

        return ResultVO.success(collect);
    }

    @Operation( summary = "保存提示信息")
    @PostMapping("/saveTips")
    @CrossOrigin("*")
    public ResultVO saveTips(
            @Parameter(description = "提示信息", required = true) @RequestParam("tips") String tips,
            @Parameter(description = "分类列表ID", required = true) @RequestParam("categoryListId") Integer categoryId) {
        return Optional.ofNullable(tips).map(tip -> {
            Optional<CategoryList> categoryListOptional = Optional.ofNullable(categoryListService.getById(categoryId));
            if (categoryListOptional.isPresent()) {
                Tips tipsEntity = new Tips();
                tipsEntity.setCategoryListId(categoryId);
                tipsEntity.setTips(tip);
                tipsService.save(tipsEntity);
                return ResultVO.success();
            } else {
                throw new IllegalArgumentException("Category ID 不存在");
            }
        }).orElseThrow(() -> new IllegalArgumentException("Tips 不能为空"));
    }
}
