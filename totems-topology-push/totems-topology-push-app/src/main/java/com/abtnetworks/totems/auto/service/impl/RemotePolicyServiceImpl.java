package com.abtnetworks.totems.auto.service.impl;

import com.abtnetworks.totems.auto.service.RemotePolicyService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

/**
 * @author Administrator
 * @Title:
 * @Description: api的公共程调用的类
 * @date 2020/9/14
 */
@Service
public class RemotePolicyServiceImpl implements RemotePolicyService {
    /***
     * 日志记录
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(RemotePolicyServiceImpl.class);
    /***/
    @Qualifier("clientCredentialsOAuth2RestTemplate")
    @Autowired
    OAuth2RestTemplate oAuth2RestTemplate;
    /**
     * rest policy
     **/
    @Value("${topology.policy-server-prefix}")
    private String policyServerPrefix;


    @Override
    public JSONArray remotePolicyDetailByIpTerms(String ipTerms, Boolean skipAny, String deviceUuid, Integer type) {
        String path = policyServerPrefix + "policy/rule-list-search";
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.put("deviceUuid", Collections.singletonList(deviceUuid));
        param.put("type", Collections.singletonList(type));
        param.put("pageSize", Collections.singletonList("-1"));
        param.put("currentPage", Collections.singletonList("0"));
        param.put("ipTerms", Collections.singletonList(ipTerms));
        param.put("skipAny", Collections.singletonList(skipAny));
        JSONArray jsonArray = null;
        ResponseEntity<JSONObject> responseEntity = oAuth2RestTemplate.postForEntity(path, param, JSONObject.class);
        if (responseEntity == null) {
            LOGGER.error("远程调用policy/rule-list-search接口返回为null，异常失败设备id{}", deviceUuid);
        } else if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            LOGGER.error("远程调用policy/rule-list-search接口返回为状态非200，检查异常失败设备id{}", deviceUuid);
        } else {
            JSONObject jsonObject = responseEntity.getBody();
            jsonArray = jsonObject.getJSONArray("data");
        }

        return jsonArray;
    }


}
