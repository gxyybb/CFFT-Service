package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cfft.beans.*;
import com.example.cfft.beans.vo.MushroomDTO;
import com.example.cfft.beans.vo.MushroomVO;
import com.example.cfft.common.utils.*;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.mapper.CategoryMapper;
import com.example.cfft.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * MushroomController 处理与蘑菇相关的请求。
 */
@Tag(name = "蘑菇管理", description = "处理与蘑菇相关的操作")
@RestController
@RequestMapping("/mushrooms")
public class MushroomController {

    @Autowired
    private MushroomService mushroomService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private MushroomImgService imgService;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private LocationMushroomService locationMushroomService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    private static final String ALL_MUSHROOMS_KEY = "allMushrooms";
    @Operation(summary = "（安卓）根据关键字查询蘑菇列表", description = "根据关键字、可食用性和毒性查询蘑菇列表")
    @CrossOrigin
    @GetMapping("/searchMushroomInLibrary")
    public ResultVO searchMushroomInLibrary(
            @Parameter(description = "搜索关键字", required = true) @RequestParam("key") String key,
            @Parameter(description = "是否可食用", required = true) @RequestParam("isEat") Integer isEat,
            @Parameter(description = "是否有毒", required = true) @RequestParam("isPosion") Integer isPosion,
            @Parameter(description = "页码", required = true) @RequestParam("page") Integer page) {
        QueryWrapper<Mushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_eat", isEat)
                .eq("is_poison", isPosion)
                .like("mushroom_name", key);
        List<Mushroom> list = mushroomService.list(queryWrapper);
        List<MushroomDTO> mushroomDTO = getMushroomDTO(list);
        PageHelper<MushroomDTO> helper = new PageHelper<>(mushroomDTO, page);
        return ResultVO.success(helper.getPageData());
    }

    @Operation(summary = "根据ID获取蘑菇信息", description = "根据蘑菇的ID获取详细信息")
    @CrossOrigin(origins = "*")
    @GetMapping("/{id}")
    public ResultVO getMushroomById(
            @Parameter(description = "蘑菇ID", required = true) @PathVariable Integer id) {
        Mushroom mushroom = mushroomService.getById(id);
        if (mushroom != null) {
            MushroomDTO mushroomDTO = getMushroomDTO(mushroom);
            return ResultVO.success(mushroomDTO);
        } else {
            return ResultVO.failure("未找到ID为" + id + "的蘑菇");
        }
    }

    @Operation(summary = "分页展示蘑菇列表", description = "根据可食用性和毒性分页展示蘑菇列表")
    @GetMapping("/getMushroomInAndroid")
    public ResultVO getMushroomInAndroid(
            @Parameter(description = "是否可食用", required = true) @RequestParam("isEat") Integer isEat,
            @Parameter(description = "是否有毒", required = true) @RequestParam("isPosion") Integer isPosion,
            @Parameter(description = "页码", required = false) @RequestParam(value = "page",  required = false,defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = false) @RequestParam(value = "size" , required = false,defaultValue = "10") Integer size) {
        QueryWrapper<Mushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_eat", isEat)
                .eq("is_poison", isPosion);
        Page<Mushroom> mushroomPage = new Page<>(page, size);
        IPage<Mushroom> mushroomIPage = mushroomService.page(mushroomPage, queryWrapper);
        List<MushroomDTO> mushroomDTOList = getMushroomDTO(mushroomIPage.getRecords());
        PageHelper<MushroomDTO> pageResultVO = new PageHelper<>(mushroomDTOList, size, page);
        return ResultVO.success(pageResultVO.getPageData());
    }

    @Operation(summary = "创建单个蘑菇", description = "创建一个新的蘑菇")
    @CrossOrigin(origins = "*")
    @PostMapping
    @Transactional
    public ResultVO createMushroom(
            @Parameter(description = "蘑菇实体", required = true) @RequestBody Mushroom mushroom) {
        String folderPath = Static.MUSHROOM_IMG + mushroom.getMushroomName();
        mushroom.setMushroomImage(folderPath);
        boolean success = mushroomService.save(mushroom);
        if (success) {
            boolean directory = FileUtil.createDirectory(folderPath);
            if (directory) {
                mushroom.setMushroomImage(folderPath);
            } else {
                return ResultVO.failure(mushroom.getMushroomName() + "导入失败");
            }
            return ResultVO.success(mushroom);
        } else {
            return ResultVO.failure("创建失败");
        }
    }

