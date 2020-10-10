package com.example.filemanager.Adapter;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.Activity.ChildActivity;
import com.example.filemanager.FileView;
import com.example.filemanager.R;
import com.example.filemanager.Utils.GetFilesUtils;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileViewAdapter extends RecyclerView.Adapter<FileViewAdapter.ViewHolder> {
  private List<FileView> fileList;
  private static final String TAG = "FileViewAdapter";

  static class ViewHolder extends RecyclerView.ViewHolder {
    View fileView;
    ImageView fileImage;
    TextView fileName;
    TextView fileModifiedTime;
    TextView fileSize;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      fileView = itemView;
      fileImage = itemView.findViewById(R.id.file_image);
      fileName = itemView.findViewById(R.id.file_name);
      fileModifiedTime = itemView.findViewById(R.id.file_modified_time);
      fileSize = itemView.findViewById(R.id.file_size);
    }
  }

  public FileViewAdapter(List<FileView> fileList) {
    Collections.sort(fileList, GetFilesUtils.getInstance().defaultOrder());
    this.fileList = fileList;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_view_item, parent, false);
    final ViewHolder holder = new ViewHolder(view);
    holder.fileView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int position = holder.getAdapterPosition();
        FileView file = fileList.get(position);
        if (file.isFolder()) {
          Intent intent = new Intent(v.getContext(), ChildActivity.class);
          intent.putExtra("path", file.getFilePath());
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
            intent.setDataAndType(Uri.fromFile(new File(file.getFilePath())), mime);
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
    return holder;
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    FileView fileView = fileList.get(position);
    //  TODO: 更多类型的icon
    switch (fileView.getFileType()) {
      case "folder":
        holder.fileImage.setImageResource(R.drawable.filetype_folder);
        break;
      case "txt":
        holder.fileImage.setImageResource(R.drawable.filetype_txt);
        break;
      default:
        holder.fileImage.setImageResource(R.drawable.filetype_unknow);
        break;
    }
    holder.fileName.setText(fileView.getFileName());
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.CHINA);
    holder.fileModifiedTime.setText(format.format(new Date(fileView.getFileTime())));
    holder.fileSize.setText(GetFilesUtils.getInstance().getFileSizeStr(fileView.getFileSize()));
  }

  @Override
  public int getItemCount() {
    return this.fileList.size();
  }
}
