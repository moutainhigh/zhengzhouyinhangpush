/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.abtnetworks.totems.disposal;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.io.IOException;


/**
 * 控制器支持类
 * @author hw
 * @version 2018-11-15
 */
@Controller
public abstract class BaseController {

	/**
	 * 日志对象
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 基础路径
	 */
	@Value("${startPath}")
	protected String startPath;
	
	/**
	 * 应急处置基础路径
	 */
	@Value("disposal")
	protected String disposalPath;

	/**
	 * 验证Bean实例对象
	 */
	@Autowired
	protected Validator validator;

	@Autowired
	protected LogClientSimple logClientSimple;

	/**
	 * 返回值
	 * @param status
	 * @param data
	 * @param errcode
	 * @param errmsg
	 * @return
	 */
	@ResponseBody
	public JSONObject returnJSON (String status, JSONObject data, String errcode, String errmsg) {

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status",status==""?"0":status);
		jsonObject.put("data",data);
		jsonObject.put("errcode",errcode);
		jsonObject.put("errmsg",errmsg);

		return jsonObject;
	}

	/**
	 * 客户端返回字符串
	 * @param response
	 * @param string
	 * @return
	 */
	protected String renderString(HttpServletResponse response, String string, String type) {
		try {
			response.reset();
	        response.setContentType(type);
	        response.setCharacterEncoding("utf-8");
			response.getWriter().print(string);
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * 参数绑定异常
	 */
	@ExceptionHandler({BindException.class, ConstraintViolationException.class, ValidationException.class})
    public String bindException() {  
        return "error/400";
    }
	

	
}
