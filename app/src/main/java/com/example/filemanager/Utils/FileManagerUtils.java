package com.example.filemanager.Utils;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.filemanager.FileView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author gaofan
 */
public class FileManagerUtils {
  public static final FileManagerUtils Instance = new FileManagerUtils();

  private FileManagerUtils() {

  }

  public interface SearchFoundFile {
    void onFoundFile(File file);
  }

  private List<FileView> paths; //  剪切或复制的文件，即剪切板
  private boolean doCut;        //  剪切或复制的标记位

  public void cut(List<FileView> paths) {
    this.paths = paths;
    doCut = true;
  }

  public void copy(List<FileView> paths) {
    this.paths = paths;
    doCut = false;
  }

  public List<FileView> paste(final File target) throws IOException {
    List<FileView> result = new ArrayList<>();
    for (FileView file : paths) {
      String filePath = file.getFile().getPath();
      String targetPath = target.getPath();
      if (doCut) {
        //  从当前目录下剪切，再粘贴到当前目录，不执行 moveToDirectory
        if (!filePath.substring(0, filePath.indexOf(file.getFileName()) - 1).equals(targetPath)) {
          FileUtils.moveToDirectory(file.getFile(), target, true);
        }
      } else {
        FileUtils.copyToDirectory(file.getFile(), target);
      }
      //  更新一下 fileView，否则会是粘贴前的信息
      FileView newFileView = new FileView(new File(targetPath + "/" + file.getFileName()));
      result.add(newFileView);
    }
    this.paths = null;  //  清空剪切板
    return result;      //  返回成功粘贴的文件
  }

  //  删除文件
  public boolean delete(File file) {
    return FileUtils.deleteQuietly(file);
  }

  //  文件递归搜索
  public void searchFiles(AsyncTask asyncTask, File currentPath, final String keyword, SearchFoundFile searchFoundFile) {
    if(asyncTask.isCancelled()){
      //搜索 被人为终止了
      return;
    }
    //  从当前路径下开始搜索
    File[] files = currentPath.listFiles();
    if (ArrayUtils.isEmpty(files)) {
      return;
    }
    for (File file : files) {
      if(asyncTask.isCancelled()){
        //搜索 被人为终止了
        return;
      }
      if (file.getName().contains(keyword)) {
        searchFoundFile.onFoundFile(file);
      }
      if (file.isDirectory()) {
        searchFiles(asyncTask, file, keyword, searchFoundFile);
      }
    }
  }

  //  新建文件
  public boolean createFile(String filePath) throws IOException {
    File file = new File(filePath);
    return file.createNewFile();
  }

  //  新建文件夹
  public boolean createDirectory(String dirPath) {
    File dir = new File(dirPath);
    return dir.mkdir();
  }

  //  移动文件或文件夹至当前目录下
  public void moveToFolder(File file, File dir) throws IOException {
    if (file.isDirectory()) {
      FileUtils.moveDirectoryToDirectory(file, dir, false);
    } else {
      FileUtils.moveFileToDirectory(file, dir, false);
    }
  }

  //  两文件或文件夹合并为一个文件夹
  public void mergeIntoFolder(File file1, File file2, File newFolder) throws IOException {
    FileUtils.moveFileToDirectory(file1, newFolder, true);
    FileUtils.moveFileToDirectory(file2, newFolder, false);
  }

  public List<FileView> getClipBoard() {
    return this.paths;
  }
}
