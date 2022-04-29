package com.abtnetworks.totems.recommend.manager.impl;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/15 11:48
 */

import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.recommend.annotation.TimeCounter;
import com.abtnetworks.totems.recommend.dto.risk.PolicyRecommendRiskDTO;
import com.abtnetworks.totems.recommend.dto.risk.PolicyRecommendRiskResultDTO;
import com.abtnetworks.totems.recommend.manager.ExternalManager;
import com.abtnetworks.totems.whale.policy.dto.PathAnalyzeDTO;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalManagerImpl implements ExternalManager {

    private static Logger logger = LoggerFactory.getLogger(ExternalManagerImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Qualifier("clientCredentialsOAuth2RestTemplate")
    @Autowired
    private OAuth2RestTemplate clientCredentialsOAuth2RestTemplate;

    @Value("${topology.push-server-prefix}")
    private String recommendPrefix;

    @Value("${topology.topology-server-prefix}")
    private String topologyPrefix;

    @Value("${topology.layer-server-prefix}")
    private String layerPrefix;

    @Value("${topology.risk-server-prefix}")
    private String riskPrefix ;
    /**KSH-4774 仿真无路径时默认返回无路径的信息。**/
    @Value("${noPathDisplay:false}")
    private Boolean noPathDisplay=false;

    @Override
    public int doGather(String gatherId) {
        logger.info(String.format("开始采集设备(%s)...", gatherId));
        String url = topologyPrefix + "topology/node/doGather.action?ids=" + gatherId;

        String result = clientCredentialsOAuth2RestTemplate.getForObject(url,String.class);
        logger.info("result:"+result);
        //HttpClientUtils.httpGet(url);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    @TimeCounter
    public String getDetailPath(PathAnalyzeDTO pathAnalyzeDTO) throws Exception {
        logger.debug("获取详细路径信息...");
        String url = layerPrefix + "topology-layer/whale/GET/detailedPath/run";
        //KSH-4774 仿真无路径时默认返回无路径的信息。
        if(!noPathDisplay) {
            url = url + "/true";
        }
        logger.info("路径查询URL为"+ url);
//        JSONObject detailedPath = HttpClientUtils.httpPost(url, JSONObject.toJSONString(pathAnalyzeDTO));
        JSONObject postData  = JSONObject.parseObject(JSONObject.toJSONString(pathAnalyzeDTO));
        JSONObject detailedPath = null;
        logger.info("查询数据流输入参数{}",postData);
        detailedPath = clientCredentialsOAuth2RestTemplate.postForEntity(url, postData, JSONObject.class).getBody();
        logger.info("查询数据流输出参数{}",detailedPath);
        if(detailedPath == null) {
            logger.error("获取路径详细信息数据为空！");
            return "";
        }
        return detailedPath.toString();
    }

    @Override
    public PolicyRecommendRiskResultDTO checkRisk(PolicyRecommendRiskDTO checkRiskDTO) {
        PolicyRecommendRiskResultDTO result = null;

        String url = riskPrefix + "risk/api/alarm/rule/checkPushRisk";

        MultiValueMap<String, Object> parameter = new LinkedMultiValueMap<>();
        parameter.add("taskId",checkRiskDTO.getTaskId());
        parameter.add("srcZoneUuid",checkRiskDTO.getSrcZoneUuid());
        parameter.add("dstZoneUuid",checkRiskDTO.getDstZoneUuid());
        parameter.add("data", checkRiskDTO.getData());

        JSONObject jsonObject = clientCredentialsOAuth2RestTemplate.postForObject(url,parameter,JSONObject.class);

        result = jsonObject.toJavaObject(PolicyRecommendRiskResultDTO.class);
        return result;
    }
}
