package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cfft.beans.Location;
import com.example.cfft.beans.LocationMushroom;
import com.example.cfft.beans.Mushroom;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.LocationMushroomService;
import com.example.cfft.service.LocationService;
import com.example.cfft.service.MushroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mushroomLocation")
public class MushroomLocationController {

    @Autowired
    private MushroomService mushroomService;
    @Autowired
    private LocationMushroomService locationMushroomService;
    @Autowired
    private LocationService locationService;

    @PostMapping
    @CrossOrigin("*")
    public ResultVO saveInfo(@RequestParam("mushroomId") Integer mushroomId, @RequestParam("locationId") Integer locationId) {
        Mushroom byId = mushroomService.getById(mushroomId);
        Location byId1 = locationService.getById(locationId);
        if (byId != null && byId1 != null) {
            QueryWrapper<LocationMushroom> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("mushroom_id", mushroomId);
            queryWrapper.eq("location_id", locationId);
            return Optional.ofNullable(locationMushroomService.getOne(queryWrapper)).map(locationMushroom -> ResultVO.error("记录已存在")).orElseGet(() -> {
                LocationMushroom locationMushroom = new LocationMushroom();
                locationMushroom.setLocationId(locationId);
                locationMushroom.setMushroomId(mushroomId);
                boolean save = locationMushroomService.save(locationMushroom);
                return save ? ResultVO.success() : ResultVO.error("保存失败");
            });
        }
        return ResultVO.error("菌菇或地区不存在");
    }

    @PostMapping("/savemore")
    @CrossOrigin("*")
    public ResultVO saveMore(@RequestBody Map<String, Object> request) {
        Integer mushroomId = (Integer) request.get("mushroomId");
        List<Integer> list = (List<Integer>) request.get("location");

        int total = list.size();
        List<Integer> failedLocations = list.stream()
                .filter(locationId -> !saveInfo(mushroomId, locationId).isSuccess())
                .collect(Collectors.toList());
        int successCount = total - failedLocations.size();
        if (successCount == total) {
            return ResultVO.success("所有记录保存成功");
        } else if (successCount == 0) {
            return ResultVO.error("所有记录保存失败", failedLocations);
        } else {
            return ResultVO.success(successCount + " 条记录保存成功，" + failedLocations.size() + " 条记录保存失败", failedLocations);
        }
    }
    @DeleteMapping("/all")
    @CrossOrigin("*")
    public ResultVO removeLocationByMushroomId(@RequestParam("mushroomId")Integer mushroomId){
        QueryWrapper<LocationMushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mushroom_id",mushroomId);
        locationMushroomService.remove(queryWrapper);
        return ResultVO.success();
    }
    @DeleteMapping
    @CrossOrigin("*")
    public ResultVO removeLocationByMushroomIdAndLocationId(@RequestParam("mushroomId")Integer mushroomId,@RequestParam("locationId")Integer locationId){
        QueryWrapper<LocationMushroom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mushroom_id",mushroomId);
        queryWrapper.eq("location_id",locationId);
        locationMushroomService.remove(queryWrapper);
        return ResultVO.success();
    }

}
