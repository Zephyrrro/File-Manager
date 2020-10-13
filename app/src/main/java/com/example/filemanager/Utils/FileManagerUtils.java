package com.example.filemanager.Utils;

import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
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

    private FileManagerUtils(){

    }

    public interface SearchFoundFile {
        void onFoundFile(File file);
    }

    private List<File> paths;
    private boolean doCut;

    public boolean canPaste(){
        return paths != null && paths.size() > 0;
    }

    public void cut(List<File> paths){
        this.paths = paths;
        doCut = true;
    }

    public void copy(List<File> paths){
        this.paths = paths;
        doCut = false;
    }

    public void paste(final File target) throws IOException {
        List<File> result = new ArrayList<>();
        for(File file : paths){
            //File targetFile = new File(FilenameUtils.concat(target.toString(), file.getName()));
            //Log.i(getClass().toString(),"file : " + targetFile);
            if(doCut){
                FileUtils.moveToDirectory(file,target,true);
            }else{
                FileUtils.copyToDirectory(file,target);
            }
        }
        this.paths = null;
    }

    public boolean delete(File file) {
        return FileUtils.deleteQuietly(file);
    }

    public void searchFiles(File currentPath, final String keyword, SearchFoundFile searchFoundFile){
        File[] files = currentPath.listFiles();
        if(ArrayUtils.isEmpty(files)){
            return;
        }
        for(File file : files){
            if(file.getName().contains(keyword)){
                searchFoundFile.onFoundFile(file);
            }
            if(file.isDirectory()){
                searchFiles(file,keyword,searchFoundFile);
            }
        }
    }

    public boolean createFile(String filePath) throws IOException {
        File file = new File(filePath);
        return file.createNewFile();
    }

    public boolean createDirectory(String dirPath) {
        File dir = new File(dirPath);
        return dir.mkdir();
    }

    public void moveToFolder(File file, File dir) throws IOException {
        FileUtils.moveFileToDirectory(file,dir,false);
    }

    public void mergeIntoFolder(File file1, File file2, File newFolder) throws IOException {
        FileUtils.moveFileToDirectory(file1,newFolder,true);
        FileUtils.moveFileToDirectory(file2,newFolder,false);
    }


}
