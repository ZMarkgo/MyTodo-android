package com.zmark.mytodo.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.zmark.mytodo.service.result.Result;
import com.zmark.mytodo.service.result.ResultCode;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiUtils {

    public static void handleResponseError(String tag, Response<?> response) {
        try (ResponseBody errorBody = response.errorBody()) {
            Log.e(tag, "handleResponseError: " + errorBody);
        } catch (Exception e) {
            Log.e(tag, "handleResponseError: " + e.getMessage(), e);
        }
    }

    public interface Callbacks<T> {
        /**
         * 请求成功，且业务逻辑成功的回调
         */
        void onSuccess(T data);

        /**
         * 请求成功的，但是业务逻辑失败的回调
         */
        void onFailure(Integer code, String msg);

        /**
         * 客户端请求失败的回调
         */
        void onClientRequestError(Throwable t);

        /**
         * 服务器内部错误的回调
         */
        void onServerInternalError();
    }

    public static <T> void doRequest(@NonNull Call<Result<T>> call, @NonNull Callbacks<T> callbacks) {
        call.enqueue(new Callback<Result<T>>() {
            @Override
            public void onResponse(@NonNull Call<Result<T>> call, @NonNull Response<Result<T>> response) {
                if (response.isSuccessful()) {
                    Result<T> result = response.body();
                    if (result == null) {
                        callbacks.onServerInternalError();
                        return;
                    }
                    if (result.getCode() == null || result.getCode() != ResultCode.SUCCESS.getCode()) {
                        callbacks.onFailure(result.getCode(), result.getMsg());
                        return;
                    }
                    callbacks.onSuccess(result.getObject());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result<T>> call, @NonNull Throwable t) {
                callbacks.onClientRequestError(t);
            }
        });
    }
}
