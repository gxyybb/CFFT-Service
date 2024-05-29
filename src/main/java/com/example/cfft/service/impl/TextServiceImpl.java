package com.example.cfft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cfft.beans.Text;
import com.example.cfft.mapper.TextMapper;
import com.example.cfft.service.TextService;
import org.springframework.stereotype.Service;

@Service
public class TextServiceImpl extends ServiceImpl<TextMapper, Text> implements TextService {
}
