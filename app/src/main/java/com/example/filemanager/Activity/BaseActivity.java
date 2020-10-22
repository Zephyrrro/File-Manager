package com.example.filemanager.Activity;

import android.content.Context;
import android.content.Intent;

import android.view.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filemanager.Adapter.FileViewAdapter;
import com.example.filemanager.FileView;
import com.example.filemanager.R;
import com.example.filemanager.Utils.DensityUtil;
import com.example.filemanager.Utils.FileManagerUtils;
import com.example.filemanager.Utils.GetFilesUtils;
import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class BaseActivity extends AppCompatActivity {
  private FloatingActionMenu fab;                     //  右下悬浮按钮
  private LinearLayout greyCover;                     //  灰色蒙层
  public RecyclerView recyclerView;
  public LinearLayout bottomSheetLayout;
  public BottomSheetBehavior bottomSheetBehavior;     //  底部操作栏

  private final String[] permissions = new String[]{  //  应用所需权限
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE
  };
  private final List<String> mPermissionList = new ArrayList<>(); //  已同意的权限
  protected String path;            //  当前文件树结点的路径
  public FileViewAdapter adapter;
  public List<FileView> fileList;   //  文件列表


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //  设置布局
    setContentView(getLayoutResourceId());
    //  开启Toolbar功能
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    //  文件列表视图初始化
    recyclerView = findViewById(R.id.file_container);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    //  底部操作栏初始化
    bottomSheetLayout = findViewById(R.id.bottomSheetLayout);
    bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);

    //  检查权限，若权限全同意则执行 init 方法
    checkPermission();
    //  设置底部操作栏各个按钮的点击回调
    setBottomSheet();
    //  设置右下悬浮按钮的点击回调
    setFloatingMenu();
  }

  private void setBottomSheet() {
    findViewById(R.id.bottom_delete).setOnClickListener(view -> deleteFile());
    findViewById(R.id.bottom_cut).setOnClickListener(view -> cutOrCopyFile(true));
    findViewById(R.id.bottom_copy).setOnClickListener(view -> cutOrCopyFile(false));
    findViewById(R.id.bottom_paste).setOnClickListener(view -> pasteFile());
  }

  private void setFloatingMenu() {
    fab = findViewById(R.id.fab);
    greyCover = findViewById(R.id.grey_cover);

    fab.setOnMenuButtonClickListener(view -> {
      setSelectModeShow(false);
      if (fab.isOpened()) {
        greyCover.setVisibility(View.GONE);
      } else {
        greyCover.setVisibility(View.VISIBLE);
      }
      fab.toggle(true);
    });

    FloatingActionButton createDirBtn = findViewById(R.id.create_dir);
    FloatingActionButton createFileBtn = findViewById(R.id.create_file);
    createDirBtn.setOnClickListener(view -> createFileOrDirBtnCallback(true));
    createFileBtn.setOnClickListener(view -> createFileOrDirBtnCallback(false));

    //  点击灰色蒙层时关闭 FloatingActionMenu
    greyCover.setOnClickListener(view -> closeFloatingMenu());
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
      case R.id.show_ext:
        item.setChecked(!item.isChecked());
        adapter.setShowExt(item.isChecked());
        return true;
      case android.R.id.home:
        if (!adapter.leaveSelectMode()) {
          if (fab.isOpened()) {
            closeFloatingMenu();
          } else {
            finishAfterTransition();
          }
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
      case R.id.paste:
        pasteFile();
        return true;
      case R.id.select_all:
        adapter.selectAll();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    //  根据当前状态确定 back 键的功能
    if (!adapter.leaveSelectMode()) {
      if (fab.isOpened()) {
        closeFloatingMenu();
      } else {
        super.onBackPressed();
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == 1) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        init();
      } else {
        Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
        finishAfterTransition();
      }
    }
  }

  protected void init() {
    Intent intent = getIntent();
    path = intent.getStringExtra("path");
    //  path 为 null，代表当前节点为根节点，path 更改为根路径
    if (path == null) {
      path = GetFilesUtils.getInstance().getBasePath();
    }

    //  BFS遍历一层节点并渲染
    this.fileList = GetFilesUtils.getInstance().getChildNode(path);
    adapter = new FileViewAdapter(this.fileList);
    recyclerView.setAdapter(adapter);
  }

  protected abstract int getLayoutResourceId();

  private void checkPermission() {
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission) !=
              PackageManager.PERMISSION_GRANTED) {
        mPermissionList.add(permission);
      }
    }

    //  未同意权限则请求权限，否则直接执行 init 方法
    if (!mPermissionList.isEmpty()) {
      String[] permissions1 = mPermissionList.toArray(new String[0]);
      ActivityCompat.requestPermissions(this, permissions1, 1);
    } else {
      init();
    }
  }

  //  设置多选模式下的UI
  public void setSelectModeShow(boolean isSelectMode) {
    if (isSelectMode) {
      fab.setVisibility(View.GONE);
      recyclerView.setPadding(0, 0, 0, DensityUtil.dip2px(this, 48));
      bottomSheetLayout.setVisibility(View.VISIBLE);
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    } else {
      fab.setVisibility(View.VISIBLE);
      recyclerView.setPadding(0, 0, 0, 0);
      bottomSheetLayout.setVisibility(View.GONE);
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
  }

  //  设置文件排序
  private void setFileViewSort(String sort) {
    Collections.sort(this.fileList, GetFilesUtils.getInstance().fileOrder(sort));
    adapter.notifyDataSetChanged();
  }

  private void closeFloatingMenu() {
    fab.close(true);
    greyCover.setVisibility(View.GONE);
  }

  /**
   * 剪切或复制文件，剪切文件则将文件从 UI 中删除
   *
   * @param isCut 是否为剪切操作
   */
  private void cutOrCopyFile(boolean isCut) {
    Set<FileView> selected = adapter.getSelectSet();
    List<FileView> fileList = new ArrayList<>(selected);
    if (isCut) {
      FileManagerUtils.Instance.cut(fileList);
      Toast.makeText(this, "已剪切~请移动到目标目录下粘贴", Toast.LENGTH_SHORT).show();
      this.fileList.removeAll(fileList);
    } else {
      FileManagerUtils.Instance.copy(fileList);
      Toast.makeText(this, "已复制~请移动到目标目录下粘贴", Toast.LENGTH_SHORT).show();
    }
    adapter.notifyOperationFinish();
  }

  private void pasteFile() {
    if (FileManagerUtils.Instance.isClipBoardEmpty()) {
      Toast.makeText(this, "剪切板为空~", Toast.LENGTH_SHORT).show();
      return;
    }
    try {
      List<FileView> result = FileManagerUtils.Instance.paste(new File(path));
      fileList.addAll(result);
      Toast.makeText(this, "粘贴成功~", Toast.LENGTH_SHORT).show();
      adapter.notifyOperationFinish();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //  删除文件前弹窗确认操作，确认后执行删除操作
  private void deleteFile() {
    final Set<FileView> selected = adapter.getSelectSet();
    final Context context = this;
    new AlertDialog.Builder(this)
            .setTitle("删除文件")
            .setMessage("你确定要删除" + selected.size() + "个文件/文件夹？此操作无法撤销。")
            .setNegativeButton("确定", (dialogInterface, i) -> {
              //  遍历勾选文件列表，逐个删除文件
              for (FileView fileView : selected) {
                if (!FileManagerUtils.Instance.delete(fileView.getFile())) {
                  Toast.makeText(context, "删除文件失败", Toast.LENGTH_SHORT).show();
                  return;
                }
                fileList.remove(fileView);
              }
              adapter.notifyOperationFinish();
              Toast.makeText(context, "删除文件成功", Toast.LENGTH_SHORT).show();
            })
            .setPositiveButton("取消", (dialogInterface, i) -> {

            }).show();
  }

  //  创建文件或文件夹
  private void createFileOrDir(String path, boolean isDirectory) {
    closeFloatingMenu();
    if (isDirectory) {
      boolean result = FileManagerUtils.Instance.createDirectory(path);
      if (result) {
        Toast.makeText(this, "创建成功", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(this, "创建失败，可能存在同名文件夹", Toast.LENGTH_SHORT).show();
        return;
      }
    } else {
      try {
        boolean result = FileManagerUtils.Instance.createFile(path);
        if (result) {
          Toast.makeText(this, "创建成功", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(this, "创建失败可能存在同名文件", Toast.LENGTH_SHORT).show();
          return;
        }
      } catch (IOException e) {
        Toast.makeText(BaseActivity.this, "创建失败, 原因是：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        return;
      }
    }
    //  创建成功，更新 UI，并滑动到新建的文件/文件夹位置
    FileView fileView = new FileView(new File(path));
    fileList.add(fileView);
    adapter.notifyOperationFinish();
    int position = fileList.indexOf(fileView);
    if (position != -1) {
      recyclerView.scrollToPosition(position);
      LinearLayoutManager mLayoutManager =
              (LinearLayoutManager) recyclerView.getLayoutManager();
      mLayoutManager.scrollToPositionWithOffset(position, 0);
    }
  }

  //  创建文件/文件夹按钮的回调，创建一个弹窗
  private void createFileOrDirBtnCallback(boolean isDirectory) {
    final View dialogView = LayoutInflater.from(BaseActivity.this)
            .inflate(R.layout.input_dialog, null);
    ((TextView) dialogView.findViewById(R.id.dialog_tip)).setText(isDirectory ? "请输入新建文件夹名称" : "请输入新的文件名");

    final EditText editText = dialogView.findViewById(R.id.dialog_input);
    editText.requestFocus();

    final AlertDialog dialog =
            new AlertDialog.Builder(BaseActivity.this).setTitle(isDirectory ? "创建一个文件夹" : "创建一个空白文件").setView(dialogView).setPositiveButton(
                    "确定", null).setNegativeButton("取消", (dialog1, which) -> {
            }).create();
    dialog.setOnShowListener(dialogInterface -> {
      Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
      positiveBtn.setOnClickListener(v -> {
        String name = editText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
          Toast.makeText(BaseActivity.this, "名称不能为空", Toast.LENGTH_SHORT).show();
        } else {
          String newPath = path + "/" + name;
          createFileOrDir(newPath, isDirectory);
          dialog.dismiss();
        }
      });
    });
    dialog.show();
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.orangeDark));
    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
  }
}

