package com.kang.websocket.bean.out;

public class ReturnOut<T> {
    //0错误，1成功
    private int resultCode=1;
    private String resultMessage="ok";
    private T data;
   // private String url;


    public ReturnOut() {
    }

    public ReturnOut(T data) {
        this.data = data;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}