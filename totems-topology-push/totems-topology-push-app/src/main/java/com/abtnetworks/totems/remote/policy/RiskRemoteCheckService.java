package com.abtnetworks.totems.remote.policy;

import com.abtnetworks.totems.recommend.vo.ComplianceRulesMatrixVO;
import com.abtnetworks.totems.remote.dto.DredgeVerifyComplianceDTO;

import java.util.List;

public interface RiskRemoteCheckService {
    /***
     * 远程调用risk违规详情
     * @param dredgeVerifyComplianceDTO
     * @return
     */
    List<ComplianceRulesMatrixVO> remoteRiskVerifyCompliance(DredgeVerifyComplianceDTO dredgeVerifyComplianceDTO);
}
