package com.az.gitember.misc;

import com.sun.org.apache.regexp.internal.RE;

/**
 * Created by Igor_Azarny on 01 - Jan - 2017.
 */
public class RemoteOperationValue {

    public enum Result {
        OK,
        ERROR,
        AUTH_REQUIRED,
        NOT_AUTHORIZED
    }

    private Result result;
    private Object value;

    public RemoteOperationValue(Result result, Object value) {
        this.result = result;
        this.value = value;
    }

    public RemoteOperationValue(Object value) {
        this.result = Result.OK;
        this.value = value;
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