    @Operation(summary = "批量创建蘑菇", description = "批量创建多个蘑菇")
    @CrossOrigin(origins = "*")
    @PostMapping("/saveList")
    public ResultVO createMushroomList(
            @Parameter(description = "蘑菇实体列表", required = true) @RequestBody List<Mushroom> mushroomList) {
        int successCount = 0;
        int failureCount = 0;
        StringBuilder failureNames = new StringBuilder();

        for (Mushroom mushroom : mushroomList) {
            ResultVO mushroomResult = createMushroom(mushroom);
            if (mushroomResult.getCode() == 200) {
                successCount++;
            } else {
                failureCount++;
                failureNames.append(mushroom.getMushroomName()).append(" ");
            }
        }

        String resultMessage = String.format("共录入 %d 条数据，录入失败 %d 条数据\n失败数据：%s", successCount, failureCount, failureNames);
        return ResultVO.success(resultMessage);
    }

    @Operation(summary = "删除蘑菇", description = "根据ID删除蘑菇")
    @CrossOrigin(origins = "*")
    @DeleteMapping("/{id}")
    @Transactional
    public ResultVO deleteMushroom(
            @Parameter(description = "蘑菇ID", required = true) @PathVariable Integer id) {
        Mushroom byId = mushroomService.getById(id);
        boolean success = mushroomService.removeById(id);
        if (success) {
            FileUtil.deleteFile(byId.getMushroomImage());
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.failure("删除失败");
        }
    }

    @Operation(summary = "根据分类名称获取蘑菇列表", description = "根据分类名称获取对应的蘑菇列表")
    @CrossOrigin(origins = "*")
    @GetMapping("/getMushroomByCategoryName/{name}")
    public ResultVO getMushroomByCategoryName(
            @Parameter(description = "分类名称", required = true) @PathVariable String name) {
        QueryWrapper<Category> categoryQueryWrapper = new QueryWrapper<>();
        categoryQueryWrapper.eq("category_name", name);
        Category one = categoryService.getOne(categoryQueryWrapper);
        return getMushroomByCategory(one.getCategoryId());
    }

