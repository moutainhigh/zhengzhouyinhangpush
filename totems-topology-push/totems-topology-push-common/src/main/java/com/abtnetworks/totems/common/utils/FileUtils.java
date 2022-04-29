package com.abtnetworks.totems.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


/**
 * Author: hanyu
 * Create: 2017/10/17
 * mailto:hanyu100@foxmail.com
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);



    /**
     * 创建目录
     * @param destDirName
     * @return
     */
    public static boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {
            return false;
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        //创建目录
        if (dir.mkdirs()) {
            System.out.println("创建目录" + destDirName + "成功！");
            return true;
        } else {
            System.out.println("创建目录" + destDirName + "失败！");
            return false;
        }
    }


    /**
     * 文件下载
     *
     * @param file
     * @param response
     * @return
     */
    public static boolean downloadOverView(File file, HttpServletResponse response) {
        try {
            // 以流的形式下载文件。
            InputStream fis = new BufferedInputStream(new FileInputStream(file.getPath()));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            response.reset();
            fis.close();
            String strName = file.getName();
            try {
                strName = new String(strName.getBytes("UTF-8"), "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(strName, "UTF-8"));
            toClient.write(buffer);
            toClient.flush();
            fis.close();
            toClient.close();
            return true;
        } catch (IOException ex) {
            logger.error("downloadZip异常", ex);
            return false;
        } finally {
            try {
            } catch (Exception e) {
                logger.error("downloadZip异常", e);
            }
        }
    }





    // 判断文件夹是否有文件并返回文件名
    public static String isDirExistFile(String dirPath) {
        if(StringUtils.isBlank(dirPath)) {
            return "aa.temp";
        }
        File file = new File(dirPath);
        File[] listFiles = file.listFiles();
        String fileName = "";
        if (listFiles != null && listFiles.length > 0) {
            for (int i = 0; i < listFiles.length; i++) {
                if (listFiles[i].isFile()) {
                    fileName = listFiles[0].getName();
                    break;
                }
            }
            return fileName;
        } else {
            return "aa.temp";
        }
    }



    /**
     * 删除目录下指定的文件
     */
    public static void deleteFileByPath(String filePath) {
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
    }



    // 判断文件是否存在
    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }



}
