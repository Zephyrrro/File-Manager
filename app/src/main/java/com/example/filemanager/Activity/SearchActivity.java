package com.example.filemanager.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.example.filemanager.Adapter.FileViewAdapter;
import com.example.filemanager.R;
import com.example.filemanager.Utils.FileManagerUtils;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends BaseActivity {

  private AsyncTask searchTask;
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

    FloatingActionMenu fab = findViewById(R.id.fab);
    fab.setVisibility(View.GONE);
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

  @Override
  public void onBackPressed() {
    searchTask.cancel(true);
    super.onBackPressed();
  }

  public void search(){
    // 关闭搜索软键盘
    InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    if(imm != null) {
      imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }
    // 检测搜索文本是否为空
    EditText searchInput = findViewById(R.id.search_input);
    if(StringUtils.isBlank(searchInput.getText())){
      return;
    }
    // 清除原有的搜索记录
    adapter.clearFiles();
    ProgressBar progressBar = findViewById(R.id.progress);
    searchTask = new SearchTask(this);

    try {
      searchTask.execute(path,searchInput.getText());
    } catch (Exception e) {
      Log.i("search","search error",e);
      Toast.makeText(this,"搜索失败:" + e.getMessage(),3000);
    }finally {
      progressBar.setVisibility(View.GONE);
    }


  }

  static class SearchTask extends AsyncTask {
    private ProgressBar progressBar;
    private BaseActivity activity;
    public SearchTask(BaseActivity activity){
      this.activity = activity;
      progressBar = activity.findViewById(R.id.progress);
    }
    @Override
    protected void onPreExecute() {
      progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
      FileManagerUtils.Instance.searchFiles(this, new File(objects[0].toString()),objects[1].toString(), file -> {
        activity.runOnUiThread(() -> activity.adapter.addFile(file));
      });
      return null;
    }
  };
}