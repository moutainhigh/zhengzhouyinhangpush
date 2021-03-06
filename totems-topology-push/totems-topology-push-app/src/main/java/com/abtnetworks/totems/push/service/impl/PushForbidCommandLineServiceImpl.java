package com.abtnetworks.totems.push.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.utils.DateUtil;
import com.abtnetworks.totems.common.utils.Encodes;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.disposal.dto.DisposalNodeCommandLineRecordDTO;
import com.abtnetworks.totems.disposal.dto.DisposalNodeCredentialDTO;
import com.abtnetworks.totems.disposal.service.DisposalOrderScenesService;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.send.SendCommandService;
import com.abtnetworks.totems.push.dao.mysql.PushForbidCommandLineMapper;
import com.abtnetworks.totems.push.dao.mysql.PushForbidIpMapper;
import com.abtnetworks.totems.push.dto.ForbidCommandLineDTO;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushForbidDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.push.entity.PushForbidCommandLineEntity;
import com.abtnetworks.totems.push.entity.PushForbidIpEntity;
import com.abtnetworks.totems.push.enums.PushForbidIpStatusEnum;
import com.abtnetworks.totems.push.enums.PushStatusEnum;
import com.abtnetworks.totems.push.service.PushForbidCommandLineService;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.github.dozermapper.core.Mapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executor;


@Slf4j
@Service
public class PushForbidCommandLineServiceImpl implements PushForbidCommandLineService {


    @Autowired
    private PushForbidCommandLineMapper pushForbidCommandLineMapper;

    @Autowired
    private PushForbidIpMapper pushForbidIpMapper;

    @Autowired
    private LogClientSimple logClientSimple;

    //??????????????????
    @Autowired
    private DisposalOrderScenesService disposalOrderScenesService;

    //????????????
    @Qualifier("forbidExecutor")
    @Autowired
    private Executor forbidExecutor;

    @Autowired
    private NodeMapper nodeDao;

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;


    @Autowired
    private Mapper dozerMapper;

    /**??????????????????**/
    @Autowired
    SendCommandService sendCommandService;


