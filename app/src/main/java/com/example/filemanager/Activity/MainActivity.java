package com.example.filemanager.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.filemanager.Adapter.FileViewAdapter;
import com.example.filemanager.FileView;
import com.example.filemanager.R;
import com.example.filemanager.Utils.FileUtils;
import com.example.filemanager.Utils.GetFilesUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

  private List<FileView> fileList;
  private String[] permissions = new String[]{
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE
  };
  private List<String> mPermissionList = new ArrayList<>();
  private static final String TAG = "MainActivity";

  private FileViewAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    checkPermission();
    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        deleteFile();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public void onBackPressed() {
    if(!adapter.leaveSelectMode()){
      super.onBackPressed();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    switch (id) {
      case R.id.delete:
        deleteFile();
        return true;
      case R.id.sort_by_default:
        if (!item.isChecked()) {
          Collections.sort(fileList, GetFilesUtils.getInstance().fileOrder(GetFilesUtils.SORT_BY_DEFAULT));
          adapter.notifyDataSetChanged();
          item.setChecked(true);
        }
        return true;
      case R.id.sort_by_size:
        if (!item.isChecked()) {
          Collections.sort(fileList, GetFilesUtils.getInstance().fileOrder(GetFilesUtils.SORT_BY_SIZE));
          adapter.notifyDataSetChanged();
          item.setChecked(true);
        }
        return true;
      case R.id.sort_by_time:
        if (!item.isChecked()) {
          Collections.sort(fileList, GetFilesUtils.getInstance().fileOrder(GetFilesUtils.SORT_BY_TIME));
          adapter.notifyDataSetChanged();
          item.setChecked(true);
        }
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case 1:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
          initFileViews();
        } else {
          Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
          finish();
        }
        break;
      default:
        break;
    }
  }

  private void checkPermission() {
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(MainActivity.this, permission) !=
              PackageManager.PERMISSION_GRANTED) {
        mPermissionList.add(permission);
      }
    }

    if (!mPermissionList.isEmpty()) {
      String[] permissions1 = mPermissionList.toArray(new String[0]);
      ActivityCompat.requestPermissions(MainActivity.this, permissions1, 1);
    } else {
      initFileViews();
    }
  }

  private void initFileViews() {
    String basePath = GetFilesUtils.getInstance().getBasePath();
    fileList = GetFilesUtils.getInstance().getChildNode(basePath);

    RecyclerView recyclerView = findViewById(R.id.file_container);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    adapter = new FileViewAdapter(fileList);
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

  }

  private void deleteFile(){
      final List<FileView> selected = adapter.getSelected();
      new AlertDialog.Builder(MainActivity.this)
          .setTitle("删除文件")
          .setMessage("你确定要删除" + selected.size() + "个文件/文件夹？此操作无法撤销。")
          .setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              try{
              for(FileView fileView : selected){
                FileUtils.Instance.delete(fileView.getFilePath());

              }
              Snackbar.make(findViewById(R.id.main_layout),"Deleted!",3000).show();
            }catch (IOException e){
              Snackbar.make(findViewById(R.id.main_layout),"删除文件失败，原因：" + e.getMessage(),3000).show();
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