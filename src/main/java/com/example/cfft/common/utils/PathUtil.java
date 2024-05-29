package com.example.cfft.common.utils;

public class PathUtil{

    public static String convertToHttpUrl(String localPath) {
        // 将本地路径中的反斜杠替换为正斜杠
        localPath = localPath.replace("\\", "/");

        // 从本地路径中提取文件名
        String fileName = localPath.substring(Static.LinuxPathNumber);

        // 构建HTTP URL
        String httpUrl = Static.BASE_URL_FOR_WEB + fileName;

        return httpUrl.replace("\\", "/");
    }

    public static String covertToLinuxUrl(String path) {
        return path.replace("C:\\CFFT", "/root/cfft").replace("\\", "/");
    }

    public static String covertToWindowsUrl(String path) {
        return path.replace("/root/cfft", "C:\\CFFT").replace("/", "\\");
    }


    public static void main(String[] args) {
        System.out.println(convertToHttpUrl("/root/cfft/default.jpg"));;
    }




}
