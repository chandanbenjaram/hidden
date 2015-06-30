package droid.samepinch.co.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by imaginationcoder on 6/30/15.
 */
public abstract class RestBase<T> implements Serializable {
    @SerializedName("access_token")
    @Expose(deserialize = false, serialize = true)
    private String token;

    @SerializedName("command")
    private String cmd;

    @Expose(deserialize = true, serialize = false)
    private Integer status;

    @Expose(deserialize = true, serialize = false)
    private String message;

    @SerializedName("body")
    @Expose(deserialize = true, serialize = true)
    private T body;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

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

    public T getBody() {
        return this.body;
    }

    public void setBody(T body) {
        this.body = body;
    }

}
