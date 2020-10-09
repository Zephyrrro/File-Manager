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

public class ChildActivity extends AppCompatActivity {
  private List<FileView> fileList;
  private FileViewAdapter adapter;

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
        finish();
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

  private void init(String path) {
    fileList = GetFilesUtils.getInstance().getChildNode(path);

    RecyclerView recyclerView = findViewById(R.id.file_container_child);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    adapter = new FileViewAdapter(fileList);
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
  }
}