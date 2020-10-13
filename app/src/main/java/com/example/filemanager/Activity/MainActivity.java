package com.example.filemanager.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.filemanager.Adapter.FileViewAdapter;
import com.example.filemanager.FileView;
import com.example.filemanager.ItemTouchCallBack;
import com.example.filemanager.R;
import com.example.filemanager.Utils.FileManagerUtils;
import com.example.filemanager.Utils.GetFilesUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class MainActivity extends BaseActivity {

  private static final String TAG = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void init() {
    super.init();
    if(path == null){
      path =  GetFilesUtils.getInstance().getBasePath();
    }else{
      //添加默认的返回图标
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      //设置返回键可用
      getSupportActionBar().setHomeButtonEnabled(true);
    }
    setTitle(Paths.get(path).toFile().getName());
    getSupportActionBar().setSubtitle(path);
    RecyclerView recyclerView = findViewById(R.id.file_container);
    adapter = new FileViewAdapter(GetFilesUtils.getInstance().getChildNode(path));
    recyclerView.setAdapter(adapter);
    ItemTouchCallBack touchCallBack = new ItemTouchCallBack(adapter);
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchCallBack);
    itemTouchHelper.attachToRecyclerView(recyclerView);
    recyclerView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        int type = event.getAction();
        switch (type) {
          case MotionEvent.ACTION_DOWN:
          case MotionEvent.ACTION_MOVE:
            break;
          case MotionEvent.ACTION_UP:
            Log.i(TAG,"recyclerView touch UP");
            if(adapter.getFromPosition() != -1){
              adapter.notifyItemRemoved(adapter.getFromPosition());
              FileView source = adapter.getFileList().get(adapter.getFromPosition());
              FileView target = adapter.getFileList().get(adapter.getToPosition());
              if(target.isFolder()){
                try {
                  FileManagerUtils.Instance.moveToFolder(source.getFile(),target.getFile());
                  adapter.getFileList().remove(source);
                  adapter.notifyItemRemoved(adapter.getFromPosition());
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }else if (!source.isFolder() && !target.isFolder()){
                String newFolderName = source.getFileName() + "和" + target.getFileName();
                File newFolder = new File(FilenameUtils.concat(source.getFile().getParent(),newFolderName));
                try {
                  FileManagerUtils.Instance.mergeIntoFolder(source.getFile(),target.getFile(),newFolder);
                  adapter.getFileList().remove(source);
                  adapter.getFileList().remove(target);
                  adapter.getFileList().add(new FileView(newFolder));
                  adapter.notifyDataSetChanged();
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
              adapter.setFromPosition(-1);
              return true;
            }
            break;

        }
        return false;
      }
    });
  }

  @Override
  protected int getLayoutResourceId() {
    return R.layout.activity_main;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if(item.getItemId() == R.id.search){
      Intent intent = new Intent(this, SearchActivity.class);
      intent.putExtra("path", GetFilesUtils.getInstance().getBasePath());
      startActivity(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}