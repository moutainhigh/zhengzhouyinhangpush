package com.abtnetworks.totems.generate.controller;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.commandline.AclPolicyDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.utils.EntityUtils;
import com.abtnetworks.totems.push.vo.NewPolicyPushVO;
import com.abtnetworks.totems.recommend.entity.CommandTaskEditableEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;

/**
 * @author Administrator
 * @Title:
 * @Description: 这个类是策略生成中没有的接口，提供对外api的特例生成功能
 * @date 2020/9/17
 */
@Api(value="命令行生成api扩展接口类")
@RestController
@RequestMapping(value="/recommend/api/")
public class PolicyGenerateApiController extends PolicyGenerateController {
    private static Logger logger = LoggerFactory.getLogger(PolicyGenerateApiController.class);
    @ApiOperation("ACL策略生成")
    @PostMapping("task/addAclPolicy")
    public JSONObject addAclPolicyPolicy(@RequestBody NewPolicyPushVO newPolicyPushVO, Authentication auth) {
        logger.info("添加ACL策略" + JSONObject.toJSONString(newPolicyPushVO));
        if(newPolicyPushVO == null) {
            return getReturnJSON(ReturnCode.EMPTY_PARAMETERS);
        }

        //检测设备是否存在
        String deviceUuid = newPolicyPushVO.getDeviceUuid();
        NodeEntity node = taskService.getTheNodeByUuid(deviceUuid);
        if(node == null) {
            return getReturnJSON(ReturnCode.DEVICE_NOT_EXIST);
        }

        //获取用户名
//        String userName = auth.getName();
        String userName = "admin";
        //格式化域信息
        newPolicyPushVO.setSrcZone(getZone(newPolicyPushVO.getSrcZone()));
        newPolicyPushVO.setDstZone(getZone(newPolicyPushVO.getDstZone()));

        //创建源acl附加信息对象
        Integer ipType = ObjectUtils.isNotEmpty(newPolicyPushVO.getIpType())?newPolicyPushVO.getIpType(): IPV4.getCode();


        RecommendTaskEntity recommendTaskEntity = EntityUtils.createRecommendTask(newPolicyPushVO.getTheme(), userName, newPolicyPushVO.getSrcIp(),
                newPolicyPushVO.getDstIp(), JSONObject.toJSONString(newPolicyPushVO.getServiceList()),     PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_ACL,
                PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED, null, null, null, null,null,ipType);

        //添加任务数据到策略下发任务列表
        addRecommendTask(recommendTaskEntity);

        //添加命令行生成任务到新表
        CommandTaskEditableEntity entity = createCommandTask(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_ACL,
                recommendTaskEntity.getId(), userName, newPolicyPushVO.getTheme(), newPolicyPushVO.getDeviceUuid());
        commandTaskManager.addCommandEditableEntityTask(entity);
        DeviceRO device = whaleManager.getDeviceByUuid(deviceUuid);
        DeviceDataRO deviceData = device.getData().get(0);
        boolean isVsys = false;
        String rootDeviceUuid = "";
        String vsysName = "";
        if(deviceData.getIsVsys() != null) {
            isVsys = deviceData.getIsVsys();
            rootDeviceUuid = deviceData.getRootDeviceUuid();
            vsysName = deviceData.getVsysName();
        }
        CmdDTO cmdDTO = EntityUtils.createCmdDTO(PolicyEnum.ACL, entity.getId(), entity.getTaskId(), deviceUuid, newPolicyPushVO.getTheme(),
                userName, recommendTaskEntity.getSrcIp(), recommendTaskEntity.getDstIp(), null,
                null, newPolicyPushVO.getServiceList(), null, newPolicyPushVO.getSrcZone(),
                newPolicyPushVO.getDstZone(), newPolicyPushVO.getInDevIf(), newPolicyPushVO.getOutDevIf(), newPolicyPushVO.getDescription(),isVsys,vsysName,
                null,null,null, null);

        logger.info("命令行生成任务为:" + JSONObject.toJSONString(cmdDTO));
        cmdDTO.getTask().setTaskType(PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_ACL);
        pushTaskService.addGenerateCmdTask(cmdDTO);
        JSONObject rs = getReturnJSON(ReturnCode.POLICY_MSG_OK);
        rs.put("taskId",entity.getTaskId());
        rs.put("pushTaskId",entity.getId());
        return rs;
    }
}
