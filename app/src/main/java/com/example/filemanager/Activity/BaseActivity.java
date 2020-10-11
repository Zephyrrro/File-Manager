package com.example.filemanager.Activity;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.filemanager.Adapter.FileViewAdapter;
import com.example.filemanager.FileView;
import com.example.filemanager.R;
import com.example.filemanager.Utils.FileManagerUtils;
import com.example.filemanager.Utils.GetFilesUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {
  public FloatingActionButton fab;
  private String[] permissions = new String[]{
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE
  };
  private List<String> mPermissionList = new ArrayList<>();
  protected String path;
  public FileViewAdapter adapter;

  private static final String TAG = "BaseActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutResourceId());
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    fab = findViewById(R.id.fab);
    fab.setOnClickListener(view -> deleteFile());

    setFabVisibility(View.GONE);
    checkPermission();
    setBottomSheet();
  }

  private void setBottomSheet(){
    findViewById(R.id.bottom_delete).setOnClickListener(view -> deleteFile());
    findViewById(R.id.bottom_cut).setOnClickListener(view -> cutOrCopyFile(true));
    findViewById(R.id.bottom_copy).setOnClickListener(view -> cutOrCopyFile(false));
    findViewById(R.id.bottom_paste).setOnClickListener(view -> pasteFile());
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case R.id.delete:
        deleteFile();
        return true;
      case android.R.id.home:
        if (!adapter.leaveSelectMode()) {
          finish();
        }
        return true;
      case R.id.sort_by_default:
        if (!item.isChecked()) {
          setFileViewSort(GetFilesUtils.SORT_BY_DEFAULT);
          item.setChecked(true);
        }
        return true;
      case R.id.sort_by_size:
        if (!item.isChecked()) {
          setFileViewSort(GetFilesUtils.SORT_BY_SIZE);
          item.setChecked(true);
        }
        return true;
      case R.id.sort_by_time:
        if (!item.isChecked()) {
          setFileViewSort(GetFilesUtils.SORT_BY_TIME);
          item.setChecked(true);
        }
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    if (!adapter.leaveSelectMode()) {
      super.onBackPressed();
    } else {
      setFabVisibility(View.GONE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case 1:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
          init();
        } else {
          Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
          finish();
        }
        break;
      default:
        break;
    }
  }

  protected void init(){
    Intent intent = getIntent();
    path = intent.getStringExtra("path");

    RecyclerView recyclerView = findViewById(R.id.file_container);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
  }

  protected abstract int getLayoutResourceId();

  private void checkPermission() {
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission) !=
              PackageManager.PERMISSION_GRANTED) {
        mPermissionList.add(permission);
      }
    }

    if (!mPermissionList.isEmpty()) {
      String[] permissions1 = mPermissionList.toArray(new String[0]);
      ActivityCompat.requestPermissions(this, permissions1, 1);
    } else {
      init();
    }
  }

  public void setFabVisibility(int visibility) {
    fab.setVisibility(visibility);
  }

  public void setFileViewSort(String sort) {
    Collections.sort(adapter.getFileList(), GetFilesUtils.getInstance().fileOrder(sort));
    adapter.notifyDataSetChanged();
  }

  private void cutOrCopyFile(boolean isCut){
    List<Path> fileList = new ArrayList<>();
    for(FileView fileView:adapter.getSelected()){
      fileList.add(fileView.getFilePath());
    }
    if (isCut) {
      FileManagerUtils.Instance.cut(fileList);
    } else {
      FileManagerUtils.Instance.copy(fileList);
    }
  }

  private void pasteFile(){
    try {
      FileManagerUtils.Instance.paste(Paths.get(path));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void deleteFile() {
    final List<FileView> selected = adapter.getSelected();
    new AlertDialog.Builder(this)
            .setTitle("删除文件")
            .setMessage("你确定要删除" + selected.size() + "个文件/文件夹？此操作无法撤销。")
            .setNegativeButton("确定", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                try {
                  for (FileView fileView : selected) {
                    FileManagerUtils.Instance.delete(fileView.getFilePath());
                    adapter.getFileList().remove(fileView);
                  }
                  adapter.notifyDataSetChanged();
                  Snackbar.make(findViewById(R.id.main_layout), "Deleted!", 3000).show();
                } catch (IOException e) {
                  Log.d(TAG, "onClick: " + e.getMessage());
                  Snackbar.make(findViewById(R.id.main_layout), "删除文件失败，原因：" + e.getMessage(), 3000).show();
                }
              }
            })
            .setPositiveButton("取消", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {

              }
            }).show();
  }
}