package com.abtnetworks.totems.common.config;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;

import java.util.*;

@Configuration
public class VmwareInterfaceStatusConfig implements InitializingBean {
    private boolean vmInterfaceAvailable = false;

    private final static Logger logger = LoggerFactory.getLogger(VmwareInterfaceStatusConfig.class);
    @Qualifier("clientCredentialsOAuth2RestTemplate")
    @Autowired
    private OAuth2RestTemplate OAuth2RestTemplate;

    @Value("${topology.vmsdn-server-prefix:http://${service_connection_url}:8085/}")
    private String vmwareServerPrefix;
    @Value("${topology.restpath.checkVMwareService:vmsdn/cloudAdds/checkVMwareService}")
    private String checkVMwareService;

    /**
     * 判断vmware服务是否开启，如果开启则push支持云策略开通
     * @return
     */
    public boolean isVmInterfaceAvailable() {
        return vmInterfaceAvailable;
    }

    public boolean checkVmInterfaceAvailableNow() {
        if(test()){
            logger.info("vmware服务正常，支持云开通模式");
            vmInterfaceAvailable = true;
        }else{
            vmInterfaceAvailable = false;
            logger.warn("vmware服务未启动，不支持云开通模式");
        }
        return vmInterfaceAvailable;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(test()){
            logger.info("vmware服务正常，支持云开通模式");
            vmInterfaceAvailable = true;
        }else{
            logger.warn("vmware服务未启动，不支持云开通模式");
        }
    }

    public boolean test() {
        String path = vmwareServerPrefix + checkVMwareService;
        JSONObject jsonObject = new JSONObject();
        ResponseEntity<JSONObject> responseEntity = null;
        logger.info("远程调用{}接口", path);
        try {
            responseEntity = OAuth2RestTemplate.postForEntity(path, "", JSONObject.class);
        } catch (Exception e) {
            logger.warn(String.format("远程调用%s接口服务端失败", path));
            return false;
        }
        if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            logger.warn("远程调用{}接口返回状态码：{}", path, responseEntity.getStatusCodeValue());
            return false;
        } else {
            jsonObject = responseEntity.getBody();
            logger.info("远程调用{}接口返回参数{}", path, jsonObject);
            Integer code = Integer.parseInt(jsonObject.get("code").toString());
            if (code != null && code == 0) {
                return (Boolean) jsonObject.get("data");
            } else {
                logger.warn("远程调用{}接口失败,errorCode:{};errmsg:{}", path, jsonObject.get("errcode"), jsonObject.get("errmsg"));
                return false;
            }
        }
    }
}
