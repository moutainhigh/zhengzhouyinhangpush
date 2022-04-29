package com.abtnetworks.totems.push.controller;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.utils.DateUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.dto.ForbidCommandLineDTO;
import com.abtnetworks.totems.push.entity.PushForbidIpEntity;
import com.abtnetworks.totems.push.enums.PushForbidIpStatusEnum;
import com.abtnetworks.totems.push.request.ForbidSendCommandRequest;
import com.abtnetworks.totems.push.service.PushForbidCommandLineService;
import com.abtnetworks.totems.push.service.PushForbidIpService;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @desc    封禁IP 列表controller
 * @author liuchanghao
 * @date 2020-09-10 16:52
 */
@Api(tags = "封禁IP列表")
@RestController
@RequestMapping(value="/forbid")
public class PushForbidIpController extends BaseController {

    private static Logger logger = Logger.getLogger(PushForbidIpController.class);

    @Autowired
    private PushForbidIpService pushForbidIpService;

    @Autowired
    private PushForbidCommandLineService pushForbidCommandLineService;


    @Autowired
    protected LogClientSimple logClientSimple;


    @ApiOperation("新增或修改封禁IP")
    @PostMapping("/addOrUpdate")
    public JSONObject addOrUpdate(Authentication authentication, @RequestBody PushForbidIpEntity forbidIpEntity){
        try {
            if(forbidIpEntity == null ){
                return getReturnJSON(ReturnCode.FAILED, "必要参数缺失！");
            }
            if( authentication != null ){
                if(ObjectUtils.isEmpty(forbidIpEntity.getId())){
                    forbidIpEntity.setCreateUser(authentication.getName());
                } else {
                    forbidIpEntity.setUpdateUser(authentication.getName());
                }
            }
            forbidIpEntity.setCreateDate(new Date());
            forbidIpEntity = pushForbidIpService.addOrUpdate(forbidIpEntity);
            JSONObject rs = getReturnJSON(ReturnCode.POLICY_MSG_OK, String.valueOf(forbidIpEntity.getId()));
            rs.put("uuid", forbidIpEntity.getUuid());
            rs.put("id", forbidIpEntity.getId());
            return rs;
        } catch (Exception e) {
            logger.error("新增或修改封禁IP异常", e);
            return getReturnJSON(ReturnCode.FAILED, "新增或修改封禁IP异常");
        }
    }

    @ApiOperation("启用/禁用 封禁IP")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "id", value = "主键id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "enableStatus", value = "启用状态：启用：Y;禁用:N", required = true, dataType = "String"),
    })
    @PostMapping("/enable")
    public ReturnT<String> enable(@RequestBody PushForbidIpEntity forbidIpEntity, Authentication authentication){
        try {
            return pushForbidIpService.enable(forbidIpEntity.getId(), forbidIpEntity.getEnableStatus(),authentication.getName());
        } catch (Exception e) {
            logger.error("启用/禁用异常", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 分页查询
     */
    @ApiOperation("封禁IP列表，查询传参JSON格式")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "page", value = "页数",  dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "limit", value = "每页条数", dataType = "Integer")
    })
    @PostMapping("/pageList")
    public ReturnT pageList(@RequestBody PushForbidIpEntity forbidIpEntity) {
        try {
            PageInfo<PushForbidIpEntity> pageInfoList = pushForbidIpService.findList(forbidIpEntity, forbidIpEntity.getPage(), forbidIpEntity.getLimit());
            return new ReturnT(pageInfoList);
        } catch (Exception e) {
            logger.error("分页查询封禁列表异常", e);
            return ReturnT.FAIL;
        }

    }


    @ApiOperation("下发记录")
    @PostMapping("/push-record-list")
    public ReturnT pushRecordList(@ApiParam(name = "page", required = true, value = "当前页", defaultValue = "1") @RequestParam Integer page,
                                  @ApiParam(name = "psize", required = true, value = "每页显示记录条数", defaultValue = "20") @RequestParam Integer psize,
                                  @ApiParam(name = "forbidUuid", required = true, value = "封禁任务单uuid") @RequestParam String forbidUuid) {
        ReturnT returnT;
        try {
            PageInfo<ForbidCommandLineDTO> pageInfoList = pushForbidCommandLineService.findLastListByUuid(page, psize, forbidUuid);
            returnT = new ReturnT(pageInfoList);
        } catch (Exception e) {
            returnT = ReturnT.FAIL;
            logger.error("查询封禁IP的下发记录异常", e);
        }

        return returnT;
    }

    @ApiOperation("下发")
    @PostMapping("/send-forbid-command")
    public ReturnT sendForbidCommand(Authentication authentication, @RequestBody ForbidSendCommandRequest commandRequest) {
        try {

            if (commandRequest == null || commandRequest.getForbidUuidList() == null || commandRequest.getForbidUuidList().isEmpty()) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失：工单uuid");
            }

            //异常信息集合
            List<String> errorList = new ArrayList<>();

            //操作人姓名
            String userName = "";
            if (authentication != null) {
                userName = authentication.getName();
            }

            for (String uuid : commandRequest.getForbidUuidList()) {
                //查询工单详情
                PushForbidIpEntity forbidIpEntity = pushForbidIpService.getByUuid(uuid);
                if (forbidIpEntity == null) {
                    errorList.add("参数: " + uuid + " 错误，未找到工单 或 已删除！");
                    continue;
                }

                int status = forbidIpEntity.getStatus();
                if (status == PushForbidIpStatusEnum.PUSH_SUCCESS.getCode().intValue()) {
                    errorList.add(forbidIpEntity.getSerialNumber() + " 已执行完成！");
                    continue;
                }

                if (status != PushForbidIpStatusEnum.PRE_PUSH.getCode().intValue()
                        && status != PushForbidIpStatusEnum.UPDATED.getCode().intValue()
                        && status != PushForbidIpStatusEnum.ENABLE.getCode().intValue()
                        && status != PushForbidIpStatusEnum.DISABLE.getCode().intValue()) {
                    errorList.add(forbidIpEntity.getSerialNumber() + " 工单状态错误，无法进行下发！");
                    continue;
                }

                //数据流id
                String streamId = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+ IdGen.randomBase62(6);

                //开始执行下发
                pushForbidCommandLineService.startSendCommandTask(streamId, uuid, userName);

            }

            //添加操作日志
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), userName + "执行封禁工单批量下发");
            return new ReturnT(errorList);
        } catch (Exception e) {
            logger.error("封禁工单下发异常", e);
            return ReturnT.FAIL;
        }
    }

}
