package com.abtnetworks.totems.hit.service;

import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;

import java.util.Map;

public interface PolicyHitService {

    TotemsReturnT<Map<String,Object>> generate(DeviceFilterRuleListRO vo);

}
