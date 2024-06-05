package com.example.cfft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cfft.beans.Location;
import com.example.cfft.service.LocationService;
import com.example.cfft.mapper.LocationMapper;
import org.springframework.stereotype.Service;

/**
* @author 14847
* @description 针对表【location】的数据库操作Service实现
* @createDate 2024-06-03 09:53:46
*/
@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location>
    implements LocationService{

}




