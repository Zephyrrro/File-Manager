package com.example.filemanager.Utils;

import android.content.Context;

public class DensityUtil {
  //  根据屏幕分辨率，将 dp 转换为 px
  public static int dip2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }
}
