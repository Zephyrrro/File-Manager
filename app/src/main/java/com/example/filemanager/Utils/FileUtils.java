package com.example.filemanager.Utils;

import android.os.Build;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * @author gaofan
 */
public class FileUtils {
    public static final FileUtils Instance = new FileUtils();

    private FileUtils(){

    }

    private List<Path> paths;
    private boolean doCut;

    public boolean canPaste(){
        return paths != null && paths.size() > 0;
    }

    public void cut(List<Path> paths){
        this.paths = paths;
        doCut = true;
    }

    public void copy(List<Path> paths){
        this.paths = paths;
        doCut = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void paste(Path target) throws IOException {
        for(Path path : paths){
            if(doCut){
                Files.move(path, target, StandardCopyOption.COPY_ATTRIBUTES);
            }else{
                Files.copy(path, target, StandardCopyOption.COPY_ATTRIBUTES);
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void  delete(Path path) throws IOException {
        Files.delete(path);
    }
}
