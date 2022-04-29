package com.abtnetworks.totems.proxy.fortress.service;

import com.abtnetworks.totems.proxy.common.dto.PythonOutDTO;
import com.abtnetworks.totems.proxy.fortress.service.impl.PythonFortressMachineServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/8/6
 */
public interface PythonFortressMachineService {
    public final static Logger log = LoggerFactory.getLogger(PythonFortressMachineServiceImpl.class);

    /**
     * 选择不同方式进入代理
     *
     * @param fileName
     * @param deviceIp
     * @param loginName
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    default String choseProxyPythonPath(String fileName, String deviceIp, String loginName, String prePath,String deviceName,String enableLoginName) throws Exception {


        StringBuffer stringBuffer = new StringBuffer(prePath).append(fileName);
        String path = stringBuffer.toString();
        log.info("python脚本的路径是{}", path);

        return path;


    }

    /**
     * python调用堡垒机获取密码
     *
     * @param deviceIp
     * @param path
     * @param loginName
     * @return
     */
    PythonOutDTO pythonFortressGetPassword(String deviceIp, String path, String loginName,String deviceName,String enableLoginName) throws Exception;


}
