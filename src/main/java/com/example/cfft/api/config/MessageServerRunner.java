package com.example.cfft.api.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class MessageServerRunner implements CommandLineRunner, DisposableBean{
    @Override
    public void run(String... args) throws Exception {
        // 设置工作目录为 D:\plotDriver\
        String workingDirectory = "C:\\nginx-1.25.3";
        String command = "start nginx";

        executeCommand(workingDirectory, command);
    }
    @Override
    public void destroy() throws Exception {
        String workingDirectory = "C:\\nginx-1.25.3";
        String command = "nginx -s stop";
        executeCommand(workingDirectory, command);
    }


    private void executeCommand(String workingDirectory, String command) {
        if (true) return;
        try {
            Process process = new ProcessBuilder()
                    .command("cmd", "/c", command)
                    .directory(new File(workingDirectory))
                    .start();

            // 等待命令执行完成
            int exitCode = process.waitFor();


            if (exitCode == 0) {
                System.out.println("Command executed successfully.");
            } else {
                System.err.println("Command execution failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
