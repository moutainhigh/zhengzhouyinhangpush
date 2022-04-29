package com.abtnetworks.totems.branch.service.impl;

import com.abtnetworks.totems.branch.dto.Branch;
import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.branch.service.RemoteBranchService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/11/16
 */
@Slf4j
@Service
public class RemoteBranchServiceImpl implements RemoteBranchService {

    @Value("${topology.topology-server-prefix}")
    private String topologyServerPrefix;

    /***/
    @Qualifier("clientCredentialsOAuth2RestTemplate")
    @Autowired
    OAuth2RestTemplate oAuth2RestTemplate;





    @Override
    public UserInfoDTO findOne(String id) {
        String path = topologyServerPrefix + "topology/ums/queryUser.action";
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.put("id", Collections.singletonList(id));
        UserInfoDTO userInfo = null;
        try {
            ResponseEntity<JSONObject> responseEntity = oAuth2RestTemplate.postForEntity(path, param, JSONObject.class);
            if (responseEntity == null) {
                log.error("远程调用topology/ums/queryUser.action接口返回为null，异常失败设备id");
            } else if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
                log.error("远程调用topology/ums/queryUser.action接口返回为状态非200，检查异常失败设备");
            } else {
                JSONObject jsonObject = responseEntity.getBody();
                JSONObject objectJSONObject = jsonObject.getJSONObject("data");
                if (objectJSONObject != null) {
                    userInfo = JSONObject.parseObject(JSONObject.toJSONString(objectJSONObject), UserInfoDTO.class);

                }
            }
        } catch (Exception e) {
            log.error("远程调用topology/ums/queryUser.action接口异常", e);
        }
        if(userInfo == null){
            userInfo = new UserInfoDTO();
            userInfo.setBranchLevel("00");
        }
        return userInfo;
    }

    @Override
    public String likeBranch(String userName) {
        UserInfoDTO userInfoDTO = findOne(userName);
        String branchLevel = "00";
        if(userInfoDTO != null && StringUtils.isNotEmpty(userInfoDTO.getBranchLevel())){
            branchLevel = userInfoDTO.getBranchLevel() + "%";
        }else{
            branchLevel  = branchLevel +"%";
        }
        return branchLevel;
    }

    @Override
    public List<Branch> getBranchListByLevel(String level) {
        String path = topologyServerPrefix + "topology/branch/listBranchByLevel";
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.put("level", Collections.singletonList(level));
        List<Branch> branchList = null;
        ResponseEntity<JSONObject> responseEntity = oAuth2RestTemplate.postForEntity(path,param,JSONObject.class);
        if (responseEntity == null) {
            log.error("远程调用topology/branch/findBranchTree接口返回为null，异常失败设备id");
        } else if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
            log.error("远程调用topology/branch/findBranchTree接口返回为状态非200，检查异常失败设备");
        } else {
            JSONObject jsonObject = responseEntity.getBody();
            JSONObject  objectJSONObject = jsonObject.getJSONObject("data");
            if(objectJSONObject != null){
                JSONArray result =   objectJSONObject.getJSONArray("result");
                if(result != null && result.size()>0){
                    branchList =  result.toJavaList(Branch.class);
                }


            }
        }
        return branchList;
    }
}
