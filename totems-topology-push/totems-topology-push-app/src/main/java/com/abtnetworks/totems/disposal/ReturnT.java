package com.abtnetworks.totems.disposal;

import java.io.Serializable;

/**
 * 公共返回结果 common return
 * @Author hw
 * @Description 返回值公约
 * @Date 2019-1-8 16:00:41
 * @param <T>
 */
public class ReturnT<T> implements Serializable {

	private static final long serialVersionUID = 45727763625719352L;

	/**
	 * 请求成功
	 */
	public static final int SUCCESS_CODE = 0;
	/**
	 * 处理失败，系统繁忙
	 */
	public static final int FAIL_CODE = -1;

	public static final String STR_MSG_SUCCESS = "Success";
	public static final String STR_MSG_ERROR = "Service Currently Unavailable";

	public static final String STR_ERROR_SUB_CODE = "unknow-error";
	public static final String STR_ERROR_SUB_MSG = "系统繁忙";

	public static final ReturnT<String> SUCCESS = new ReturnT<String>(SUCCESS_CODE, STR_MSG_SUCCESS);
	public static final ReturnT<String> FAIL = new ReturnT<String>(FAIL_CODE, STR_MSG_ERROR);

	/**
	 * 网关返回码,详见文档
	 */
	private int code;
	/**
	 * 网关返回码描述,详见文档
	 */
	private String msg;
	/**
	 * 数据
	 */
	private T data;

	/**
	 * 业务返回码，参见具体的API接口文档
	 */
	private String sub_code;
	/**
	 * 业务返回码描述，参见具体的API接口文档
	 */
	private String sub_msg;

	/**
	 * 签名,详见文档
	 */
	private String sign;

	public ReturnT(){

	}

	public ReturnT(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	/**
	 * 业务error 构造函数
	 * @param sub_code
	 * @param sub_msg
	 */
	public ReturnT(String sub_code, String sub_msg) {
		this.code = FAIL_CODE;
		this.msg = STR_MSG_ERROR;
		this.sub_code = sub_code;
		this.sub_msg = sub_msg;
	}

	public ReturnT(int code, String msg, T data, String sub_code, String sub_msg) {
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.sub_code = sub_code;
		this.sub_msg = sub_msg;
	}

	/**
	 * Success 构造返回值（一般用于json数据返回）
	 * @param data
	 */
	public ReturnT(T data) {
		this.code = SUCCESS_CODE;
		this.msg = STR_MSG_SUCCESS;
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getSub_code() {
		return sub_code;
	}

	public void setSub_code(String sub_code) {
		this.sub_code = sub_code;
	}

	public String getSub_msg() {
		return sub_msg;
	}

	public void setSub_msg(String sub_msg) {
		this.sub_msg = sub_msg;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	@Override
	public String toString() {
		return "ReturnT [code=" + code + ", msg=" + msg + ", data=" + data +
				", sub_code=" + sub_code + ", sub_msg=" + sub_msg + ", sign=" + sign + "]";
	}

}
