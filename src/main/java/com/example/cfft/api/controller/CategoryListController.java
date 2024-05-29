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
import com.example.cfft.service.MushroomService;
import com.example.cfft.service.TipsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categoryList")
public class CategoryListController {
    @Autowired
    private CategoryListService categoryListService;
    @Autowired
    private TipsMapper tipsMapper;

    @Autowired
    private TipsService tipsService;

    @PostMapping
    @CrossOrigin("*")
    public ResultVO saveCategoryList(@RequestParam("categoryName")String categoryListName, @RequestParam("image")MultipartFile image) {

        CategoryList categoryList = new CategoryList();
        categoryList.setCategoryName(Optional.ofNullable(categoryListName).orElseGet(()->{
            return "默认分类";
        }));
        String s = FileUtil.saveFile(Static.CATEGORYLIST + categoryListName, image);
        categoryList.setImage(s);
        categoryListService.save(categoryList);
        return  ResultVO.success();
    }
    @DeleteMapping
    @CrossOrigin("*")
    public ResultVO delete(@RequestParam(value = "categoryName", required = false) String categoryName,
                           @RequestParam(value = "categoryId", required = false) Integer categoryId) {
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
    @GetMapping
    @CrossOrigin("*")
    public ResultVO getAllInformation(){
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

    @PostMapping("/saveTips")
    @CrossOrigin("*")
    public ResultVO saveTips(@RequestParam("tips") String tips, @RequestParam("categoryListId") Integer categoryId) {
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





