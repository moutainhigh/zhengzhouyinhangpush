package com.abtnetworks.totems.translation;

public class BaseRequest<T> extends PaginationParamDto {

    private T val;

    public T getVal() {
        return val;
    }

    public void setVal(T val) {
        this.val = val;
    }
}