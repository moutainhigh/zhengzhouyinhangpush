package com.abtnetworks.totems.disposal.controller;

import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.disposal.CommonConfigParam;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalOrderDTO;
import com.abtnetworks.totems.disposal.dto.DisposalTeamBranchDTO;
import com.abtnetworks.totems.disposal.entity.DisposalBranchEntity;
import com.abtnetworks.totems.disposal.entity.DisposalOrderCenterEntity;
import com.abtnetworks.totems.disposal.entity.DisposalTeamBranchEntity;
import com.abtnetworks.totems.disposal.service.DisposalBranchService;
import com.abtnetworks.totems.disposal.service.DisposalOrderCenterService;
import com.abtnetworks.totems.disposal.service.DisposalOrderService;
import com.abtnetworks.totems.disposal.service.DisposalTeamBranchService;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.abtnetworks.totems.disposal.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author hw
 * @Description
 * @Date 10:10 2019/11/12
 */
@Api(tags = {"应急处置>>封堵协作单位"})
@RestController
@RequestMapping(value = "${startPath}/disposal/teamBranch")
public class DisposalTeamBranchController extends BaseController {

    @Autowired
    private DisposalTeamBranchService disposalTeamBranchService;

    @Autowired
    private DisposalBranchService disposalBranchService;

    @Autowired
    private DisposalOrderService disposalOrderService;

    @Autowired
    private DisposalOrderCenterService disposalOrderCenterService;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 新增保存
     */
    @ApiOperation("封堵协作单位，指派下级单位，传参为JSON格式")
    @PostMapping("/save")
    public ReturnT<String> save(Authentication authentication, @RequestBody DisposalTeamBranchDTO teamBranchDTO){
        try {
            if (teamBranchDTO == null || AliStringUtils.isEmpty(teamBranchDTO.getCenterUuid()) || teamBranchDTO.getNeedAuditFlag() == null
                || teamBranchDTO.getBranchUuidArray() == null || teamBranchDTO.getBranchUuidArray().length == 0) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失！");
            }

            DisposalOrderCenterEntity orderCenterEntity = disposalOrderCenterService.getByUuid(teamBranchDTO.getCenterUuid());
            if (orderCenterEntity == null) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "无效工单！");
            }
            /**
             * category 分类 0:策略，1:路由
             * type 工单类型：1手动、2黑IP、3路径
             */
            if (orderCenterEntity.getType() == 1 && teamBranchDTO.getNeedAuditFlag() == false) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "工单号："+orderCenterEntity.getOrderNo()+"，为场景封堵工单，需要下级单位审核！");
            }

            List<DisposalTeamBranchEntity> list = new ArrayList<>();
            List<DisposalBranchEntity> branchEntityList = disposalBranchService.findByUUIDs(teamBranchDTO.getBranchUuidArray());
            for (DisposalBranchEntity branchEntity : branchEntityList) {
                DisposalTeamBranchEntity teamBranchEntity = new DisposalTeamBranchEntity();
                teamBranchEntity.setNeedAuditFlag(teamBranchDTO.getNeedAuditFlag());
                teamBranchEntity.setBranchUuid(branchEntity.getUuid());
                teamBranchEntity.setBranchCode(branchEntity.getCode());
                teamBranchEntity.setBranchName(branchEntity.getName());
                teamBranchEntity.setBranchIp(branchEntity.getIp());
                teamBranchEntity.setBranchRemarks(branchEntity.getRemarks());
                teamBranchEntity.setCenterUuid(teamBranchDTO.getCenterUuid());
                teamBranchEntity.setHandleStatus(0);
                teamBranchEntity.setCallbackHandleStatus(0);
                list.add(teamBranchEntity);
            }
            if (list.size() > 0) {
                //指派下级单位入库保存
                ReturnT<String> returnT = disposalTeamBranchService.batchSave(teamBranchDTO.getCenterUuid(), list);
                if (returnT.getCode() == ReturnT.SUCCESS_CODE && ReturnT.STR_MSG_SUCCESS.equals(returnT.getMsg())) {
                    //true：需要审核，false：无需审核自动
                    if (teamBranchDTO.getNeedAuditFlag()) {
                        disposalOrderCenterService.updateSendTypeByUuid(teamBranchDTO.getCenterUuid(), 0);
                    } else {
                        disposalOrderCenterService.updateSendTypeByUuid(teamBranchDTO.getCenterUuid(), 1);
                    }
                    //保存成功，开始kafka下发到下级单位
                    DisposalOrderDTO orderDTO = disposalOrderService.getByCenterUuid(teamBranchDTO.getCenterUuid());
                    orderDTO.setCallbackFlag(false);
                    kafkaTemplate.send(CommonConfigParam.STR_TOPIC_ASSIGN_BRANCH, orderDTO);

                    String infoMsg = "";
                    if (branchEntityList != null && branchEntityList.size() > 0) {
                        infoMsg = JSONObject.toJSONString(branchEntityList);
                    }
                    logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                            authentication.getName()+"工单号："+orderDTO.getOrderNo()+"，指派下级单位:"+infoMsg);
                    return ReturnT.SUCCESS;
                } else {
                    return new ReturnT<String>(ReturnT.FAIL_CODE, "指派下级单位失败！");
                }
            } else {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "关联下级单位list无数据！");
            }
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public ReturnT<String> delete(int id){
        return disposalTeamBranchService.delete(id);
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    public ReturnT<String> update(DisposalTeamBranchEntity disposalTeamBranch){
        return disposalTeamBranchService.update(disposalTeamBranch);
    }

    /**
     * 查询 get By Id
     */
    @PostMapping("/getById")
    public DisposalTeamBranchEntity getById(int id){
        return disposalTeamBranchService.getById(id);
    }

    @ApiOperation("查看派发的下级协作单位")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "centerUuid", value = "工单内容UUID", required = false, dataType = "String")
    })
    @PostMapping("findByCenterUuid")
    public ReturnT findByCenterUuid(String centerUuid) {
        try {
            if (AliStringUtils.isEmpty(centerUuid)) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失！");
            }
            List<DisposalTeamBranchEntity> list = disposalTeamBranchService.findByCenterUuid(centerUuid);
            return new ReturnT(list);
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 分页查询
     */
    @PostMapping("/pageList")
    public ReturnT pageList(DisposalTeamBranchEntity disposalTeamBranch) {
        ReturnT returnT;
        try {
            PageInfo<DisposalTeamBranchEntity> pageInfoList = disposalTeamBranchService.findList(disposalTeamBranch, disposalTeamBranch.getPage(), disposalTeamBranch.getLimit());
            returnT = new ReturnT(pageInfoList);
        } catch (Exception e) {
            return ReturnT.FAIL;
        }
        return returnT;
    }

}

