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

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.filemanager.R;

import java.util.Collections;
import java.util.List;

public class ChildActivity extends BaseActivity {
  private String path;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setTitle(path);

    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);//添加默认的返回图标
    getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

  }

  @Override
  public void init() {
    Intent intent = getIntent();
    path = intent.getStringExtra("path");

    fileList = GetFilesUtils.getInstance().getChildNode(path);

    RecyclerView recyclerView = findViewById(R.id.file_container);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    adapter = new FileViewAdapter(fileList);
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
  }

  @Override
  protected int getLayoutResourceId() {
    return R.layout.activity_main;
  }
}