package com.example.filemanager;

import java.io.File;

public class FileView {
  private String fileName;
  private Boolean isFolder;
  private String fileType;
  private String filePath;
  private long fileSize;

  private final String FILE_TYPE_FOLDER = "folder";

  public FileView(File file) {
    this.fileName = file.getName();
    if (file.isDirectory()) {
      this.isFolder = true;
      this.fileType = FILE_TYPE_FOLDER;
    } else {
      this.isFolder = false;
      this.fileType = getFileType(file.getName());
    }
    this.filePath = file.getAbsolutePath();
  }

  private String getFileType(String fileName) {
    if (!fileName.equals("")) {
      int dotIndex = fileName.lastIndexOf(".");
      if (dotIndex != -1) {
        return fileName.substring(dotIndex + 1);
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

  public String getFilePath() {
    return filePath;
  }
}
