package com.example.filemanager.Adapter;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;
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
import com.example.filemanager.Activity.SearchActivity;
import com.example.filemanager.FileView;
import com.example.filemanager.R;
import com.example.filemanager.Utils.GetFilesUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FileViewAdapter extends RecyclerView.Adapter<FileViewAdapter.ViewHolder> {
  private boolean selectMode;
  private List<FileView> fileList;
  private static final String TAG = "FileViewAdapter";
  private List<ViewHolder> viewHolders = new ArrayList<>();
  private Map<String, Integer> fileTypeIconMap = new HashMap<String, Integer>();

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
    fileTypeIconMap.put("unknown", R.drawable.filetype_unknow);
    fileTypeIconMap.put("folder", R.drawable.filetype_folder);
    fileTypeIconMap.put("txt", R.drawable.filetype_txt);
    fileTypeIconMap.put("pdf", R.drawable.filetype_pdf);
    fileTypeIconMap.put("mp3", R.drawable.filetype_mp3);
    fileTypeIconMap.put("mp4", R.drawable.filetype_mp4);
    fileTypeIconMap.put("jpg", R.drawable.filetype_jpg);
    fileTypeIconMap.put("png", R.drawable.filetype_png);
  }


  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_view_item, parent, false);
    final ViewHolder holder = new ViewHolder(view);
    holder.fileView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (selectMode) {
          holder.selected.setChecked(!holder.selected.isChecked());
          return;
        }
        int position = holder.getAdapterPosition();
        FileView file = fileList.get(position);
        if (file.isFolder()) {
          Intent intent = new Intent(v.getContext(), MainActivity.class);
          intent.putExtra("path", file.getFilePath().toString());
          v.getContext().startActivity(intent);
        } else {
          boolean isNeedMatch = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
          StrictMode.VmPolicy defaultVmPolicy = null;
          try {
            if (isNeedMatch) {
              defaultVmPolicy = StrictMode.getVmPolicy();
              StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
              StrictMode.setVmPolicy(builder.build());
            }
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mime = mimeTypeMap.getMimeTypeFromExtension(file.getFileType());
            mime = TextUtils.isEmpty(mime) ? "" : mime;
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(file.getFilePath().toString())), mime);
            v.getContext().startActivity(intent);
          } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(v.getContext(), "无法打开", Toast.LENGTH_SHORT).show();
          } finally {
            if (isNeedMatch) {
              StrictMode.setVmPolicy(defaultVmPolicy);
            }
          }
        }
      }
    });

    holder.fileView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        if (!selectMode) {
          selectMode = true;
          ((BaseActivity) parent.getContext()).setFabVisibility(View.VISIBLE);
          for (ViewHolder viewHolder : viewHolders) {
            viewHolder.selected.setVisibility(View.VISIBLE);
          }
        }
        goSelectMode();
        holder.selected.setChecked(true);
        return true;
      }
    });
    viewHolders.add(holder);
    return holder;
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    FileView fileView = fileList.get(position);
    //  TODO: 更多类型的icon
    Integer fileImage = fileTypeIconMap.get(fileView.getFileType());
    if (fileImage == null) {
      fileImage = fileTypeIconMap.get("unknown");
    }
    holder.fileImage.setImageResource(fileImage);
    holder.fileName.setText(fileView.getFileName());
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.CHINA);
    holder.fileModifiedTime.setText(format.format(new Date(fileView.getFileTime())));
    holder.fileSize.setText(GetFilesUtils.getInstance().getFileSizeStr(fileView.getFileSize()));
    holder.selected.setVisibility(selectMode ? View.VISIBLE : View.GONE);
  }


  @Override
  public int getItemCount() {
    return this.fileList.size();
  }

  public void goSelectMode() {
    selectMode = true;
    for(ViewHolder viewHolder: viewHolders){
      viewHolder.selected.setVisibility(View.VISIBLE);
    }
  }

  public boolean leaveSelectMode(){
    if(!selectMode){
      return false;
    }
    selectMode = false;
    for (ViewHolder viewHolder : viewHolders) {
      viewHolder.selected.setChecked(false);
      viewHolder.selected.setVisibility(View.GONE);
    }
    return false;
  }

  public List<FileView> getSelected() {
    if (!selectMode) {
      return new ArrayList<>();
    }
    List<FileView> selectedFileView = new ArrayList<>();
    for (int i = 0; i < viewHolders.size(); i++) {
      if (viewHolders.get(i).selected.isChecked()) {
        selectedFileView.add(fileList.get(i));
      }
    }
    return selectedFileView;
  }

  public List<FileView> getFileList() {
    return fileList;
  }

  public void addFile(File file){
    if(fileList.add(new FileView(file))){
      notifyItemInserted(fileList.size()-1);
    }
  }
}
