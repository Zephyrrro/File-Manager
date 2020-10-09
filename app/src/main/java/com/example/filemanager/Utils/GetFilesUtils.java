package com.example.filemanager.Utils;

import android.os.Environment;
import android.util.Log;

import com.example.filemanager.FileView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GetFilesUtils {

  private static final String TAG = "GetFilesUtils";
  public static final String SORT_BY_TIME = "SORT_BY_TIME";
  public static final String SORT_BY_SIZE = "SORT_BY_SIZE";
  public static final String SORT_BY_DEFAULT = "SORT_BY_DEFAULT";
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
          if (childNode.getName().startsWith(".")) {
            continue;
          }
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

  public Comparator<FileView> fileOrder(final String pattern) {
    return new Comparator<FileView>() {
      @Override
      public int compare(FileView o1, FileView o2) {
        int order = 0;
        long diff = 0;
        switch (pattern) {
          case SORT_BY_SIZE:
            diff = o1.getFileSize() - o2.getFileSize();
            if (diff != 0) {
              order = diff > 0 ? 1 : -1;
            }
            break;
          case SORT_BY_TIME:
            diff = o1.getFileTime() - o2.getFileTime();
            if (diff != 0) {
              order = diff > 0 ? 1 : -1;
            }
            break;
          case SORT_BY_DEFAULT:
          default:
            if (o1.isFolder() && !o2.isFolder()) {
              order = -1;
            } else if (!o1.isFolder() && o2.isFolder()) {
              order = 1;
            } else {
              order = o1.getFileName().compareTo(o2.getFileName());
            }
            break;
        }
        return order;
      }
    };
  }

  public Comparator<FileView> defaultOrder() {
    return fileOrder(SORT_BY_DEFAULT);
  }
}
