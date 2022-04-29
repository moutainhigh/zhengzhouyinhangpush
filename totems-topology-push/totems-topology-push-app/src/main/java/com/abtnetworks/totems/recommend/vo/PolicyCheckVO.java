package com.abtnetworks.totems.recommend.vo;

import com.abtnetworks.totems.external.vo.PolicyCheckListVO;
import lombok.Data;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/21 6:28
 */
@Data
public class PolicyCheckVO {
    List<PolicyCheckListVO> hiddenPolicy;

    List<PolicyCheckListVO> mergePolicy;

    List<PolicyCheckListVO> redundancyPolicy;
}
