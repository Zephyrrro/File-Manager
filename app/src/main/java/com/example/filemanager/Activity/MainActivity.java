package com.example.filemanager.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

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

  @Override
  public void init() {
    super.init();
    if (!path.equals(GetFilesUtils.getInstance().getBasePath())) {
      //添加默认的返回图标
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      //设置返回键可用
      getSupportActionBar().setHomeButtonEnabled(true);
    }
    setTitle(new File(path).getName());
    getSupportActionBar().setSubtitle(path);
    RecyclerView recyclerView = findViewById(R.id.file_container);

    ItemTouchCallBack touchCallBack = new ItemTouchCallBack(adapter);
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchCallBack);
    itemTouchHelper.attachToRecyclerView(recyclerView);
    recyclerView.setOnTouchListener((v, event) -> {
      int type = event.getAction();
      switch (type) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
          break;
        case MotionEvent.ACTION_UP:
          Log.i(TAG, "recyclerView touch UP");
          if (adapter.getFromPosition() != -1) {
            adapter.notifyItemRemoved(adapter.getFromPosition());
            FileView source = fileList.get(adapter.getFromPosition());
            FileView target = fileList.get(adapter.getToPosition());
            if (target.isFolder()) {
              Log.d(TAG, "folder to folder");
              try {
                FileManagerUtils.Instance.moveToFolder(source.getFile(), target.getFile());
                fileList.remove(source);
                adapter.setItemCheckStates(adapter.getFromPosition(), false);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "移动成功~", Toast.LENGTH_SHORT).show();
              } catch (IOException e) {
                e.printStackTrace();
              }
            } else if (!source.isFolder() && !target.isFolder()) {
              String newFolderName = source.getFileName() + "和" + target.getFileName();
              File newFolder = new File(FilenameUtils.concat(source.getFile().getParent(), newFolderName));
              try {
                FileManagerUtils.Instance.mergeIntoFolder(source.getFile(), target.getFile(), newFolder);
                fileList.remove(source);
                fileList.remove(target);
                fileList.add(new FileView(newFolder));
                adapter.setItemCheckStates(adapter.getFromPosition(), false);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "移动成功~", Toast.LENGTH_SHORT).show();
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
    });
  }

  @Override
  protected int getLayoutResourceId() {
    return R.layout.activity_main;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.search) {
      Intent intent = new Intent(this, SearchActivity.class);
      intent.putExtra("path", path);
      startActivity(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}