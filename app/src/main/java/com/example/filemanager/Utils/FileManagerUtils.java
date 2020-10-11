package com.example.filemanager.Utils;

import android.os.Build;
import android.os.FileUtils;
import android.util.Log;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    public List<Path> paste(final Path target) throws IOException {
        List<Path> result = new ArrayList<>();
        for(Path file : paths){
            Path targetFile = Paths.get(target.toString(), file.toFile().getName());
            Log.i(getClass().toString(),"file : " + targetFile);
            if(doCut){
                result.add(Files.move(file, targetFile, StandardCopyOption.COPY_ATTRIBUTES));
            }else{
                result.add(Files.copy(file, targetFile, StandardCopyOption.COPY_ATTRIBUTES));
            }
        }
        this.paths = null;
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void delete(Path path) throws IOException {
       // File
        Files.delete(path);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void searchFiles(Path currentPath, final String keyword, SearchFoundFile searchFoundFile){
        File[] files = currentPath.toFile().listFiles();
        if(files == null){
            return;
        }
        Arrays.stream(files).parallel().forEach(x -> {
            if(x.getName().contains(keyword)){
                searchFoundFile.onFoundFile(x);
            }
            if(x.isDirectory()){
                searchFiles(x.toPath(),keyword,searchFoundFile);
            }
        }
        );
    }
}
