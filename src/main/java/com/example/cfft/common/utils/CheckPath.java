package com.example.cfft.common.utils;

public class CheckPath {

    public static void main(String[] args) {
        String path1 = "C:\\CFFT\\user\\zs";
        String path2 = "D:\\CFFT\\user\\zs";
        String path3 = "/root/cfft/user/zs";

        System.out.println(startsWithCColonBackslash(path1)); // true
        System.out.println(startsWithCColonBackslash(path2)); // false
        System.out.println(startsWithCColonBackslash(path3)); // false
    }
    public static boolean startsWithRoot(String path) {
        return path != null && path.startsWith("/root/");
    }
    public static boolean startsWithCColonBackslash(String path) {
        return path != null && path.startsWith("C:\\");
    }
}
