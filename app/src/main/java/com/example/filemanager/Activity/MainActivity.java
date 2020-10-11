package com.example.filemanager.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.filemanager.Adapter.FileViewAdapter;
import com.example.filemanager.R;
import com.example.filemanager.Utils.GetFilesUtils;

import java.nio.file.Paths;
import java.util.Collections;

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