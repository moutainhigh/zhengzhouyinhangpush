package com.abtnetworks.totems.disposal.controller;

import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.utils.DateUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.disposal.CommonConfigParam;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalOrderDTO;
import com.abtnetworks.totems.disposal.dto.DisposalRollbackOrderDTO;
import com.abtnetworks.totems.disposal.entity.DisposalRollbackEntity;
import com.abtnetworks.totems.disposal.entity.DisposalTeamBranchEntity;
import com.abtnetworks.totems.disposal.request.RollbackSaveQueryRequest;
import com.abtnetworks.totems.disposal.service.*;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.abtnetworks.totems.disposal.BaseController;

import java.util.*;

/**
 * @Author hw
 * @Description
 * @Date 17:23 2019/11/15
 */
@Api(value = "应急处置>>回滚工单")
@RestController
@RequestMapping(value = "${startPath}/disposal/rollback")
public class DisposalRollbackController extends BaseController {

    @Autowired
    private DisposalRollbackService disposalRollbackService;

    @Autowired
    private DisposalOrderService disposalOrderService;

    @Autowired
    private DisposalTeamBranchService disposalTeamBranchService;

    @Autowired
    private DisposalDeleteCommandLineRecordService deleteCommandLineRecordService;



    @Autowired
    private DisposalOrderScenesService disposalOrderScenesService;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 新增
     */
    @ApiOperation("执行回滚，传参为JSON格式")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "type", value = "工单类型：1手动、2黑IP、3路径", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "category", value = "分类：策略，路由", required = false, dataType = "Integer")
    })
    @PostMapping("/save")
    public ReturnT save(Authentication authentication, @RequestBody RollbackSaveQueryRequest queryRequest){
        try {
            // valid
            if (queryRequest == null || queryRequest.getpCenterUuidList() == null || queryRequest.getpCenterUuidList().size() == 0) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
            }

            List<String> errorList = new ArrayList<>();
            for (String pCenterUuid : queryRequest.getpCenterUuidList()) {
                //查询是否有回滚记录
                DisposalRollbackEntity resultRollbackEntity = disposalRollbackService.getRollbackEntity(pCenterUuid, pCenterUuid);
                if (resultRollbackEntity != null) {
                    DisposalOrderDTO orderDTO = disposalOrderService.getByCenterUuid(pCenterUuid);
                    errorList.add(orderDTO.getOrderNo()+" 已执行回滚！");
                } else {
                    DisposalRollbackEntity disposalRollback = new DisposalRollbackEntity();
                    disposalRollback.setPCenterUuid(pCenterUuid);
                    disposalRollback.setCenterUuid(pCenterUuid);
                    disposalRollback.setCreateUser(authentication.getName());
                    ReturnT<String> returnT = disposalRollbackService.insertBySelectOrder(disposalRollback);
                    if (returnT.getCode() == ReturnT.SUCCESS_CODE && ReturnT.STR_MSG_SUCCESS.equals(returnT.getMsg())) {
                        List<DisposalTeamBranchEntity> teamBranchEntityList = disposalTeamBranchService.findByCenterUuid(disposalRollback.getPCenterUuid());
                        //判断是否有下级单位
                        if (teamBranchEntityList != null && teamBranchEntityList.size() > 0) {
                            //开始kafka下发到下级单位
                            DisposalOrderDTO orderDTO = disposalOrderService.getByCenterUuid(disposalRollback.getPCenterUuid());
                            orderDTO.setCallbackFlag(true);
                            kafkaTemplate.send(CommonConfigParam.STR_TOPIC_ASSIGN_BRANCH, orderDTO);
                            logger.info("回滚工单号:"+orderDTO.getOrderNo()+" 自动派发到下级单位");
                        }

                        //数据流id
                        String streamId = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);
                        //执行回滚命令
                        disposalRollbackService.startSendDeleteCommandTasks(streamId, disposalRollback.getPCenterUuid(), authentication.getName());
                    } else {
                        DisposalOrderDTO orderDTO = disposalOrderService.getByCenterUuid(pCenterUuid);
                        errorList.add(orderDTO.getOrderNo()+" 回滚失败！");
                    }
                }
            }
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                    authentication.getName()+"执行封堵工单回滚");
            return new ReturnT(errorList);
        } catch (Exception e) {
            return ReturnT.FAIL;
        }
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public ReturnT<String> delete(int id){
        return disposalRollbackService.delete(id);
    }

    /**
     * 查询 get Dto By centerUuid
     */
    @ApiOperation("查询封堵工单内容")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "centerUuid", value = "工单内容UUID", required = true, dataType = "String")
    })
    @PostMapping("/getByCenterUuid")
    public ReturnT getByCenterUuid(String centerUuid){
        try {
            DisposalRollbackOrderDTO rollbackOrderDTO = disposalRollbackService.getByCenterUuid(centerUuid);
            return new ReturnT(rollbackOrderDTO);
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 分页查询
     */
    @ApiOperation("回滚工单List列表，传参JSON格式")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "type", value = "工单类型：1手动、2黑IP、3路径", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "category", value = "分类：策略，路由", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "page", value = "页数", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "limit", value = "每页条数", required = false, dataType = "Integer")
    })
    @PostMapping("/pageList")
    public ReturnT pageList(@RequestBody DisposalRollbackOrderDTO rollbackOrderDTO) {
        ReturnT returnT;
        try {
            PageInfo<DisposalRollbackOrderDTO> pageInfoList = disposalRollbackService.findDtoList(rollbackOrderDTO,
                    rollbackOrderDTO.getPage(), rollbackOrderDTO.getLimit());
            returnT = new ReturnT(pageInfoList);
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }

        return returnT;
    }

}

