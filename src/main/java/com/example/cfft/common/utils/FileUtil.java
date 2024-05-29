package com.example.cfft.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class FileUtil{
    public static byte[] readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }
    public static void deleteFile(String filePath) {
        File file;
        if(filePath!= null) {
             file = new File(filePath);
        } else
            return;
        // 检查文件或目录是否存在
        if (file.exists()) {
            // 如果是目录，递归删除目录及其内容
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                // 如果是文件，直接删除文件
                file.delete();
                System.out.println("文件已删除：" + filePath);
            }
        } else {
            System.out.println("文件或目录不存在：" + filePath);
        }

    }

    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归删除子目录
                    deleteDirectory(file);
                } else {
                    // 删除子文件
                    file.delete();
                    System.out.println("文件已删除：" + file.getAbsolutePath());
                }
            }
        }

        // 删除空目录
        directory.delete();
        System.out.println("目录已删除：" + directory.getAbsolutePath());
    }
    /**
     * 将文件存储在指定路径下
     * @param path 目标路径
     * @param file 要存储的文件
     * @return 文件存储后的绝对路径，如果存储失败则返回null
     */
    public static String saveFile(String path, MultipartFile file) {
        try {
            // 构造目标文件对象
            File directory = new File(path);
            if (!directory.exists()) {
                // 如果目标文件所在目录不存在，则创建目录
                if (!directory.mkdirs()) {
                    System.out.println("创建目录失败：" + directory.getAbsolutePath());
                    return null;
                }
            }

            File dest = new File(directory, file.getOriginalFilename());

            // 将文件保存到目标路径
            file.transferTo(dest);

            // 返回文件存储后的绝对路径
            return dest.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean createDirectory(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            return folder.mkdirs();
        }
        return true;
    }
    public static String storeFile(MultipartFile file, String uploadDir) {
        Path targetDir = Paths.get(uploadDir);
        try {
            // 如果目录不存在，创建它
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // 获取当前时间戳作为文件名
            String fileName = file.getOriginalFilename();

            // 构建目标路径
            Path targetPath = targetDir.resolve(fileName);

            // 复制文件到目标路径
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 返回存储后的文件绝对路径
            return targetPath.toAbsolutePath().toString();
        } catch (IOException e) {
            e.printStackTrace();
            // 处理异常
            return null; // 或者抛出自定义异常，具体根据实际情况处理
        }
    }
    public static String extractFrame(String videoFilePath) {
        try {
            // 获取视频文件的绝对路径和文件名
            File videoFile = new File(videoFilePath);
            String videoFileAbsolutePath = videoFile.getAbsolutePath();

            // 构建FFmpeg命令
            String parentDir = videoFile.getParent();
            String outputImagePath = parentDir + File.separator + "frame_5.jpg";

            // 构建 FFmpeg 命令，将文件名用于输出图片
            String ffmpegCommand = "ffmpeg -i " + videoFileAbsolutePath + " -ss 4 -vframes 1 -q:v 2 " + outputImagePath;

            // 打印 FFmpeg 命令，便于调试
            System.out.println("FFmpeg command: " + ffmpegCommand);

            // 执行命令
            Process process = Runtime.getRuntime().exec(ffmpegCommand);
            int exitValue = process.waitFor(); // 等待 FFmpeg 命令执行完成

            // 打印命令执行结果，便于调试
            System.out.println("FFmpeg command exit value: " + exitValue);

            if (exitValue == 0) {
                return outputImagePath; // 提取成功，返回图片路径
            } else {
                // 提取失败，打印错误信息
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                System.out.println("FFmpeg command execution error:");
                while ((line = errorReader.readLine()) != null) {
                    System.out.println(line);
                }
                return Static.DEFAULT_COVER_IMAGE_PATH;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // 处理异常，打印异常信息
            return Static.DEFAULT_COVER_IMAGE_PATH;
        }
    }
    public static String extractAudio(String audioFilePath) {
        try {
            // 获取音频文件的绝对路径和文件名
            File audioFile = new File(audioFilePath);
            String audioFileAbsolutePath = audioFile.getAbsolutePath();

            // 构建FFmpeg命令
            String parentDir = audioFile.getParent();
            String outputAudioPath = parentDir + File.separator + LocalDate.now() + ".wav";

            // 构建 FFmpeg 命令，将文件名用于输出音频
            String[] command = {"ffmpeg", "-y", "-i", audioFileAbsolutePath, "-ar", "16000", "-ac", "1", "-c:a", "pcm_s16le", outputAudioPath};

            // 执行命令并捕获输出
            String result = executeFFmpegCommand(command);
            if ("success".equals(result)) {
                return outputAudioPath; // 提取成功，返回音频路径
            } else {
                return null; // 提取失败
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String executeFFmpegCommand(String[] command) {
        try {
            // 构建 ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 创建一个线程来读取错误流
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
            errorGobbler.start();

            // 等待 FFmpeg 命令执行完成，并设置超时
            boolean finished = process.waitFor(60, java.util.concurrent.TimeUnit.SECONDS);

            if (!finished) {
                // 如果 FFmpeg 命令没有在指定时间内完成，则强制终止
                process.destroyForcibly();
                throw new RuntimeException("FFmpeg 命令执行超时");
            }

            // 获取退出码
            int exitCode = process.exitValue();

            // 打印 FFmpeg 命令和输出信息，便于调试
            System.out.println("FFmpeg command: " + String.join(" ", command));
            System.out.println("FFmpeg command exit value: " + exitCode);
            System.out.println("FFmpeg command output: " + errorGobbler.getOutput());

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg 执行失败，错误信息：" + errorGobbler.getOutput());
            }

            return "success"; // 表示成功执行
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    // 内部类，用于处理流
    private static class StreamGobbler extends Thread {
        private InputStream inputStream;
        private String type;
        private StringBuilder output = new StringBuilder();

        public StreamGobbler(InputStream inputStream, String type) {
            this.inputStream = inputStream;
            this.type = type;
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getOutput() {
            return output.toString();
        }
    }
}
