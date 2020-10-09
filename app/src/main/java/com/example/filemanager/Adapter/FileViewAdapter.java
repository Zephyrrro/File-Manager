package com.example.filemanager.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.Activity.ChildActivity;
import com.example.filemanager.FileView;
import com.example.filemanager.R;

import java.util.List;

public class FileViewAdapter extends RecyclerView.Adapter<FileViewAdapter.ViewHolder> {
  private List<FileView> fileList;

  static class ViewHolder extends RecyclerView.ViewHolder {
    View fileView;
    ImageView fileImage;
    TextView fileName;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      fileView = itemView;
      fileImage = itemView.findViewById(R.id.file_image);
      fileName = itemView.findViewById(R.id.file_name);
    }
  }

  public FileViewAdapter(List<FileView> fileList) {
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
          Toast.makeText(v.getContext(), "这是文件", Toast.LENGTH_SHORT).show();
          //  TODO: open file
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
  }

  @Override
  public int getItemCount() {
    return this.fileList.size();
  }
}
