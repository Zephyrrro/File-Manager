package com.example.filemanager;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * @author gaofan
 * 处理拖拽功能的类
 */
public class ItemTouchCallBack extends ItemTouchHelper.Callback{
    private static final String TAG = "ItemTouchCallBack";
    private OnItemTouchListener onItemTouchListener;

    public ItemTouchCallBack(OnItemTouchListener onItemTouchListener) {
        this.onItemTouchListener = onItemTouchListener;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        Log.i(TAG,"getMovementFlags");
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager ||
                recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager){
            //此处不需要进行滑动操作，可设置为除4和8之外的整数，这里设为0
            //不支持滑动
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0 );
        }else {
            //如果是LinearLayoutManager则只能向上向下滑动
            //此处第二个参数设置支持向右滑动
            return makeMovementFlags(ItemTouchHelper.UP   | ItemTouchHelper.DOWN , 0 );
        }
    }


    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        Log.i(TAG,"clearView:" + viewHolder.getAdapterPosition());
    }

    @Override
    public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        Log.i(TAG,"onMoved!from:" + fromPos + "to:" + toPos);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // 记录下来项目的起始和目标位置 ，用于进行拖拽逻辑
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition   = target.getAdapterPosition();
        Log.i(TAG,"onMove,from:" + fromPosition + "to:" + toPosition);
        // 通知监听者进行move动作
        onItemTouchListener.onMove(fromPosition,toPosition);
        return true;
    }


    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // 向左滑动 本项目不使用
        Log.i(TAG,"onSwiped");
    }
    /**
     * 移动交换数据的更新监听 - 监听者模式
     */
    public interface OnItemTouchListener {
        //拖动Item时调用
        void onMove(int fromPosition, int toPosition);
    }
}