    @Override
    public PageInfo<ForbidCommandLineDTO> findLastListByUuid(Integer currentPage, Integer pageSize, String uuid) {
        PageHelper.startPage(currentPage, pageSize);

        List<ForbidCommandLineDTO> list = pushForbidCommandLineMapper.getLastListByUuid(uuid);
        if (list != null && !list.isEmpty()) {
            for (ForbidCommandLineDTO dto : list) {
                if(dto.getPushTime() != null ){
                    dto.setPushTimeStr(DateUtil.dateToString(dto.getPushTime(), DateUtil.timeStamp_STANDARD));
                }
                dto.setPushStatusDesc(PushStatusEnum.getDescByCode(dto.getPushStatus()));
            }
        }
        PageInfo<ForbidCommandLineDTO> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public void startSendCommandTask(String streamId, String uuid, String userName) {

        //????????????
        PushForbidIpEntity forbidIpEntity = pushForbidIpMapper.getByUuid(uuid);
        if (forbidIpEntity == null) {
            log.info("???????????????????????????????????????");
            return;
        }

        //?????????
        pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.PUSHING.getCode());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), userName + "????????????????????????" + forbidIpEntity.getSerialNumber() + " ????????????");

        //??????uuid?????????????????????????????????
        List<PushForbidCommandLineEntity> waitSendList = pushForbidCommandLineMapper.getWaitSendRecordByUuid(uuid);
        if (waitSendList == null || waitSendList.isEmpty()) {
            log.info("?????????????????????????????????????????????????????????????????????????????????");
            pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.PUSH_FAIL.getCode());
            updateStatus(waitSendList, "??????????????????");
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), userName + "????????????????????????" + forbidIpEntity.getSerialNumber() + " ??????????????????");
            return;
        }

        //??????????????????
        Set<String> deviceUuidSet = new HashSet<>();
        for (PushForbidCommandLineEntity lineEntity : waitSendList) {
            deviceUuidSet.add(lineEntity.getDeviceUuid());
        }

        String[] deviceUuidArray = deviceUuidSet.toArray(new String[0]);
        //???????????????????????????
        List<DisposalNodeCredentialDTO> nodeCredentialList = disposalOrderScenesService.findNodeCredentialDtoList(deviceUuidArray);
        if (nodeCredentialList == null || nodeCredentialList.isEmpty()) {
            pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.PUSH_FAIL.getCode());
            // ??????????????????????????????????????????
            updateStatus(waitSendList, userName + "????????????????????????" + forbidIpEntity.getSerialNumber() + " ????????????????????????ssh???????????????");
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), userName + "????????????????????????" + forbidIpEntity.getSerialNumber() + " ????????????????????????ssh???????????????");
            return;
        }

        //??????????????????
        Map<String, DisposalNodeCredentialDTO> credentialMap = new HashMap<>();
        for (DisposalNodeCredentialDTO dto : nodeCredentialList) {
            credentialMap.put(dto.getDeviceUuid(), dto);
        }

        forbidExecutor.execute(new ExtendedRunnable(new ExecutorDto(streamId, forbidIpEntity.getSerialNumber(), forbidIpEntity.getSerialNumber(), new Date())) {
            @Override
            protected void start() throws InterruptedException, Exception {

                Set<String> pushErrorDeviceSet = new HashSet<>();

                //??????IP??????
                int checkIpv4 = 0;
                if (InputValueUtils.checkIp(forbidIpEntity.getSrcIp()) != 0) {
                    checkIpv4 = 1;
                }

                log.info("????????????: " + forbidIpEntity.getSerialNumber() + " : ?????????????????????");
                for (PushForbidCommandLineEntity command : waitSendList) {
                    if (command == null) {
                        log.error("???????????????");
                        continue;
                    }

                    String deviceUuid = command.getDeviceUuid();
                    //????????????
                    NodeEntity node = nodeDao.getTheNodeByUuid(deviceUuid);
                    if (node == null) {
                        log.error("?????????????????????????????????");
                        continue;
                    }

                    DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUuid);
                    if (deviceRO == null || deviceRO.getData() == null || deviceRO.getData().isEmpty()) {
                        log.error("???mongodb?????????????????????!");
                        continue;
                    }
                    DeviceDataRO deviceDataRO = deviceRO.getData().get(0);

                    //??????????????????
                    Boolean isVsys = deviceDataRO.getIsVsys();
                    if (isVsys == null) {
                        isVsys = false;
                    }

                    //????????????????????????
                    DisposalNodeCredentialDTO nodeCredentialDTO = credentialMap.get(deviceUuid);
                    if (nodeCredentialDTO == null) {
                        pushErrorDeviceSet.add(deviceUuid);
                        log.error("????????????????????????, deviceUuid:" + deviceUuid);
                        continue;
                    }

                    //???????????????
                    PushCmdDTO cmdDTO = new PushCmdDTO();
                    dozerMapper.map(nodeCredentialDTO, cmdDTO);

                    cmdDTO.setPolicyFlag(command.getPolicyName());

                    //????????????????????????????????????????????????????????????????????????????????????
                    PushForbidDTO pushForbidDTO = new PushForbidDTO();
                    if(!command.getCommandType().equals(PolicyEnum.FORBID.getKey())){
                        pushForbidDTO.setIsAppendCommandLine(false);
                    }else{
                        pushForbidDTO.setIsAppendCommandLine(true);
                    }
                    cmdDTO.setPushForbidDTO(pushForbidDTO);

                    //?????????
                    cmdDTO.setCommandline(command.getCommandline());
                    cmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString(node.getModelNumber()));
                    //?????????????????????????????????192.168.215.32(2) ???(2)
                    if (cmdDTO.getDeviceManagerIp().contains("(")) {
                        String tmpIp = cmdDTO.getDeviceManagerIp().substring(0, cmdDTO.getDeviceManagerIp().indexOf("("));
                        cmdDTO.setDeviceManagerIp(tmpIp);
                    }

                    cmdDTO.setIsVSys(isVsys);
                    cmdDTO.setVSysName(deviceDataRO.getVsysName());
                    //??????????????????
                    cmdDTO.setPassword(Encodes.decodeBase64Key(cmdDTO.getPassword()));
                    cmdDTO.setEnablePassword(Encodes.decodeBase64Key(cmdDTO.getEnablePassword()));

                    //????????????
                    if (nodeCredentialDTO.getControllerId().contains("ssh")) {
                        cmdDTO.setExecutorType("ssh");
                    } else {
                        cmdDTO.setExecutorType("telnet");
                    }
                    log.info("??????????????????" + cmdDTO.getExecutorType());

                    //??????????????????
                    cmdDTO.setCharset(node.getCharset());
                    PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(cmdDTO);

                    //???????????????
                    if (pushResultDTO.getResult() != ReturnCode.POLICY_MSG_OK) {
                        pushErrorDeviceSet.add(deviceUuid);
                        StringBuffer stringBuffer = new StringBuffer(pushResultDTO.getCmdEcho()).append(SendCommandStaticAndConstants.LINE_BREAK).append(pushResultDTO.getSendErrorEnum().getMessage());
                        command.setCommandlineEcho(stringBuffer.toString());
                        command.setPushStatus(PushStatusEnum.PUSH_FAILED.getCode());
                    } else {
                        command.setCommandlineEcho(pushResultDTO.getCmdEcho());
                        command.setPushStatus(PushStatusEnum.PUSH_STATUS_ENUM.getCode());
                    }

                    command.setPushTime(new Date());
                    //??????????????????
                    pushForbidCommandLineMapper.updateByPrimaryKeySelective(command);

                }

                //?????????????????????????????????????????????
                if (pushErrorDeviceSet.size() > 0) {
                    pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.PUSH_FAIL.getCode());
                    logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), userName + "????????????????????????" + forbidIpEntity.getSerialNumber() + ">>????????????");
                } else {
                    pushForbidIpMapper.updateStatusByUuid(uuid, PushForbidIpStatusEnum.PUSH_SUCCESS.getCode());
                    logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), userName + "????????????????????????" + forbidIpEntity.getSerialNumber() + ">>????????????");
                }
            }
        });
    }

    /**
     * ??????????????????????????????????????????
     * @param waitSendList
     * @param msg
     */
    private void updateStatus(List<PushForbidCommandLineEntity> waitSendList, String msg){
        // ??????????????????????????????????????????
        for (PushForbidCommandLineEntity lineEntity : waitSendList) {
            lineEntity.setPushTime(new Date());
            lineEntity.setPushStatus(PushStatusEnum.PUSH_FAILED.getCode());
            lineEntity.setCommandlineEcho(msg);
            pushForbidCommandLineMapper.updateByPrimaryKeySelective(lineEntity);
        }
    }
}
