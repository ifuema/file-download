package com.ifuema.filedownload.vo;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DownloadLog {
    public void printLog(List<String> stringList) {
        System.out.println();
        for (String string : stringList) {
            System.out.println(string);
        }
    }
}
