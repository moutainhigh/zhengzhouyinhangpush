package com.abtnetworks.totems.common;

import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.vo.ResultResponseVO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;


/**
 * 控制器支持类
 * @author ThinkGem
 * @version 2013-3-23
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
     * 验证Bean实例对象
     */
    @Autowired
    protected Validator validator;
    /***
     * 校验参数
     * @param tClass
     * @param <T>
     */
    protected <T,G>void validatorParam( T tClass,Class<G>... gClass){
        Set<ConstraintViolation<T>> result = validator.validate(tClass,gClass);
        if (CollectionUtils.isNotEmpty(result) && result.size()>0){
            Iterator<ConstraintViolation<T>> iteratorResult = result.iterator();
            while(iteratorResult.hasNext()){
                ConstraintViolation<T> message = iteratorResult.next();
                throw new IllegalArgumentException(message.getMessage());
            }

        }
    }
    /**
     * 返回值
     * @param status
     * @param data
     * @param errcode
     * @param errmsg
     * @return
     */
    @ResponseBody
    public <T>JSONObject returnJSON (String status, T data, String errcode, String errmsg) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status",status==""?"0":status);
        jsonObject.put("data",data);
        jsonObject.put("errcode",errcode);
        jsonObject.put("errmsg",errmsg);

        return jsonObject;
    }

    /**
     * 由于转json会自动去掉null字段，这里加上这种
     * @param data
     * @param <T>
     * @return
     */
    @ResponseBody
    public <T>ResultResponseVO returnResponseSuccess (T data) {
        ResultResponseVO resultResponseVO = new ResultResponseVO(data, "0");
        return resultResponseVO;
    }

    /**
     * 由于转json会自动去掉null字段，这里加上这种

     * @param <T>
     * @return
     */
    @ResponseBody
    public <T>ResultResponseVO returnResponseFail (String status,String msg) {
        ResultResponseVO resultResponseVO = new ResultResponseVO(status, msg);
        return resultResponseVO;
    }
    @ResponseBody
    public JSONObject returnJSON (String status, JSONArray data, String errcode, String errmsg) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status",status==""?"0":status);
        jsonObject.put("data",data);
        jsonObject.put("errcode",errcode);
        jsonObject.put("errmsg",errmsg);

        return jsonObject;
    }

    /**
     *
     * @param status
     * @param data
     * @param errcode
     * @param errmsg
     * @return
     */
    @ResponseBody
    public JSONObject returnJSON (String status, String data, String errcode, String errmsg) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status",status==""?"0":status);
        jsonObject.put("data",data);
        jsonObject.put("errcode",errcode);
        jsonObject.put("errmsg",errmsg);

        return jsonObject;
    }



    /**
     * 根据返回值获取返回值JSON对象
     * @param rc 返回值
     * @return 对应结构JSON对象
     */
    public JSONObject getReturnJSON(int rc) {
        JSONObject jsonObject = new JSONObject();
        return getReturnJSON(rc, jsonObject);
    }

    /**
     * 根据返回值和错误信息获取返回json对象
     * @param rc 返回值
     * @param msg 错误信息
     * @return 返回值JSON对象
     */
    public JSONObject getReturnJSON(int rc, String msg) {
        String status = "-1";
        String errcode = "";
        String errmsg = "";
        JSONObject jsonObject = new JSONObject();

        if(rc == ReturnCode.POLICY_MSG_OK) {
            status = "0";
        }
        errcode = String.valueOf(rc);
        errmsg = msg;
        return returnJSON(status, jsonObject, errcode, errmsg);
    }

    /**
     * 根据返回值和JSON对象获取返回值JSON对象
     * @param rc 返回值
     * @param t 返回值相关JSON对象
     * @return 对应返回值JSON对象
     */
    public <T extends Object>JSONObject getReturnJSON(int rc, T t) {
        String status = "-1";
        String errcode = "";
        String errmsg = "";

        if(rc == ReturnCode.POLICY_MSG_OK) {
            status = "0";
        }
        errcode = String.valueOf(rc);
        errmsg = ReturnCode.getMsg(rc);
        String jsonObjectString = JSONObject.toJSONString(t);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);
        return returnJSON(status, jsonObject, errcode, errmsg);
    }

    /**
     * 根据返回值和JSON对象获取返回值JSON对象
     * @param rc 返回值
     * @param jsonArray 返回值相关JSON对象
     * @return 对应返回值JSON对象
     */
    public JSONObject getReturnJSON(int rc, JSONArray jsonArray) {
        String status = "-1";
        String errcode = "";
        String errmsg = "";

        if(rc == ReturnCode.POLICY_MSG_OK) {
            status = "0";
        }
        errcode = String.valueOf(rc);
        errmsg = ReturnCode.getMsg(rc);
        return returnJSON(status, jsonArray, errcode, errmsg);
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
