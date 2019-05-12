package com.czj.dto;

/**
 * 返回给前端的结果
 * @author czj
 */
public class SeckillResult<T> {

    //是否请求成功
    private boolean success;

    //请求返回的数据
    private T data;

    //请求失败时返回的错误信息
    private String error;

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
