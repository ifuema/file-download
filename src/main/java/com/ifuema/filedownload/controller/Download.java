package com.ifuema.filedownload.controller;

import com.ifuema.filedownload.entity.Folder;
import com.ifuema.filedownload.vo.DownloadLog;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Download {
    @Resource
    private Folder folder;
    @Resource
    private DateTimeFormatter dateTimeFormatter;
    @Resource
    private DownloadLog downloadLog;

    @GetMapping
    public String view(String path, HttpServletRequest request) {
        List<String> stringList = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer("<html><body>");
        stringList.add("----------------------------------------------------------------------------------------------------");
        stringList.add("访问请求");
        stringList.add("请求时间：" + LocalDateTime.now().format(dateTimeFormatter));
        stringList.add("请求路径（修剪前）：" + path);
        File file;
        if (path == null) {
            file = new File(folder.getUrl());
        } else {
            path = path.replaceAll("\\.", "");
            file = new File(folder.getUrl() + "/" + path);
            stringList.add("请求路径（修剪后）：" + path);
        }
        stringList.add("客户端ip：" + request.getRemoteAddr());
        stringList.add("客户端主机名：" + request.getRemoteHost());
        if (!file.exists() || !file.canRead()) {
            stringBuffer.append("<h1>拒绝访问！</h1>");
            stringList.add("回复：拒绝访问！");
        } else {
            stringBuffer.append("<h3>" + (path == null ? "" : path) + "</h3><table border='1'><tr><th>类型</th><th>文件名</th><th>文件大小</th></tr>");
            File[] files = file.listFiles();
            for (File cfile : files) {
                if (cfile.isDirectory()) {
                    stringBuffer.append("<tr><td>文件夹</td><td><a href='/?path=" + (path != null ? path + "/" : "") + cfile.getName() + "'>" + cfile.getName() + "</a></td><td></td></tr>");
                } else if (cfile.isFile()) {
                    String l;
                    if (cfile.length() > 1000000000) {
                        l = String.format("%.2f", cfile.length() / 1024D / 1024D / 1024D) + "GB";
                    } else if (cfile.length() > 1000000) {
                        l = String.format("%.2f", cfile.length() / 1024D / 1024D) + "MB";
                    } else {
                        l = String.format("%.2f", cfile.length() / 1024D) + "KB";
                    }
                    stringBuffer.append("<tr><td>文件</td><td><a href='/download?path=/" + (path != null ? path + "/" : "") + cfile.getName() + "'>" + cfile.getName() + "</a></td><td>" + l + "</td></tr>");
                }
            }
            stringBuffer.append("</table>");
            stringList.add("回复：success");
        }
        stringBuffer.append("</body></html>");
        stringList.add("----------------------------------------------------------------------------------------------------");
        downloadLog.printLog(stringList);
        return new String(stringBuffer);
    }

    @GetMapping("/download")
    public String fileDownload(String path, HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<String> stringList = new ArrayList<>();
        stringList.add("####################################################################################################");
        stringList.add("下载请求");
        stringList.add("请求时间：" + LocalDateTime.now().format(dateTimeFormatter));
        stringList.add("请求文件（修剪前）：" + path);
        if (path == null) {
            stringList.add("客户端ip：" + request.getRemoteAddr());
            stringList.add("客户端主机名：" + request.getRemoteHost());
            stringList.add("回复：文件不存在！");
            stringList.add("####################################################################################################");
            downloadLog.printLog(stringList);
            return "<h1>文件不存在！</h1>";
        }
        String fileName = path.substring(path.lastIndexOf("/"));
        path = path.substring(0, path.lastIndexOf("/")).replaceAll("\\.", "");
        File file = new File(folder.getUrl() + "/" + path + fileName);
        stringList.add("请求文件（修剪后）：" + path + fileName);
        stringList.add("客户端ip：" + request.getRemoteAddr());
        stringList.add("客户端主机名：" + request.getRemoteHost());
        if (!file.exists()) {
            stringList.add("回复：文件不存在！");
            stringList.add("####################################################################################################");
            downloadLog.printLog(stringList);
            return "<h1>文件不存在！</h1>";
        } else if (!file.isFile()) {
            stringList.add("回复：目标不是文件！");
            stringList.add("####################################################################################################");
            downloadLog.printLog(stringList);
            return "<h1>目标不是文件！</h1>";
        } else if (!file.canRead()) {
            stringList.add("回复：拒绝访问！");
            stringList.add("####################################################################################################");
            downloadLog.printLog(stringList);
            return "<h1>拒绝访问！</h1>";
        } else {
            InputStream inputStream = new FileInputStream(file);
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
            ServletOutputStream outputStream = response.getOutputStream();
//            byte[] b = new byte[1024];
//            int len;
//            while ((len = inputStream.read(b)) > 0) {
//                outputStream.write(b, 0, len);
//            }
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
            stringList.add("回复：success");
            stringList.add("####################################################################################################");
            downloadLog.printLog(stringList);
            return "<h1>success</h1>";
        }
    }
}
