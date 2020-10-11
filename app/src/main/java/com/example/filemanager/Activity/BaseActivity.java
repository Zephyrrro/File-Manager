package com.example.filemanager.Activity;

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
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.filemanager.Adapter.FileViewAdapter;
import com.example.filemanager.FileView;
import com.example.filemanager.R;
import com.example.filemanager.Utils.FileUtils;
import com.example.filemanager.Utils.GetFilesUtils;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {
  private FloatingActionMenu fab;
  private LinearLayout buttomActionBar; //  底部操作栏
  private LinearLayout greyCover; //  灰色蒙层
  private ImageButton cutButton;
  private ImageButton copyButton;
  private ImageButton deleteButton;

  private String[] permissions = new String[]{
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE
  };
  private List<String> mPermissionList = new ArrayList<>();

  public FileViewAdapter adapter;
  public List<FileView> fileList;

  private static final String TAG = "BaseActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutResourceId());
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    fab = findViewById(R.id.fab);
    fab.setOnMenuButtonClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (fab.isOpened()) {
          greyCover.setVisibility(View.GONE);
        } else {
          greyCover.setVisibility(View.VISIBLE);
        }
        fab.toggle(true);
      }
    });

    buttomActionBar = findViewById(R.id.actionbar_bottom);
    cutButton = findViewById(R.id.button_cut);
    copyButton = findViewById(R.id.button_copy);
    deleteButton = findViewById(R.id.button_delete);
    greyCover = findViewById(R.id.grey_cover);

    greyCover.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        fab.close(true);
        greyCover.setVisibility(View.GONE);
      }
    });

    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        deleteFile();
      }
    });

    checkPermission();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    switch (id) {
      case android.R.id.home:
        if (adapter.leaveSelectMode()) {
          finish();
        }
        return true;
      case R.id.cut:
        findViewById(R.id.actionbar_bottom).setVisibility(View.GONE);
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
    if (adapter.leaveSelectMode()) {
      super.onBackPressed();
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

  protected abstract void init();

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

  public void setButtomActionBarVisibility(int visibility) {
    buttomActionBar.setVisibility(visibility);
  }

  private void setFileViewSort(String sort) {
    Collections.sort(fileList, GetFilesUtils.getInstance().fileOrder(sort));
    adapter.notifyDataSetChanged();
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
                    FileUtils.Instance.delete(fileView.getFilePath());
                    fileList.remove(fileView);
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
