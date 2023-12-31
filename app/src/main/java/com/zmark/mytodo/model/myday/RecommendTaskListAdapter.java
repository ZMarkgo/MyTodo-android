package com.zmark.mytodo.model.myday;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zmark.mytodo.R;
import com.zmark.mytodo.handler.OnTaskContentClickListener;
import com.zmark.mytodo.handler.OnTaskSimpleAddedListener;

import java.util.List;

public class RecommendTaskListAdapter extends RecyclerView.Adapter<RecommendTaskListAdapter.ViewHolder> {
    private final static String TAG = "RecommendTaskListAdapter";
    private final List<RecommendTaskList> recommendTaskLists;

    private OnTaskSimpleAddedListener onTaskSimpleAddedListener;

    private OnTaskContentClickListener onTaskContentClickListener;

    public void setOnTaskAddedListener(OnTaskSimpleAddedListener listener) {
        this.onTaskSimpleAddedListener = listener;
    }

    public void setOnTaskContentClickListener(OnTaskContentClickListener listener) {
        this.onTaskContentClickListener = listener;
    }

    public RecommendTaskListAdapter(List<RecommendTaskList> recommendTaskLists) {
        this.recommendTaskLists = recommendTaskLists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_day_recommend_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendTaskList recommendTaskList = recommendTaskLists.get(position);
        holder.bind(recommendTaskList);
    }

    @Override
    public int getItemCount() {
        return recommendTaskLists.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RecommendTaskAdapter taskAdapter;
        private final TextView recommendListTitle;

        private final LinearLayout noTaskMsgView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recommendListTitle = itemView.findViewById(R.id.recommendListTitle);
            RecyclerView recommendListView = itemView.findViewById(R.id.recommendListView);
            noTaskMsgView = itemView.findViewById(R.id.noTaskMsgView);

            // Create a RecommendTaskAdapter for the nested RecyclerView
            taskAdapter = new RecommendTaskAdapter();
            taskAdapter.setOnTaskAddedListener(taskSimple -> {
                // 更新UI
                bind(recommendTaskLists.get(getAdapterPosition()));
                if (onTaskSimpleAddedListener != null) {
                    onTaskSimpleAddedListener.onTaskAdded(taskSimple);
                }
            });
            taskAdapter.setOnTaskContentClickListener(taskSimple -> {
                if (onTaskContentClickListener != null) {
                    onTaskContentClickListener.onTaskContentClick(taskSimple);
                }
            });
            recommendListView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recommendListView.setAdapter(taskAdapter);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void bind(RecommendTaskList recommendTaskList) {
            recommendListTitle.setText(recommendTaskList.getTitle());
            if (recommendTaskList.isEmpty()) {
                noTaskMsgView.setVisibility(View.VISIBLE);
            } else {
                noTaskMsgView.setVisibility(View.GONE);
                taskAdapter.setRecommendTaskList(recommendTaskList);
                taskAdapter.notifyDataSetChanged();
            }
        }
    }
}
