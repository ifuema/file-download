package com.ifuema.filedownload.entity;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Scanner;

@Component
public class Folder {
    private String url;
    private Scanner sc = new Scanner(System.in);

    public Folder() {
        while (true) {
            System.out.println("共享文件夹路径：");
            url = sc.nextLine();
            File file = new File(url);
            if (!file.exists()) {
                System.out.println("路径不存在！");
            } else if (!file.isDirectory()) {
                System.out.println("不是一个文件夹路径！");
            } else if (!file.canRead()) {
                System.out.println("拒绝访问！");
            } else {
                break;
            }
        }
    }

    public String getUrl() {
        return url;
    }
}
