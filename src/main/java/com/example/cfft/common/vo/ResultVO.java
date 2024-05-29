package com.example.cfft.common.vo;

import lombok.Getter;

//相应给前端的状态码和信息返回值均为json类型

@Getter
public class ResultVO{
/*code:200表示成功响应500表示响应失败（不管是成功还是失败，有需要的话都有msg和date）
* msg是响应的字符串
* data是响应的数据
* */
    private int code;
    private String msg;
    private Object data;


    private ResultVO(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ResultVO successYY(String msg,Object data){
        return new ResultVO(205,msg,data);
    }
    public static ResultVO successYY(String msg){
        return successYY(msg,null);
    }
    public static ResultVO successYY(Object object){
        return successYY(null,object);
    }
    public static ResultVO success(String msg,Object data) {

        return new ResultVO(200, msg, data);
    }
    public static ResultVO success() {

        return new ResultVO(200, null, null);
    }
    public static ResultVO success(Object data) {

        return ResultVO.success(null,data);
    }
    public static ResultVO success(String msg) {

        return  ResultVO.success(msg,null);
    }
    public static ResultVO error(String msg,Object data) {

        return new ResultVO(500, msg, data);
    }
    public static ResultVO error() {

        return new ResultVO(500, null,null);
    }
    public static ResultVO error(Object data) {

        return ResultVO.error(null,data);
    }
    public static ResultVO error(String msg) {

        return  ResultVO.error(msg,null);
    }
    public static ResultVO failure(String msg,Object data) {
        return error(msg,data);
    }public static ResultVO failure(Object data) {
        return error(data);
    }public static ResultVO failure(String msg) {
        return error(msg);
    }public static ResultVO failure() {
        return error();
    }

}
