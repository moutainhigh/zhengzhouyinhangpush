package com.abtnetworks.totems.auto.service;

import com.alibaba.fastjson.JSONArray;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/9/14
 */
public interface RemotePolicyService {

    /**
     * 远程调用/policy/rule-list-search这个接口数据
     * @param ipTerms
     * @param skipAny
     * @param deviceUuid
     * @param type
     * @return
     */
    JSONArray remotePolicyDetailByIpTerms(String ipTerms, Boolean skipAny, String deviceUuid, Integer type);


}
