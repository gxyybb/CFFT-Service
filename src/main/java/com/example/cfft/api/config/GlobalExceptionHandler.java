package com.example.cfft.api.config;

import com.example.cfft.common.vo.ResultVO;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public ResultVO handleMultipartException(MultipartException ex) {
        ex.printStackTrace();
        return ResultVO.failure("File upload error: " + ex.getMessage());
    }
}
