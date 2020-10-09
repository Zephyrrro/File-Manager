package com.example.filemanager.Utils;

import android.os.Environment;
import android.util.Log;

import com.example.filemanager.FileView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetFilesUtils {

  private static final String TAG = "GetFilesUtils";
  private static GetFilesUtils instance;

  private GetFilesUtils() {
  }

  public static synchronized GetFilesUtils getInstance() {
    if (instance == null) {
      instance = new GetFilesUtils();
    }
    return instance;
  }

  public List<FileView> getChildNode(File file) {
    List<FileView> list = new ArrayList<>();
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      Log.d(TAG, "getChildNode: " + Arrays.toString(files));
      if (files != null) {
        for (File childNode : files) {
          FileView fileView = new FileView(childNode);
          list.add(fileView);
        }
      }
    }
    return list;
  }

  public List<FileView> getChildNode(String path) {
    File file = new File(path);
    return getChildNode(file);
  }

  private String getSDPath() {
    String sdCard = Environment.getExternalStorageState();
    if (sdCard.equals(Environment.MEDIA_MOUNTED)) {
      return Environment.getExternalStorageDirectory().getAbsolutePath();
    } else {
      return null;
    }
  }

  public String getBasePath() {
    String basePath = getSDPath();
    if (basePath == null) {
      return Environment.getDataDirectory().getAbsolutePath();
    } else {
      return basePath;
    }
  }
}
