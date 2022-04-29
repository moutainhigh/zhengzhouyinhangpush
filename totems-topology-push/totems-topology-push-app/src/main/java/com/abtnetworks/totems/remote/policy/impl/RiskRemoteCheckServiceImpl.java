package com.abtnetworks.totems.remote.policy.impl;

import com.abtnetworks.totems.recommend.vo.ComplianceRulesMatrixVO;
import com.abtnetworks.totems.remote.dto.DredgeVerifyComplianceDTO;
import com.abtnetworks.totems.remote.policy.RiskRemoteCheckService;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class RiskRemoteCheckServiceImpl implements RiskRemoteCheckService {

    /**
     * rest
     **/
    @Value("${topology.risk-prefix}")
    private String riskPrefix;

    /**
     * 远程调用
     ***/

    @Qualifier("clientCredentialsOAuth2RestTemplate")
    @Autowired
    OAuth2RestTemplate oAuth2RestTemplate;

    @Override
    public List<ComplianceRulesMatrixVO> remoteRiskVerifyCompliance(DredgeVerifyComplianceDTO dredgeVerifyComplianceDTO) {
        String path = riskPrefix + "api/compliance/verify/dredge";
        MultiValueMap<String, DredgeVerifyComplianceDTO> param = new LinkedMultiValueMap<>();
        param.put("dredgeVerifyComplianceDTO", Collections.singletonList(dredgeVerifyComplianceDTO));

        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        List<ComplianceRulesMatrixVO> complianceRulesMatrixVOS = new ArrayList<>();
        try {
            ResponseEntity<JSONObject> responseEntity = oAuth2RestTemplate.postForEntity(path, dredgeVerifyComplianceDTO, JSONObject.class);
            if (responseEntity == null) {
                log.error("远程调用risk中api/compliance/verify/dredge接口返回为null");
            }else if ((HttpStatus.SC_OK != responseEntity.getStatusCodeValue())) {
                log.error("远程调用risk中api/compliance/verify/dredge接口返回为状态非200");
            } else {
                jsonObject = responseEntity.getBody();
                if (jsonObject != null) {
                    jsonArray = jsonObject.getJSONArray("data");
                    complianceRulesMatrixVOS = jsonArray.toJavaList(ComplianceRulesMatrixVO.class);
                }
            }
            return complianceRulesMatrixVOS;
        }catch (Exception e){
            log.error("远程调用api/compliance/verify/dredge失败",e);
        }
        return complianceRulesMatrixVOS;
    }
}
