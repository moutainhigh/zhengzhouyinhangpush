package com.abtnetworks.totems.remote.policy.impl;

import com.abtnetworks.totems.remote.policy.PolicyRemoteCheckService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
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
 * @Description: 请写注释类
 * @date 2021/3/21
 */
@Slf4j
@Service
public class PolicyRemoteCheckServiceImpl implements PolicyRemoteCheckService {

    /**
     * rest
     **/
    @Value("${topology.policy-server-prefix}")
    private String policyServerPrefix;

    /**
     * 远程调用
     ***/

    @Qualifier("clientCredentialsOAuth2RestTemplate")
    @Autowired
    OAuth2RestTemplate oAuth2RestTemplate;


    @Override
    public JSONArray remotePolicyObjectDetail(String deviceUuid, String service, String type) {
        String path = policyServerPrefix + "device/query-object-detail";
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.put("name", Collections.singletonList(service));
        param.put("deviceUuid", Collections.singletonList(deviceUuid));
        param.put("type", Collections.singletonList(type));
        JSONArray jsonArray = null;
        ResponseEntity<JSONObject> responseEntity = oAuth2RestTemplate.postForEntity(path, param, JSONObject.class);

        if (responseEntity == null) {
            log.error("远程调用device/query-object-detail接口返回为null，异常失败设备id{}", deviceUuid);
        } else if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            log.error("远程调用device/query-object-detaile接口返回为状态非200，检查异常失败设备id{}", deviceUuid);
        } else {
            JSONObject jsonObject = responseEntity.getBody();
            jsonArray = jsonObject.getJSONArray("data");
        }
        return jsonArray;
    }
}
