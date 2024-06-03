package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cfft.beans.*;
import com.example.cfft.beans.vo.MushroomDTO;
import com.example.cfft.beans.vo.MushroomVO;
import com.example.cfft.common.utils.FileUtil;
import com.example.cfft.common.utils.PageHelper;
import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.utils.Static;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.mapper.CategoryMapper;
import com.example.cfft.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MushroomController 处理与蘑菇相关的请求。
 */
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


    /**
     * （安卓）根据关键字查询蘑菇列表
     * @param key
     * @param isEat
     * @param isPosion
     * @param page
     * @return
     */
    @CrossOrigin
    @GetMapping("/searchMushroomInLibrary")
    public ResultVO searchMushroomInLibrary(@RequestParam("key") String key,@RequestParam("isEat")Integer isEat,@RequestParam("isPosion") Integer isPosion ,@RequestParam("page") Integer page) {
        QueryWrapper<Mushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_eat",isEat)
                        .eq("is_poison",isPosion)
                .like("mushroom_name",key);
        List<Mushroom> list = mushroomService.list(queryWrapper);
        List<MushroomDTO> mushroomDTO = getMushroomDTO(list);
        PageHelper<MushroomDTO> helper = new PageHelper<>(mushroomDTO,page);
        return ResultVO.success(helper.getPageData());

    }


    /**
     * 根据ID获取蘑菇信息。
     *
     * @param id 蘑菇的ID。
     * @return 包含蘑菇信息的响应。
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/{id}")
    public ResultVO getMushroomById(@PathVariable Integer id) {
        Mushroom mushroom = mushroomService.getById(id);
        if (mushroom != null) {
            MushroomDTO mushroomDTO = getMushroomDTO(mushroom);
            return ResultVO.success(mushroomDTO);
        } else {
            return ResultVO.failure("未找到ID为" + id + "的蘑菇");
        }
    }


    /**
     * 分页展示蘑菇列表
     * @param isEat
     * @param isPosion
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/getMushroomInAndroid")
    public ResultVO getMushroomInAndroid(@RequestParam("isEat") Integer isEat,
                                         @RequestParam("isPosion") Integer isPosion,
                                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                                         @RequestParam(value = "size", defaultValue = "10") Integer size) {

        // 构建查询条件
        QueryWrapper<Mushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_eat", isEat)
                .eq("is_poison", isPosion);
        // 使用MyBatis-Plus自带的分页查询功能
        Page<Mushroom> mushroomPage = new Page<>(page, size);
        IPage<Mushroom> mushroomIPage = mushroomService.page(mushroomPage, queryWrapper);
        // 将查询结果转换为DTO对象
        List<MushroomDTO> mushroomDTOList = getMushroomDTO(mushroomIPage.getRecords());
        // 构建返回结果
        PageHelper<MushroomDTO> pageResultVO = new PageHelper<>(mushroomDTOList,size,page);
        return ResultVO.success(pageResultVO.getPageData());
    }





















    /**
     * 创建单个蘑菇。
     *
     * @param mushroom 要创建的蘑菇实例。
     * @return 创建结果的响应。
     */
    @CrossOrigin(origins = "*")
    @PostMapping
    @Transactional
    public ResultVO createMushroom(@RequestBody Mushroom mushroom) {
        String folderPath = Static.MUSHROOM_IMG + mushroom.getMushroomName();
        mushroom.setMushroomImage(folderPath);
        boolean success = mushroomService.save(mushroom);
        if (success) {
            // 创建文件夹
            boolean directory = FileUtil.createDirectory(folderPath);

            if (directory) {

                mushroom.setMushroomImage(folderPath);

            }else {
                ResultVO.failure(mushroom.getMushroomName()+"导入失败");
            }

            return ResultVO.success(mushroom);
        } else {
            return ResultVO.failure("创建失败");
        }
    }

    /**
     * 批量创建蘑菇。
     *
     * @param mushroomList 包含要创建的蘑菇列表。
     * @return 创建结果的响应，包含成功和失败的数量以及失败的蘑菇名称。
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/saveList")
    public ResultVO createMushroomList(@RequestBody List<Mushroom> mushroomList) {
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


    /**
     * 删除蘑菇。
     *
     * @param id 要删除的蘑菇的ID。
     * @return 删除结果的响应。
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping("/{id}")
    @Transactional
    public ResultVO deleteMushroom(@PathVariable Integer id) {
        Mushroom byId = mushroomService.getById(id);
        boolean success = mushroomService.removeById(id);
        if (success) {
            FileUtil.deleteFile(byId.getMushroomImage());
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.failure("删除失败");
        }
    }
    /**
     * 根据分类名称获取蘑菇列表。
     *
     * @param name 分类名称。
     * @return 包含与指定分类名称关联的蘑菇的响应。
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/getMushroomByCategoryName/{name}")
    public ResultVO getMushroomByCategoryName(@PathVariable String name) {
        QueryWrapper<Category> categoryQueryWrapper = new QueryWrapper<>();
        categoryQueryWrapper.eq("category_name", name);

        Category one = categoryService.getOne(categoryQueryWrapper);
        return getMushroomByCategory(one.getCategoryId());
    }

    /**
     * 根据分类ID获取蘑菇列表。
     *
     * @param id 分类ID。
     * @return 包含与指定分类关联的蘑菇的响应。
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/getMushroomByCategory/{id}")
    public ResultVO getMushroomByCategory(@PathVariable Integer id) {
        QueryWrapper<Mushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", id);
        return ResultVO.success(mushroomService.list(queryWrapper));
    }

    /**
     * 获取蘑菇所有信息
     * @param id
     * @return
     */

    @CrossOrigin(origins = "*")
    @GetMapping("/getMushroomImgs/{id}")
    public ResultVO getMushroomImgs(@PathVariable String id){
        QueryWrapper<MushroomImg> query = new QueryWrapper<MushroomImg>();
        query.eq("mushroom_id", id);
        List<MushroomImg> list = imgService.list(query);
        if (list!=null){
            for (MushroomImg img: list){
                img.setImgUrl(PathUtil.convertToHttpUrl(img.getImgUrl()));
            }
        }
        return ResultVO.success(list);
    }

    /**
     * 删除蘑菇图片
     * @param id
     * @return
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping("/deleteMushroomImg")
    public ResultVO deleteMushroomImgs(@RequestParam("id") Integer id){
        boolean b = imgService.removeById(id);
        if (b) {
            return ResultVO.success("删除成功");
        }else {
            return ResultVO.failure("删除失败，请刷新页面");
        }
    }




















    /**
     * 根据关键字搜索蘑菇。
     *
     * @param keyword 搜索蘑菇的关键字。
     * @return 包含与搜索关键字匹配的蘑菇的响应。
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/search/{keyword}")
    public ResultVO getSearchByKeyword(@PathVariable String keyword) {
        QueryWrapper<Mushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("mushroom_name", "%" + keyword + "%");
        return ResultVO.success(mushroomService.list(queryWrapper));
    }


    /**
     * （服务器）获取所有蘑菇
     * @return
     */
    @CrossOrigin(origins = "*")
    @GetMapping
    public ResultVO listAllMushrooms() {
        List<Mushroom> list = mushroomService.list();
        List<MushroomVO> mushroomVOList = list.stream()
                .distinct()
                .map(mushroom -> {
                    Integer categoryId = mushroom.getCategoryId();
                    return Optional.ofNullable(categoryService.getById(categoryId))
                            .map(category -> getMushroomVO(mushroom, category))
                            .orElse(null); // 返回 null 以便后续过滤
                })
                .filter(Objects::nonNull) // 过滤掉 null 值
                .collect(Collectors.toList());

        return ResultVO.success(mushroomVOList);
    }







    /**
     * 上传蘑菇的图片。
     *
     * @param id         要上传的蘑菇的ID。
     * @param imageFile  包含新图片的文件。
     * @return 更新结果的响应。
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/saveImage")
    public ResultVO saveMushroomIMG(@RequestParam("id") String id, @RequestParam("imageFile") MultipartFile imageFile) {
        Integer i = Integer.valueOf(id);
        return mushroomService.saveImage(i, imageFile);
    }



    /**
     * 更新多个蘑菇
     * @param list
     * @return
     */

    @PutMapping("/updateList")
    @CrossOrigin(origins = "*")
    public ResultVO update(@RequestParam("list")List<Mushroom> list){
        int x = list.size();
        int y = 0;
        List<Mushroom> list1 = new ArrayList<>();
        for (Mushroom m: list){
            ResultVO resultVO = updateOne(m);
            if (resultVO.getCode()==200){
                y++;
            }else {
                list1.add(m);
            }
        }
        return ResultVO.success("更新成功"+y+"条记录"+"更新失败"+(x-y)+"条记录",list1);
    }


    /**
     * 更新蘑菇信息。
     *
     * @param id             要更新的蘑菇的ID。
     * @param updatedMushroom 包含更新信息的蘑菇实例。
     * @return 更新结果的响应。
     */
    @CrossOrigin(origins = "*")
    @PutMapping("/{id}")
    public ResultVO updateMushroom(@PathVariable Integer id, @RequestBody Mushroom updatedMushroom) {

        updatedMushroom.setMushroomId(id);
        boolean success = mushroomService.updateById(updatedMushroom);
        if (success) {
            return ResultVO.success(updatedMushroom);
        } else {
            return ResultVO.failure("更新失败");
        }

    }



    /**
     * 删除蘑菇图片
     * @param imageId
     * @return
     */



    @CrossOrigin(origins = "*")
    @DeleteMapping("/image")
    @Transactional
    public ResultVO removeImage(@RequestParam("imageId") Integer imageId){
        MushroomImg byId = imgService.getById(imageId);
        String imgUrl = byId.getImgUrl();
        FileUtil.deleteFile(imgUrl);
        imgService.removeById(imageId);
        return ResultVO.success();
    }





    /**
     * 更新单个蘑菇
     * @param mushroom
     * @return
     */
    @PutMapping("/update")
    @CrossOrigin(origins = "*")

    public ResultVO updateOne(@RequestBody Mushroom mushroom){
        mushroom.setMushroomImage(null);
        boolean b = mushroomService.updateById(mushroom);
        return b? ResultVO.success("更新成功"):ResultVO.error("更新失败");
    }







    private  MushroomDTO getMushroomDTO(Mushroom mushroom){
        MushroomDTO mushroomDTO = new MushroomDTO();
        BeanUtils.copyProperties(mushroom, mushroomDTO);
        QueryWrapper<MushroomImg> query = new QueryWrapper<MushroomImg>();
        query.eq("mushroom_id", mushroom.getMushroomId());
        List<MushroomImg> list = imgService.list(query);
        if (list != null && !list.isEmpty()) {
            for (MushroomImg m : list) {
                m.setImgUrl(PathUtil.convertToHttpUrl(m.getImgUrl()));
            }
        } else {
            list = new ArrayList<>(); // 创建一个空的列表
            MushroomImg img = new MushroomImg();
            img.setImgUrl(PathUtil.convertToHttpUrl(Static.DefaultImage));
            list.add(img);
        }
        QueryWrapper<LocationMushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mushroom_id",mushroom.getMushroomId());
        List<LocationMushroom> list1 = locationMushroomService.list(queryWrapper);
        List<Location> collect = list1.stream().map(locationMushroom -> locationService.getById(locationMushroom.getLocationId())).toList();
        mushroomDTO.setLocations(collect);
        mushroomDTO.setMushroomImages(list);
        Category byId = categoryService.getById(mushroom.getCategoryId());
        String categorysByCategoryId = categoryMapper.getCategorysByCategoryId(byId.getCategoryId());
        mushroomDTO.setCategory(categorysByCategoryId);
        return mushroomDTO;
    }
    private List<MushroomDTO> getMushroomDTO(List<Mushroom> list){
        List<MushroomDTO> list1 = new ArrayList<>();
        for (Mushroom m: list){
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
        queryWrapper.eq("mushroom_id",room.getMushroomId());
        List<LocationMushroom> list1 = locationMushroomService.list(queryWrapper);
        List<Location> collect = list1.stream().map(locationMushroom -> locationService.getById(locationMushroom.getLocationId())).toList();
        mushroomVO.setLocations(collect);
        // Adding null check for category.getCategoryName()
        if (category.getCategoryName() != null) {
            mushroomVO.setCategory(category.getCategoryName());
        } else {
            mushroomVO.setCategory("Unknown Category");
        }

        // Adding null checks for other properties
        mushroomVO.setIsEat((room.getIsEat()==1) ? "是" : "否");
        mushroomVO.setMushroomLocation(room.getMushroomLocation());
        mushroomVO.setMushroomDesc(room.getMushroomDesc());
        mushroomVO.setIsPoison((room.getIsPoison()==1) ? "是" : "否");
        return mushroomVO;
    }


}
