package com.abtnetworks.totems.remote.policy;

import com.alibaba.fastjson.JSONArray;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/21
 */
public interface PolicyRemoteCheckService {

    /***
     * 远程调用policy策略对象详情
     * @param deviceUuid
     * @param service
     * @param type
     * @return
     */
    JSONArray remotePolicyObjectDetail(String deviceUuid, String service, String type);
}
