package com.example.filemanager.Activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.example.filemanager.Adapter.FileViewAdapter;
import com.example.filemanager.R;
import com.example.filemanager.Utils.FileManagerUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

public class SearchActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setTitle(path);

    setSupportActionBar(toolbar);
    //添加默认的返回图标
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    //设置返回键可用
    getSupportActionBar().setHomeButtonEnabled(true);
    EditText searchInput = findViewById(R.id.search_input);
    searchInput.setVisibility(View.VISIBLE);

    searchInput.setOnEditorActionListener((textView, i, keyEvent) -> {
      if(i == EditorInfo.IME_ACTION_SEARCH){
        search();
      }
      return true;
    });
  }

  @Override
  public void init() {
    super.init();

    RecyclerView recyclerView = findViewById(R.id.file_container);
    adapter = new FileViewAdapter(new ArrayList<>());
    recyclerView.setAdapter(adapter);
  }

  @Override
  protected int getLayoutResourceId() {
    return R.layout.activity_main;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if(item.getItemId() == R.id.search){
      search();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void search(){
    EditText searchInput = findViewById(R.id.search_input);
    if(StringUtils.isBlank(searchInput.getText())){
      return;
    }
    FileManagerUtils.Instance.searchFiles(new File(path),searchInput.getText().toString(), file -> {
      runOnUiThread(() -> adapter.addFile(file));
    });
  }
}