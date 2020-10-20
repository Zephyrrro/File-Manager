package com.example.filemanager.Adapter;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.Activity.BaseActivity;
import com.example.filemanager.Activity.MainActivity;
import com.example.filemanager.FileView;
import com.example.filemanager.ItemTouchCallBack;
import com.example.filemanager.R;
import com.example.filemanager.Utils.GetFilesUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileViewAdapter extends RecyclerView.Adapter<FileViewAdapter.ViewHolder> implements ItemTouchCallBack.OnItemTouchListener {
  private boolean showExt;      //  是否显示后缀名
  private boolean selectMode;   //  多选模式标志位
  private final List<FileView> fileList;
  private final Set<FileView> selectSet = new HashSet<>();                  //  选择文件列表，用于剪切、复制等操作
  private final SparseBooleanArray checkStates = new SparseBooleanArray();  //  选择状态列表，防止holder复用后状态混乱
  private final Map<String, Integer> fileTypeIconMap = new HashMap<>();
  private BaseActivity mContext = null;

  static class ViewHolder extends RecyclerView.ViewHolder {
    View fileView;
    ImageView fileImage;
    TextView fileName;
    TextView fileModifiedTime;
    TextView fileSize;
    CheckBox selected;


    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      fileView = itemView;
      selected = itemView.findViewById(R.id.selected);
      fileImage = itemView.findViewById(R.id.file_image);
      fileName = itemView.findViewById(R.id.file_name);
      fileModifiedTime = itemView.findViewById(R.id.file_modified_time);
      fileSize = itemView.findViewById(R.id.file_size);
    }
  }

  public FileViewAdapter(List<FileView> fileList) {
    Collections.sort(fileList, GetFilesUtils.getInstance().defaultOrder());
    this.fileList = fileList;
    //  各种文件类型的icon
    fileTypeIconMap.put("unknown", R.drawable.filetype_unknow);
    fileTypeIconMap.put("folder", R.drawable.filetype_folder);
    fileTypeIconMap.put("txt", R.drawable.filetype_txt);
    fileTypeIconMap.put("pdf", R.drawable.filetype_pdf);
    fileTypeIconMap.put("mp3", R.drawable.filetype_mp3);
    fileTypeIconMap.put("mp4", R.drawable.filetype_mp4);
    fileTypeIconMap.put("jpg", R.drawable.filetype_jpg);
    fileTypeIconMap.put("png", R.drawable.filetype_png);
  }

  public Set<FileView> getSelectSet() {
    return selectSet;
  }

  //  拖拽的起始位置以及结束位置
  int fromPosition = -1;
  int toPosition = -1;

  @Override
  public void onMove(int fromPosition, int toPosition) {
    this.fromPosition = fromPosition;
    this.toPosition = toPosition;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
    if (mContext == null) {
      mContext = (BaseActivity) parent.getContext();
    }
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_view_item, parent, false);
    return new ViewHolder(view);
  }


  //  viewHolder绑定
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    FileView fileView = fileList.get(position);

    //  FileView UI数据绑定
    Integer fileImage = fileTypeIconMap.get(fileView.getFileType());
    if (fileImage == null) {
      fileImage = fileTypeIconMap.get("unknown");
    }
    holder.fileImage.setImageResource(fileImage);
    String fileName = fileView.getFileName();
    if (!showExt && !fileView.isFolder() && fileName.indexOf(".") > 0) {
      fileName = fileName.substring(0, fileName.lastIndexOf("."));
    }
    holder.fileName.setText(fileName);
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA);
    holder.fileModifiedTime.setText(format.format(new Date(fileView.getFileTime())));
    holder.fileSize.setText(GetFilesUtils.getInstance().getFileSizeStr(fileView.getFileSize()));

    //  设置 TAG 防止 holder 复用时复选框错乱
    holder.selected.setTag(position);
    if (selectMode) {
      //  多选模式下显示复选框
      holder.selected.setVisibility(View.VISIBLE);
      holder.selected.setChecked(checkStates.get(position, false));
    } else {
      //  否则隐藏复选框，并清空勾选状态
      holder.selected.setVisibility(View.GONE);
      holder.selected.setChecked(false);
      checkStates.clear();
    }
    //  复选框勾选状态改变时的回调函数，勾选时将文件加入列表，取消勾选则移除
    holder.selected.setOnCheckedChangeListener((buttonView, isChecked) -> {
      int pos = (int) buttonView.getTag();
      FileView file = fileList.get(pos);
      if (isChecked) {
        checkStates.put(pos, true);
        selectSet.add(file);
      } else {
        checkStates.delete(pos);
        selectSet.remove(file);
      }
    });

    //  设置 TAG 防止 holder 复用时复选框错乱
    holder.fileView.setTag(position);
    holder.fileView.setOnClickListener(v -> {
      FileView file = fileList.get(position);

      //  多选模式下点击则勾选
      if (selectMode) {
        holder.selected.setChecked(!holder.selected.isChecked());
        return;
      }
      if (file.isFolder()) {
        //  若点击的项目为文件夹，则启动新活动，展示文件夹里的内容
        Intent intent = new Intent(v.getContext(), MainActivity.class);
        intent.putExtra("path", file.getFile().toString());
        v.getContext().startActivity(intent);
      } else {
        //  若为文件，则根据文件类型打开文件
        boolean isNeedMatch = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
        StrictMode.VmPolicy defaultVmPolicy = null;
        try {
          //  若 Android 版本 > 7.0，则暴力绕过文件权限检查
          if (isNeedMatch) {
            defaultVmPolicy = StrictMode.getVmPolicy();
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
          }
          //  根据文件 mimeType 判断文件类型，并自动打开对应程序
          MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
          String mime = mimeTypeMap.getMimeTypeFromExtension(file.getFileType());
          mime = TextUtils.isEmpty(mime) ? "" : mime;
          Intent intent = new Intent();
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.setAction(Intent.ACTION_VIEW);
          intent.setDataAndType(Uri.fromFile(new File(file.getFile().toString())), mime);
          v.getContext().startActivity(intent);
        } catch (Exception e) {
          //  手机内找不到可打开的程序，则 Toast 提示
          e.printStackTrace();
          Toast.makeText(v.getContext(), "无法打开", Toast.LENGTH_SHORT).show();
        } finally {
          //  重置权限检查
          if (isNeedMatch) {
            StrictMode.setVmPolicy(defaultVmPolicy);
          }
        }
      }
    });

    //  长按时的回调函数，若不处于多选模式下，则打开多选模式，并将长按的文件加入选择列表
    holder.fileView.setOnLongClickListener(view -> {
      if (!selectMode) {
        checkStates.put(position, true);
        selectSet.add(fileList.get(position));
        goSelectMode();
      }
      return true;
    });
  }


  @Override
  public int getItemCount() {
    return this.fileList.size();
  }

  //  打开多选模式
  public void goSelectMode() {
    selectMode = true;
    mContext.setSelectModeShow(true);
    notifyDataSetChanged();
  }

  //  关闭多选模式
  public boolean leaveSelectMode() {
    if (!selectMode) {
      return false;
    }
    selectMode = false;
    mContext.setSelectModeShow(false);
    selectSet.clear();
    checkStates.clear();
    notifyDataSetChanged();
    return true;
  }

  //  搜索文件时，将搜到的文件加入到列表
  public void addFile(File file) {
    if (fileList.add(new FileView(file))) {
      notifyItemInserted(fileList.size() - 1);
    }
  }

  //  仅用于清空搜索记录
  public void clearFiles(){
    fileList.clear();
    notifyDataSetChanged();
  }

  //  是否显示文件后缀名
  public void setShowExt(boolean showExt) {
    this.showExt = showExt;
    notifyDataSetChanged();
  }

  public int getFromPosition() {
    return fromPosition;
  }

  public int getToPosition() {
    return toPosition;
  }

  public void setFromPosition(int fromPosition) {
    this.fromPosition = fromPosition;
  }

  //  复制、剪切、粘贴、删除操作结束的统一操作，更新视图
  public void notifyOperationFinish() {
    leaveSelectMode();
    notifyDataSetChanged();
  }

  //  将所有文件加入选择列表，并开启多选模式
  public void selectAll() {
    for (FileView fileView : fileList) {
      checkStates.put(fileList.indexOf(fileView), true);
      selectSet.add(fileView);
    }
    goSelectMode();
  }

  //  用于拖拽操作后的选择状态更新
  public void setItemCheckStates(int position, boolean status) {
    if (status) {
      this.checkStates.put(position, true);
    } else {
      this.checkStates.delete(position);
    }
  }
}


