package com.example.filemanager;

import android.os.Build;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.Locale;
import java.nio.file.Path;

public class FileView {
  private String fileName;
  private Boolean isFolder;
  private String fileType;
  private File file;
  private long fileSize;
  private long fileTime;

  private final String FILE_TYPE_FOLDER = "folder";

  @RequiresApi(api = Build.VERSION_CODES.O)
  public FileView(File file) {
    this.fileName = file.getName();
    if (file.isDirectory()) {
      this.isFolder = true;
      this.fileType = FILE_TYPE_FOLDER;
    } else {
      this.isFolder = false;
      this.fileType = getFileType(file.getName());
    }
    this.file = file;
    this.fileSize = file.length();
    this.fileTime = file.lastModified();
  }

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
