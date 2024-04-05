package com.bangnv.cafeorder.model.java;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Response<T> {

    @SerializedName("status")
    @Expose
    private Integer status;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("data")
    @Expose
    private T data;

    @SerializedName("optional")
    @Expose
    private Object optional;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Object getOptional() {
        return optional;
    }

    public void setOptional(Object optional) {
        this.optional = optional;
    }


}
