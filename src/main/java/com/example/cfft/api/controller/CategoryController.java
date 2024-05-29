package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cfft.beans.Category;
import com.example.cfft.common.utils.FileUtil;
import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 保存类别图片
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/saveImage")
    public ResultVO saveImage(@RequestParam("categoryId") Integer categoryId, @RequestParam("imageFile") MultipartFile imageFile){
        return categoryService.saveImage(categoryId,imageFile);
    }
    /**
     * 创建分类
     */
    @CrossOrigin(origins = "*")
    @PostMapping
    public ResultVO createCategory(@RequestBody Category category) {
        // 使用 AtomicBoolean 来记录保存操作是否成功
        AtomicBoolean saveSuccess = new AtomicBoolean(false);

        // 使用 Optional.ifPresent 来保存类别
        Optional.ofNullable(category).ifPresent(c -> {
            saveSuccess.set(categoryService.save(c));
        });

        // 根据保存操作的结果返回相应的 ResultVO
        return saveSuccess.get() ? ResultVO.success() : ResultVO.error();
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/saveList")
    public ResultVO saveCategoryList(@RequestBody List<Category> categoryList) {
        // 使用并发集合来确保线程安全
        Set<Category> distinctCategories = new HashSet<>(categoryList);

        // 使用并发集合来统计成功和失败的数据
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        StringBuilder failureNames = new StringBuilder();

        // 使用并行流处理，提高性能
        distinctCategories.parallelStream().forEach(category -> {
            ResultVO resultVO = createCategory(category);
            if (resultVO.getCode() == 200) {
                successCount.incrementAndGet();
            } else {
                failureCount.incrementAndGet();
                synchronized (failureNames) {
                    failureNames.append(category.getCategoryName()).append(" ");
                }
            }
        });

        String resultMessage = String.format("共录入 %d 条数据，录入失败 %d 条数据\n失败数据：%s",
                successCount.get(),
                failureCount.get(),
                failureNames.toString().trim());
        return ResultVO.success(resultMessage);
    }



    @DeleteMapping("/image")
    @CrossOrigin(origins = "*")
    @Transactional
    public ResultVO removeImage(@RequestParam("id") Integer categoryId) {
        return Optional.ofNullable(categoryService.getById(categoryId))
                .map(category -> {
                    Optional.ofNullable(category.getCategoryImg())
                            .ifPresent(FileUtil::deleteFile);
                    category.setCategoryImg(null);
                    boolean updateResult = categoryService.updateById(category);
                    return updateResult ? ResultVO.success() : ResultVO.error("失败", "更新类别信息失败");
                })
                .orElse(ResultVO.error("失败", "未找到对应的类别"));
    }

    /**
     * 获取所有分类
     */
    @CrossOrigin(origins = "*")
    @GetMapping()
    public ResultVO removeImage() {
        // 获取所有类别列表
        List<Category> categoryList = categoryService.list();

        // 使用 Stream 处理类别列表
        List<Category> updatedList = categoryList.stream()
                .peek(category -> {
                    Optional.ofNullable(category.getCategoryImg())
                            .ifPresent(img -> category.setCategoryImg(PathUtil.convertToHttpUrl(img)));
                })
                .collect(Collectors.toList());

        return ResultVO.success(updatedList);
    }


    /**
     * 根据分类级别获取分类列表
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/{level}")
    public ResultVO getCategoryByLevel(@PathVariable("level") Integer level) {
        QueryWrapper<Category> query = new QueryWrapper<>();
        query.eq("category_level", level);

        List<Category> collect = categoryService.list(query).stream().distinct().peek(category -> {
            Optional.ofNullable(category.getCategoryImg()).ifPresent(img -> {
                category.setCategoryImg(PathUtil.convertToHttpUrl(img));
            });
        }).collect(Collectors.toList());

        return ResultVO.success(collect);
    }

    /**
     * 根据分类名称获取子分类列表
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/getChildCategoryByName")
    public ResultVO getChildCategoryByName(@RequestParam("categoryName") String categoryName) {
        QueryWrapper<Category> query = new QueryWrapper<>();
        query.eq("category_parent", categoryName);
        List<Category> list = categoryService.list(query);

        List<Category> updatedList = Optional.ofNullable(list)
                .map(l -> l.stream()
                        .distinct()
                        .peek(lt -> Optional.ofNullable(lt.getCategoryImg())
                                .ifPresent(img -> lt.setCategoryImg(PathUtil.convertToHttpUrl(img))))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        return ResultVO.success(updatedList);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/search/{keyword}")
    public ResultVO searchCategory(@PathVariable("keyword") String keyword) {
        // 初步模糊查询
        QueryWrapper<Category> query = new QueryWrapper<>();
        query.like("category_name", keyword);
        List<Category> categories = categoryService.list(query);


        List<Category> sortedCategories = categories.stream()
                .distinct()
                .filter(category -> containsSubsequence(category.getCategoryName(), keyword))
                .peek(category -> Optional.ofNullable(category.getCategoryImg())
                        .ifPresent(img -> category.setCategoryImg(PathUtil.convertToHttpUrl(img))))
                .sorted(Comparator.comparingInt(category -> -getMatchScore(category.getCategoryName(), keyword)))  // 按评分降序排序
                .collect(Collectors.toList());

        return ResultVO.success(sortedCategories);
    }
    //检查是否包含key
    private boolean containsSubsequence(String text, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        if (text.isEmpty()) {
            return false;
        }
        Iterator<Character> textIterator = text.chars().mapToObj(c -> (char) c).iterator();
        return keyword.chars()
                .mapToObj(c -> (char) c)
                .allMatch(keywordChar -> {
                    while (textIterator.hasNext()) {
                        if (textIterator.next().equals(keywordChar)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    private int getMatchScore(String text, String keyword) {
        if (keyword.isEmpty() || text.isEmpty()) {
            return 0;
        }
        AtomicInteger score = new AtomicInteger(0);
        Iterator<Character> textIterator = text.chars().mapToObj(c -> (char) c).iterator();
        keyword.chars()
                .mapToObj(c -> (char) c)
                .forEach(keywordChar -> {
                    while (textIterator.hasNext()) {
                        if (textIterator.next().equals(keywordChar)) {
                            score.incrementAndGet();
                            break;
                        }
                    }
                });
        return score.get();
    }

    /**
     * 根据分类ID获取子分类列表
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/getChildCategoryById")
    public ResultVO searchCategory(@RequestParam("categoryId") Integer categoryId) {
        // 获取父分类
        Optional<Category> categoryOptional = Optional.ofNullable(categoryService.getById(categoryId));

        // 如果父分类存在，获取其名称并搜索子分类
        if (categoryOptional.isPresent()) {
            String categoryName = categoryOptional.get().getCategoryName();
            return getChildCategoryByName(categoryName);
        }

        // 如果父分类不存在，返回错误信息
        return ResultVO.error("未找到对应的类别");
    }


    /**
     * 根据分类名称获取父分类
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/getParentCategory")
    public ResultVO getParentCategory(@RequestParam("categoryName") String categoryName) {
        QueryWrapper<Category> query = new QueryWrapper<>();
        query.eq("category_name", categoryName);
        Category one = categoryService.getOne(query);

        return ResultVO.success(one.getCategoryParent());
    }

    /**
     * 删除分类
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping
    @Transactional
    public ResultVO deleteCategory(@RequestParam("categoryId") Integer categoryId) {
        AtomicBoolean remove = new AtomicBoolean(false);
        Optional.ofNullable(categoryService.getById(categoryId)).ifPresent(
            category -> {
                FileUtil.deleteFile(category.getCategoryImg());
                boolean b = categoryService.removeById(categoryId);
                remove.set(b);

            }
        );


        return remove.get() ? ResultVO.success() : ResultVO.error();
    }
    @CrossOrigin(origins = "*")
    @GetMapping("/getCategoryById")
    public ResultVO getCategoryById(@RequestParam("categoryId") Integer categoryId) {
        return Optional.ofNullable(categoryService.getById(categoryId))
                .map(ResultVO::success)
                .orElseGet(() -> ResultVO.error("未找到对应的类别"));
    }
    /**
     * 更新分类
     */
    @CrossOrigin(origins = "*")
    @PutMapping
    public ResultVO updateCategory(@RequestBody Category category) {
        boolean b = categoryService.updateById(category);
        return b ? ResultVO.success() : ResultVO.error();
    }
}
