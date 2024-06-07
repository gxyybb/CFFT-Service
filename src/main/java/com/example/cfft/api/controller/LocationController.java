package com.example.cfft.api.controller;

import com.example.cfft.beans.Location;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Tag(name = "位置管理", description = "处理位置相关操作")
@RequestMapping("/location")
public class LocationController {
    @Autowired
    private LocationService locationService;
    @Operation(summary = "保存位置", description = "保存新的位置信息")
    @PostMapping
    @CrossOrigin("*")
    public ResultVO saveLocation(
            @Parameter(description = "位置信息", required = true) @RequestBody Location location) {
        return Optional.ofNullable(location).map(lo ->{
            boolean save = locationService.save(location);
            return save?ResultVO.success(location):ResultVO.error("保存失败",location);
        }).orElse(ResultVO.error("保存失败",location));
    }

    @Operation(summary = "批量保存位置", description = "批量保存多个位置信息")
    @PostMapping("/list")
    @CrossOrigin("*")
    public ResultVO saveLocationList(
            @Parameter(description = "位置信息列表", required = true) @RequestBody List<Location> list) {
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
    @Operation(summary = "获取位置列表", description = "获取所有位置的信息")
    @GetMapping
    @CrossOrigin("*")
    public ResultVO getList(){
        return ResultVO.success(locationService.list());
    }
    @Operation(summary = "更新位置", description = "更新现有的位置信息")
    @PutMapping
    @CrossOrigin
    public ResultVO update(
            @Parameter(description = "位置信息", required = true) @RequestBody Location location) {
        return ResultVO.success(locationService.updateById(location));
    }
    @Operation(summary = "删除位置", description = "根据位置ID删除位置")
    @DeleteMapping
    @CrossOrigin("*")
    public ResultVO removeLocation(
            @Parameter(description = "位置ID", required = true) @RequestParam("id") Integer locationId) {
        locationService.removeById(locationId);
        return ResultVO.success();
    }
}
