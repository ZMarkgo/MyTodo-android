package com.zmark.mytodo.fragment.taskadd;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.zmark.mytodo.MainApplication;
import com.zmark.mytodo.R;
import com.zmark.mytodo.api.ApiUtils;
import com.zmark.mytodo.api.TaskService;
import com.zmark.mytodo.api.bo.task.req.TaskCreatReq;
import com.zmark.mytodo.api.invariant.Msg;
import com.zmark.mytodo.api.result.Result;
import com.zmark.mytodo.api.result.ResultCode;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTaskBottomSheetFragment extends BottomSheetDialogFragment {
    private static final String TAG = "AddTaskBottomSheetFragment";
    private Boolean isImportant = false;
    private Boolean isUrgent = false;
    private EditText newTaskTitleInput;
    private EditText newTaskDescriptionInput;
    private TextView listTextView;
    private TextView endDateTextView;
    private TextView endTimeTextView;
    private TextView priorityTextView;
    private TextView tagTextView;
    private TextView reminderTimeTextView;

    public AddTaskBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View bottomSheetView = inflater.inflate(R.layout.fragment_add_task, container, false);// 获取底部抽屉中的 EditText
        this.findViews(bottomSheetView);
        bottomSheetView.findViewById(R.id.timeSetImageView).setOnClickListener(view -> this.handleDueDateTimePicker());
        bottomSheetView.findViewById(R.id.prioritySetImageView).setOnClickListener(view -> {
            this.handlePrioritySet();
        });
        bottomSheetView.findViewById(R.id.tagSetImageView).setOnClickListener(view -> {
            // todo 处理用户点击设置标签的逻辑
            Toast.makeText(requireContext(), "设置标签", Toast.LENGTH_SHORT).show();
        });
        bottomSheetView.findViewById(R.id.reminderSetImageView).setOnClickListener(view -> {
            // todo 处理用户点击设置提醒的逻辑
            Toast.makeText(requireContext(), "设置提醒", Toast.LENGTH_SHORT).show();
        });
        bottomSheetView.findViewById(R.id.listSetImageView).setOnClickListener(view -> {
            // todo 处理用户点击设置清单的逻辑
            Toast.makeText(requireContext(), "设置清单", Toast.LENGTH_SHORT).show();
        });
        bottomSheetView.findViewById(R.id.confirmButton).setOnClickListener(view -> {
            // 处理用户点击发送按钮的逻辑
            if (newTaskTitleInput.getText().toString().isEmpty()) {
                // 如果待办事项为空，则不执行添加操作
                Toast.makeText(requireContext(), Msg.NO_TASK_TITLE, Toast.LENGTH_SHORT).show();
                return;
            }
            String title = newTaskTitleInput.getText().toString();
            String description = Optional.of(newTaskDescriptionInput.getText().toString()).orElse("");
            String endDate = Optional.of(endDateTextView.getText().toString()).orElse(null);
            String endTime = Optional.of(endTimeTextView.getText().toString()).orElse(null);
            // 执行添加待办事项的操作
            TaskCreatReq createReq = new TaskCreatReq();
            createReq.setTitle(title);
            createReq.setDescription(description);
            createReq.setEndDate(endDate);
            createReq.setEndTime(endTime);
            createReq.setImportant(isImportant);
            createReq.setUrgent(isUrgent);
            createNewTask(createReq);
        });

        // 在输入法弹出时调整窗口的大小
        Dialog dialog = getDialog();
        if (dialog != null) {
            // 设置Dialog的windowSoftInputMode属性
            Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        return bottomSheetView;
    }

    private void handleDueDateTimePicker() {
        // 获取当前日期和时间
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 创建日期选择器对话框
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, monthOfYear, dayOfMonth1) -> {
                    // 在日期选择后，创建时间选择器对话框
                    TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                            (view1, selectedHourOfDay, selectedMinute) -> {
                                // 处理用户选择的日期和时间

                                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                        selectedYear, monthOfYear + 1, dayOfMonth1);

                                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d",
                                        selectedHourOfDay, selectedMinute);

                                endDateTextView.setText(selectedDate);
                                endTimeTextView.setText(selectedTime);
                            }, hourOfDay, minute, DateFormat.is24HourFormat(requireContext()));

                    // 显示时间选择器对话框
                    timePickerDialog.show();
                }, year, month, dayOfMonth);

        // 显示日期选择器对话框
        datePickerDialog.show();
    }

    private void handlePrioritySet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("设置优先级");
        String[] priorityArray = {"重要且紧急", "重要不紧急", "紧急不重要", "不重要不紧急"};

        builder.setSingleChoiceItems(priorityArray, 0, (dialog, which) -> {
            switch (which) {
                case 0:
                    isImportant = true;
                    isUrgent = true;
                    priorityTextView.setText("重要且紧急");
                    break;
                case 1:
                    isImportant = true;
                    isUrgent = false;
                    priorityTextView.setText("重要不紧急");
                    break;
                case 2:
                    isImportant = false;
                    isUrgent = true;
                    priorityTextView.setText("紧急不重要");
                    break;
                case 3:
                    isImportant = false;
                    isUrgent = false;
                    priorityTextView.setText("不重要不紧急");
                    break;
                default:
                    break;
            }
            dialog.dismiss();
        });
        builder.show();
    }
    
    private void createNewTask(TaskCreatReq taskCreatReq) {
        TaskService taskService = MainApplication.getTaskService();
        Call<Result<Object>> call = taskService.createNewTask(taskCreatReq);
        call.enqueue(new Callback<Result<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Result<Object>> call, @NonNull Response<Result<Object>> response) {
                if (response.isSuccessful()) {
                    Result<Object> result = response.body();
                    assert result != null;
                    if (result.getCode() == ResultCode.SUCCESS.getCode()) {
                        Log.i(TAG, "onResponse: 任务创建成功");
                        Toast.makeText(requireContext(), "任务创建成功", Toast.LENGTH_SHORT).show();
                        // 关闭底部抽屉
                        dismiss();
                    } else {
                        Log.w(TAG, "code:" + result.getCode() + " onResponse: " + result.getMsg());
                        Toast.makeText(requireContext(), result.getMsg(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    ApiUtils.handleResponseError(TAG, response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<Object>> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(requireContext(), Msg.CLIENT_REQUEST_ERROR, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findViews(View view) {
        newTaskTitleInput = view.findViewById(R.id.newTaskTitleInput);
        newTaskDescriptionInput = view.findViewById(R.id.newTaskDescriptionInput);
        listTextView = view.findViewById(R.id.listTextView);
        endDateTextView = view.findViewById(R.id.endDateTextView);
        endTimeTextView = view.findViewById(R.id.endTimeTextView);
        priorityTextView = view.findViewById(R.id.priorityTextView);
        tagTextView = view.findViewById(R.id.tagTextView);
        reminderTimeTextView = view.findViewById(R.id.reminderTimeTextView);
    }
}