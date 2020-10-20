package com.example.filemanager;

import java.io.File;
import java.util.Locale;

/**
 * @author Hibiscus_L
 * */
public class FileView {
  private final String fileName;  //  文件名称
  private final Boolean isFolder; //  是否为文件夹
  private final String fileType;  //  文件类型
  private final File file;        //  文件实例
  private final long fileSize;    //  文件大小
  private final long fileTime;    //  文件最后修改时间

  public FileView(File file) {
    this.fileName = file.getName();
    if (file.isDirectory()) {
      this.isFolder = true;
      this.fileType = "folder";
    } else {
      this.isFolder = false;
      this.fileType = getFileType(file.getName());
    }
    this.file = file;
    this.fileSize = file.length();
    this.fileTime = file.lastModified();
  }

  //  获取文件后缀名
  private String getFileType(String fileName) {
    if (!fileName.equals("")) {
      int dotIndex = fileName.lastIndexOf(".");
      if (dotIndex != -1) {
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.US);
      }
    }
    return "";
  }

  public String getFileName() {
    return fileName;
  }

  public Boolean isFolder() {
    return isFolder;
  }

  public String getFileType() {
    return fileType;
  }

  public File getFile() {
    return file;
  }

  public long getFileSize() {
    return fileSize;
  }

  public long getFileTime() {
    return fileTime;
  }
}
