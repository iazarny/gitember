package com.az.gitember.misc;

/**
 * Created by Igor_Azarny on 01 - Jan - 2017.
 */
public class RemoteOperationValue {

    public enum Result {
        OK,
        ERROR,
        AUTH_REQUIRED,
        GIT_AUTH_REQUIRED,
        NOT_AUTHORIZED
    }

    private Result result;
    private Object value;
    private Object secondValue;

    public RemoteOperationValue(Result result, Object value, Object secondValue) {
        this.result = result;
        this.value = value;
        this.secondValue = secondValue;
    }

    public RemoteOperationValue(Result result, Object value) {
        this.result = result;
        this.value = value;
    }

    public RemoteOperationValue(Object value) {
        this.result = Result.OK;
        this.value = value;
    }

    public Object getSecondValue() {
        return secondValue;
    }

    public void setSecondValue(Object secondValue) {
        this.secondValue = secondValue;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "RemoteOperationValue{" +
                "result=" + result +
                ", value=" + value +
                '}';
    }
}
