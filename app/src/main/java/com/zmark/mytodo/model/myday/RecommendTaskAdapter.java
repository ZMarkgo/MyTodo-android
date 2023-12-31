package com.zmark.mytodo.model.myday;

import static com.zmark.mytodo.invariant.Msg.CLIENT_REQUEST_ERROR;
import static com.zmark.mytodo.invariant.Msg.SERVER_INTERNAL_ERROR;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zmark.mytodo.MainApplication;
import com.zmark.mytodo.R;
import com.zmark.mytodo.handler.OnTaskContentClickListener;
import com.zmark.mytodo.handler.OnTaskSimpleAddedListener;
import com.zmark.mytodo.model.task.TaskSimple;
import com.zmark.mytodo.network.impl.MyDayTaskServiceImpl;
import com.zmark.mytodo.network.impl.TaskServiceImpl;
import com.zmark.mytodo.utils.TimeUtils;

import java.util.List;

public class RecommendTaskAdapter extends RecyclerView.Adapter<RecommendTaskAdapter.ViewHolder> {
    private final static String TAG = "RecommendTaskAdapter";
    private RecommendTaskList recommendTaskList;

    private OnTaskSimpleAddedListener onTaskSimpleAddedListener;

    private OnTaskContentClickListener onTaskContentClickListener;

    public void setOnTaskAddedListener(OnTaskSimpleAddedListener listener) {
        this.onTaskSimpleAddedListener = listener;
    }

    public void setOnTaskContentClickListener(OnTaskContentClickListener listener) {
        this.onTaskContentClickListener = listener;
    }

    public RecommendTaskAdapter() {
        this.recommendTaskList = new RecommendTaskList();
    }

    public void setRecommendTaskList(RecommendTaskList recommendTaskList) {
        this.recommendTaskList = recommendTaskList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_day_recommend_task_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<TaskSimple> taskSimpleRespList = recommendTaskList.getTaskSimpleRespList();
        TaskSimple taskSimple = taskSimpleRespList.get(position);
        holder.bind(taskSimple);
    }

    @Override
    public int getItemCount() {
        return recommendTaskList.getSize();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public LinearLayout taskContentLayout;
        public TextView taskTitleTextView;
        public TextView tagsTextView;
        public TextView dueDateTextView;
        public ImageView recurringIconImageView;
        public View addToMyDayLayoutButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            taskContentLayout = itemView.findViewById(R.id.taskContentLayout);
            taskTitleTextView = itemView.findViewById(R.id.todoTitle);
            tagsTextView = itemView.findViewById(R.id.tagLayout);
            dueDateTextView = itemView.findViewById(R.id.dueDate);
            recurringIconImageView = itemView.findViewById(R.id.recurringIcon);
            addToMyDayLayoutButton = itemView.findViewById(R.id.addToMyDayLayoutButton);
        }

        public void bind(TaskSimple taskSimple) {
            if (!taskSimple.isBinding()) {
                taskTitleTextView.setText(taskSimple.getTitle());
                // 设置CheckBox的选中状态和事件
                checkBox.setOnCheckedChangeListener(null);
                // 设置任务内容点击事件
                taskContentLayout.setOnClickListener(v -> {
                    if (onTaskContentClickListener != null) {
                        onTaskContentClickListener.onTaskContentClick(taskSimple);
                    }
                });
                // 使用 setTag 区分不同的 CheckBox
                checkBox.setTag(taskSimple.getId());
                checkBox.setChecked(taskSimple.getCompleted());
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        taskSimple.setCompleted(true);
                        TaskServiceImpl.completeTask(taskSimple.getId());
                    } else {
                        taskSimple.setCompleted(false);
                        TaskServiceImpl.unCompleteTask(taskSimple.getId());
                    }
                    // 通知Adapter刷新特定位置的项目
                    // 使用 Handler 延迟执行 notifyItemChanged
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // 设置标志位，表示正在执行onBindViewHolder
                        taskSimple.setBinding(true);
                        notifyItemChanged(getAdapterPosition());
                        // 重置标志位
                        taskSimple.setBinding(false);
                    });
                });
                // 显示标签
                String tagsString = taskSimple.getTagString();
                if (!tagsString.isEmpty()) {
                    if (tagsString.length() >= 6) {
                        tagsString = tagsString.substring(0, 6) + "...";
                    }
                    tagsTextView.setText(tagsString);
                } else {
                    tagsTextView.setVisibility(View.GONE);
                }
                // 显示到期时间
                String dueDate = taskSimple.getDueDate();
                String formattedDueDate = TimeUtils.getSingleFormattedDateStr(dueDate);
                dueDateTextView.setText(formattedDueDate);
                // 已过期的任务，设置颜色
                if (!taskSimple.getCompleted() && TimeUtils.isDateOverdue(dueDate)) {
                    dueDateTextView.setTextColor(MainApplication.getOverdueTaskTextColor());
                } else {
                    dueDateTextView.setTextColor(MainApplication.getTextColor());
                }
                // 显示循环图标（如果是循环任务）
                recurringIconImageView.setVisibility(taskSimple.isRecurring() ? View.VISIBLE : View.GONE);
                // 重要：确保每次设置 CheckBox 状态时都明确指定，避免 RecyclerView 复用导致的问题
                checkBox.setChecked(taskSimple.getCompleted());
                // 设置添加到我的一天的按钮
                addToMyDayLayoutButton.setOnClickListener(v -> {
                    addTaskToMyDay(taskSimple);
                });
            }
        }

        private void addTaskToMyDay(TaskSimple taskSimple) {
            Long taskId = taskSimple.getId();
            MyDayTaskServiceImpl.addTaskToMyDay(taskId, new MyDayTaskServiceImpl.Callbacks() {
                @Override
                public void onSuccess() {
                    // 1. 从推荐任务列表中移除该任务
                    recommendTaskList.removeTask(taskId);
                    // 2. 通知 Adapter 刷新
                    notifyItemRemoved(getAdapterPosition());
                    // 3. 通知 Listener 刷新
                    if (onTaskSimpleAddedListener != null) {
                        onTaskSimpleAddedListener.onTaskAdded(taskSimple);
                    }
                }

                @Override
                public void onFailure(Integer code, String msg) {
                    Log.w(TAG, "onFailure: " + code + " " + msg);
                    Toast.makeText(itemView.getContext(), msg, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onClientRequestError(Throwable t) {
                    Log.e(TAG, "onFailure: ", t);
                    Toast.makeText(itemView.getContext(), CLIENT_REQUEST_ERROR, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onServerInternalError() {
                    Toast.makeText(itemView.getContext(), SERVER_INTERNAL_ERROR, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