    @Operation(summary = "根据分类ID获取蘑菇列表", description = "根据分类ID获取对应的蘑菇列表")
    @CrossOrigin(origins = "*")
    @GetMapping("/getMushroomByCategory/{id}")
    public ResultVO getMushroomByCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Integer id) {
        QueryWrapper<Mushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", id);
        return ResultVO.success(mushroomService.list(queryWrapper));
    }

    @Operation(summary = "获取蘑菇所有信息", description = "根据蘑菇ID获取所有图片信息")
    @CrossOrigin(origins = "*")
    @GetMapping("/getMushroomImgs/{id}")
    public ResultVO getMushroomImgs(
            @Parameter(description = "蘑菇ID", required = true) @PathVariable String id) {
        QueryWrapper<MushroomImg> query = new QueryWrapper<>();
        query.eq("mushroom_id", id);
        List<MushroomImg> list = imgService.list(query);
        if (list != null) {
            for (MushroomImg img : list) {
                img.setImgUrl(PathUtil.convertToHttpUrl(img.getImgUrl()));
            }
        }
        return ResultVO.success(list);
    }

    @Operation(summary = "删除蘑菇图片", description = "根据图片ID删除蘑菇图片")
    @CrossOrigin(origins = "*")
    @DeleteMapping("/deleteMushroomImg")
    public ResultVO deleteMushroomImgs(
            @Parameter(description = "图片ID", required = true) @RequestParam("id") Integer id) {
        boolean b = imgService.removeById(id);
        if (b) {
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.failure("删除失败，请刷新页面");
        }
    }

    @Operation(summary = "根据关键字搜索蘑菇", description = "根据关键字搜索蘑菇列表")
    @CrossOrigin(origins = "*")
    @GetMapping("/search/{keyword}")
    public ResultVO getSearchByKeyword(
            @Parameter(description = "搜索关键字", required = true) @PathVariable String keyword) {
        QueryWrapper<Mushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("mushroom_name", "%" + keyword + "%");
        List<Mushroom> list = mushroomService.list(queryWrapper);
        List<MushroomVO> mushroomVOList = list.stream()
                .distinct()
                .map(mushroom -> {
                    Integer categoryId = mushroom.getCategoryId();
                    return Optional.ofNullable(categoryService.getById(categoryId))
                            .map(category -> getMushroomVO(mushroom, category))
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();
        return ResultVO.success(mushroomVOList);
    }

    @Operation(summary = "获取所有蘑菇", description = "获取所有蘑菇信息")
    @CrossOrigin(origins = "*")
    @GetMapping
    public ResultVO listAllMushrooms() {
        List<MushroomVO> mushroomVOList;
        // 尝试从缓存中获取数据
        mushroomVOList = (List<MushroomVO>) redisTemplate.opsForValue().get(ALL_MUSHROOMS_KEY);
        if (mushroomVOList != null) {
            return ResultVO.success(mushroomVOList);
        }

        // 缓存中没有数据，从数据库中获取
        List<Mushroom> list = mushroomService.list();
        mushroomVOList = list.stream()
                .distinct()
                .map(mushroom -> {
                    Integer categoryId = mushroom.getCategoryId();
                    return Optional.ofNullable(categoryService.getById(categoryId))
                            .map(category -> getMushroomVO(mushroom, category))
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 将数据缓存到Redis中
        redisTemplate.opsForValue().set(ALL_MUSHROOMS_KEY, mushroomVOList, 20, TimeUnit.MINUTES);

        return ResultVO.success(mushroomVOList);
    }

    @Operation(summary = "上传蘑菇的图片", description = "根据蘑菇ID上传图片")
    @CrossOrigin(origins = "*")
    @PostMapping("/saveImage")
    public ResultVO saveMushroomIMG(
            @Parameter(description = "蘑菇ID", required = true) @RequestParam("id") String id,
            @Parameter(description = "图片文件", required = true) @RequestParam("imageFile") MultipartFile imageFile) {
        Integer i = Integer.valueOf(id);
        return mushroomService.saveImage(i, imageFile);
    }

    @Operation(summary = "更新多个蘑菇", description = "批量更新蘑菇信息")
    @PutMapping("/updateList")
    @CrossOrigin(origins = "*")
    public ResultVO update(
            @Parameter(description = "蘑菇实体列表", required = true) @RequestParam("list") List<Mushroom> list) {
        int x = list.size();
        int y = 0;
        List<Mushroom> list1 = new ArrayList<>();
        for (Mushroom m : list) {
            ResultVO resultVO = updateOne(m);
            if (resultVO.getCode() == 200) {
                y++;
            } else {
                list1.add(m);
            }
        }
        return ResultVO.success("更新成功" + y + "条记录" + "更新失败" + (x - y) + "条记录", list1);
    }

    @Operation(summary = "更新单个蘑菇", description = "根据蘑菇ID更新蘑菇信息")
    @CrossOrigin(origins = "*")
    @PutMapping("/{id}")
    public ResultVO updateMushroom(
            @Parameter(description = "蘑菇ID", required = true) @PathVariable Integer id,
            @Parameter(description = "更新后的蘑菇实体", required = true) @RequestBody Mushroom updatedMushroom) {
        updatedMushroom.setMushroomId(id);
        boolean success = mushroomService.updateById(updatedMushroom);
        if (success) {
            return ResultVO.success(updatedMushroom);
        } else {
            return ResultVO.failure("更新失败");
        }
    }

    @Operation(summary = "删除蘑菇图片", description = "根据图片ID删除蘑菇图片")
    @CrossOrigin(origins = "*")
    @DeleteMapping("/image")
    @Transactional
    public ResultVO removeImage(
            @Parameter(description = "图片ID", required = true) @RequestParam("imageId") Integer imageId) {
        MushroomImg byId = imgService.getById(imageId);
        String imgUrl = byId.getImgUrl();
        FileUtil.deleteFile(imgUrl);
        imgService.removeById(imageId);
        return ResultVO.success();
    }

    @Operation(summary = "更新单个蘑菇", description = "更新蘑菇信息")
    @PutMapping("/update")
    @CrossOrigin(origins = "*")
    public ResultVO updateOne(
            @Parameter(description = "蘑菇实体", required = true) @RequestBody Mushroom mushroom) {
        mushroom.setMushroomImage(null);
        boolean b = mushroomService.updateById(mushroom);
        return b ? ResultVO.success("更新成功") : ResultVO.error("更新失败");
    }

    private MushroomDTO getMushroomDTO(Mushroom mushroom) {
        MushroomDTO mushroomDTO = new MushroomDTO();
        BeanUtils.copyProperties(mushroom, mushroomDTO);

        // 查询蘑菇分布的地点
        QueryWrapper<LocationMushroom> locationMushroomQueryWrapper = new QueryWrapper<>();
        locationMushroomQueryWrapper.eq("mushroom_id", mushroom.getMushroomId());

        StringBuilder locationBuilder = new StringBuilder();
        locationMushroomService.list(locationMushroomQueryWrapper).stream().distinct().map(
                locationMushroom -> locationService.getById(locationMushroom.getLocationId()).getProvince()
        ).forEach(s -> {
            if (locationBuilder.length() > 0) {
                locationBuilder.append("，");
            }
            locationBuilder.append(s);
        });

        if (locationBuilder.length() > 0) {
            locationBuilder.append("。");
        }
        mushroomDTO.setMushroomLocation(locationBuilder.toString());

        // 查询蘑菇图片
        QueryWrapper<MushroomImg> imgQuery = new QueryWrapper<>();
        imgQuery.eq("mushroom_id", mushroom.getMushroomId());
        List<MushroomImg> imgList = imgService.list(imgQuery);
        if (imgList != null && !imgList.isEmpty()) {
            imgList.forEach(img -> img.setImgUrl(PathUtil.convertToHttpUrl(img.getImgUrl())));
        } else {
            MushroomImg img = new MushroomImg();
            img.setImgUrl(PathUtil.convertToHttpUrl(Static.DefaultImage));
            imgList = Collections.singletonList(img);
        }
        mushroomDTO.setMushroomImages(imgList);

        // 查询蘑菇分布位置
        List<LocationMushroom> locationMushrooms = locationMushroomService.list(locationMushroomQueryWrapper);
        List<Location> locations = locationMushrooms.stream()
                .map(locationMushroom -> locationService.getById(locationMushroom.getLocationId()))
                .peek(location -> location.setDescription(mushroom.getMushroomName()+":\n"+mushroom.getMushroomDesc()))
                .collect(Collectors.toList());
        mushroomDTO.setLocations(locations);

        // 查询蘑菇分类路径
        try {
            Category category = categoryService.getById(mushroom.getCategoryId());
            String categoryPath = categoryMapper.getCategorysByCategoryId(category.getCategoryId());
            mushroomDTO.setCategory(categoryPath);
        } catch (Exception e) {
            // 处理异常情况，如递归查询超出限制等
            mushroomDTO.setCategory("该菌菇分类未定");
            // 记录日志或其他处理
        }

        return mushroomDTO;
    }


    private List<MushroomDTO> getMushroomDTO(List<Mushroom> list) {
        List<MushroomDTO> list1 = new ArrayList<>();
        for (Mushroom m : list) {
            MushroomDTO mushroomDTO = getMushroomDTO(m);
            list1.add(mushroomDTO);
        }
        return list1;
    }

    private MushroomVO getMushroomVO(Mushroom room, Category category) {
        MushroomVO mushroomVO = new MushroomVO();
        mushroomVO.setMushroomId(room.getMushroomId());
        mushroomVO.setMushroomName(room.getMushroomName());
        QueryWrapper<LocationMushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mushroom_id", room.getMushroomId());
        List<LocationMushroom> list1 = locationMushroomService.list(queryWrapper);
        List<Location> collect = list1.stream()
                .map(locationMushroom -> locationService.getById(locationMushroom.getLocationId()))
                .toList();
        mushroomVO.setLocations(collect);
        mushroomVO.setCategory(Optional.ofNullable(category.getCategoryName()).orElse("Unknown Category"));
        mushroomVO.setIsEat((room.getIsEat() == 1) ? "是" : "否");
        mushroomVO.setMushroomLocation(room.getMushroomLocation());
        mushroomVO.setMushroomDesc(room.getMushroomDesc());
        mushroomVO.setIsPoison((room.getIsPoison() == 1) ? "是" : "否");
        return mushroomVO;
    }
}
