package com.example.filemanager.Activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.filemanager.Adapter.FileViewAdapter;
import com.example.filemanager.FileView;
import com.example.filemanager.Utils.GetFilesUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;

import com.example.filemanager.R;

import java.util.List;

public class ChildActivity extends AppCompatActivity {
  private List<FileView> fileList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_child);

    Intent intent = getIntent();
    String path = intent.getStringExtra("path");
    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setTitle(path);

    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);//添加默认的返回图标
    getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

    init(path);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void init(String path) {
    fileList = GetFilesUtils.getInstance().getChildNode(path);

    RecyclerView recyclerView = findViewById(R.id.file_container_child);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    FileViewAdapter adapter = new FileViewAdapter(fileList);
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
  }
}