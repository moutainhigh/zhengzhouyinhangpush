package com.abtnetworks.totems.common.vo;

import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 返回类型
 * @date 2021/1/27
 */
@Data
public class ResultResponseVO<D> {
    /****数据**/
    private D data;
    /***状态***/
    private String status;
    /***错误码***/
    private String errCode;
    /***错误信息***/
    private String errMsg;


    public ResultResponseVO(D data, String status, String errCode, String errMsg) {

        this.data = data;
        this.status = status;
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public ResultResponseVO() {

    }

    public ResultResponseVO(D data, String status) {
        this.data = data;
        this.status = status;
    }

    public ResultResponseVO( String status,  String errMsg) {
        this.status = status;
        this.errMsg = errMsg;
    }
}
