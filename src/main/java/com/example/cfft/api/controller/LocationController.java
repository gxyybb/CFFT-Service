package com.example.cfft.api.controller;

import com.example.cfft.beans.Location;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/location")
public class LocationController {
    @Autowired
    private LocationService locationService;
    @PostMapping
    @CrossOrigin("*")
    public ResultVO saveLocation(@RequestBody Location location){
        return Optional.ofNullable(location).map(lo ->{
            boolean save = locationService.save(location);
            return save?ResultVO.success(location):ResultVO.error("保存失败",location);
        }).orElse(ResultVO.error("保存失败",location));
    }

    @PostMapping("/list")
    @CrossOrigin("*")
    public ResultVO saveLocationList(@RequestBody List<Location> list) {
        int x = list.size();
        List<ResultVO> results = list.stream()
                .map(this::saveLocation)
                .toList();

        List<Object> failedLocations = results.stream()
                .filter(resultVO -> resultVO.getCode() == 500)
                .map(ResultVO::getData)
                .collect(Collectors.toList());

        int y = failedLocations.size();
        int successCount = x - y;

        if (y == 0) {
            return ResultVO.success("共保存" + x + "条数据，全部成功保存。", list);
        } else {
            String message = "共保存" + x + "条数据，其中成功保存" + successCount + "条，失败" + y + "条。";
            return ResultVO.error(message, failedLocations);
        }
    }
    @GetMapping
    @CrossOrigin("*")
    public ResultVO getList(){
        return ResultVO.success(locationService.list());
    }
    @PutMapping
    @CrossOrigin
    public ResultVO update(@RequestBody Location location){
        return ResultVO.success(locationService.updateById(location));
    }
    @DeleteMapping
    @CrossOrigin("*")
    public ResultVO removeLocation(@RequestParam("id")Integer locationId){
        locationService.removeById(locationId);
        return ResultVO.success();
    }
}
