package com.abtnetworks.totems.hit.controller;

import com.abtnetworks.totems.commandLine.vo.SecurityPolicyParamVO;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.hit.service.PolicyHitService;
import com.abtnetworks.totems.push.controller.PushController;
import com.abtnetworks.totems.push.vo.NewPolicyPushVO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author: wangxinghui
 * @Date: 2022/1/22 14:17
 */
@Api(tags="命中收敛")
@RestController
@RequestMapping(value="/hit/")
public class PolicyHitController extends BaseController {

    private static Logger logger = Logger.getLogger(PolicyHitController.class);

    @Autowired
    private PolicyHitService policyHitService;

    @ApiOperation(value = "删除策略", httpMethod = "POST", notes = "根据策略信息删除生成策略命令行", nickname = "")
    @RequestMapping(value = "/delete-policy-push", method = RequestMethod.POST)
    public JSONObject deletePolicyPush(@ApiParam(name = "PolicyDetailVO", value = "删除策略", required = true) @RequestBody DeviceFilterRuleListRO vo, Authentication auth) throws Exception {
        TotemsReturnT<Map<String,Object>> result = policyHitService.generate(vo);
        JSONObject rs = new JSONObject();
        rs.put("msg",result.getMsg());
        rs.put("code",result.getCode());
        rs.put("data",result.getData());
        return rs;
    }

}
