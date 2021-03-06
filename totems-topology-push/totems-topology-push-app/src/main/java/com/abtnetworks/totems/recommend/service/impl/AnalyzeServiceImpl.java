package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.manager.ExternalManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.AnalyzeService;
import com.abtnetworks.totems.whale.policy.dto.PathAnalyzeDTO;
import com.abtnetworks.totems.whale.policy.ro.PathAnalyzeDataRO;
import com.abtnetworks.totems.whale.policy.ro.PathAnalyzeRO;
import com.abtnetworks.totems.whale.policy.ro.PathDetailRO;
import com.abtnetworks.totems.whale.policy.ro.PathFlowRO;
import com.abtnetworks.totems.whale.policy.ro.PathInfoRO;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.abtnetworks.totems.common.constants.PolicyConstants.BIG_INTERNET_RECOMMEND;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/13 17:13
 */
@Service
public class AnalyzeServiceImpl implements AnalyzeService {
    private static Logger logger = LoggerFactory.getLogger(AnalyzeServiceImpl.class);

    @Autowired
    private RecommendTaskManager recommendTaskManager;

    @Autowired
    private WhaleManager whaleManager;

    @Autowired
    private ExternalManager externalManager;

    @Value("${push.whale:false}")
    private Boolean isNginZ;

    @Value("${noPathDisplay:false}")
    private Boolean noPathDisplay=false;

    private final static String  SAME_SUBNET ="????????????????????????";

    @Override
    public int analyzePathByPathInfo(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO) {
        int pathId = task.getId();
        int taskId = task.getTaskId();
        String theme = task.getTheme();
        logger.info(String.format("??????(%d)[%s]??????%d??????????????????...", taskId, theme, pathId));

        //????????????????????????
        PathAnalyzeDTO pathAnalyzeDTO = null;
        if (isNginZ == null || !isNginZ) {
            pathAnalyzeDTO = whaleManager.getAnylizePathDTO(task.getSrcIp(), task.getDstIp(), task.getServiceList(),
                    task.getSrcNodeUuid(), task.getDstNodeUuid(), task.getWhatIfCaseUuid(), task.getIdleTimeout());
        }else{
            // ????????????????????????
            pathAnalyzeDTO = whaleManager.getAnylizePathDTOByQT(task,simulationTaskDTO);
        }
        logger.info("pathAnalyzeDTO is \n" + JSONObject.toJSONString(pathAnalyzeDTO));

        String detailedPathString = null;
        try {
            detailedPathString = externalManager.getDetailPath(pathAnalyzeDTO);
        } catch(Exception e) {
            logger.error(String.format("??????(%d)[%s]??????%d????????????????????????...", taskId, theme, pathId), e);
            return saveStatus(taskId, theme, pathId, ReturnCode.FAILED,noPathDisplay);
        }
        if(StringUtils.isBlank(detailedPathString)){
            //??????????????????????????????????????????
            logger.error(String.format("??????(%d)[%s]??????%d????????????????????????", taskId, theme, pathId));
            return saveStatus(taskId, theme, pathId, ReturnCode.FAILED,noPathDisplay);
        }
        JSONObject detailPathObject = JSONObject.parseObject(detailedPathString);
        logger.info(String.format("??????(%d)[%s]??????%d???????????????:\n", taskId, theme, pathId) + JSONObject.toJSONString(detailPathObject, false));

        //??????????????????, MySQL??????
        recommendTaskManager.saveAnalyzeDetailPath(pathId, detailedPathString);

        //?????????????????????????????????
        PathAnalyzeRO pathAnalyzeRO = detailPathObject.toJavaObject(PathAnalyzeRO.class);

        //??????????????????????????????????????????????????????????????????
        task.setPathAnalyzeRO(pathAnalyzeRO);

        //??????????????????
        int rc = checkPathStatus(pathAnalyzeRO,simulationTaskDTO);

        return saveStatus(taskId, theme, pathId, rc,noPathDisplay);
    }

    /**
     * ??????????????????
     * @param pathAnalyzeVO ????????????????????????
     * @param simulationTaskDTO ????????????
     * @return ???????????????
     */
    private int checkPathStatus(PathAnalyzeRO pathAnalyzeVO, SimulationTaskDTO simulationTaskDTO) {
        logger.debug("??????????????????..." + JSONObject.toJSONString(pathAnalyzeVO));
        List<PathAnalyzeDataRO> list = pathAnalyzeVO.getData();
        if(list == null ) {
            return ReturnCode.PATH_NO_DATA;
        }

        if (list.size() <= 0) {
            return ReturnCode.PATH_NO_ACCESS;
        }
        int taskType = simulationTaskDTO.getTaskType();
        PathAnalyzeDataRO dataVO = list.get(0);
        List<PathInfoRO> pathList = dataVO.getPathList();
        // ???????????????ip??????ip????????????????????????
        if (CollectionUtils.isEmpty(pathList) && StringUtils.isNotBlank(pathAnalyzeVO.getMessage()) && pathAnalyzeVO.getMessage().contains(SAME_SUBNET)) {
            return ReturnCode.SRC_DST_FROM_SAME_SUBNET;
        }
        boolean fullAccess = true;
        boolean allNoAccess = true;
        boolean longlinkDeny = false;
        for(PathInfoRO path: pathList) {
            if(taskType  == BIG_INTERNET_RECOMMEND){
                List<PathFlowRO> pathFlowROS = path.getEndFlow();
                if(CollectionUtils.isNotEmpty(pathFlowROS)){
                    //?????????????????????????????????????????????nat
                    logger.info("?????????????????????????????????????????????nat{}",JSONObject.toJSONString(pathFlowROS));
                    return ReturnCode.BIG_INTERNET_NAT_ERROR;
                }
            }

            if (path.getPathStatus().equals("FULLY_CLOSED") && path.getPathType().equals("NO_PATH")) {
                logger.debug("????????????");
                if (StringUtils.isNotBlank(pathAnalyzeVO.getMessage()) && pathAnalyzeVO.getMessage().contains(SAME_SUBNET)) {
                } else {
                    fullAccess = false;
                }
            } else if (path.getPathStatus().equals("FULLY_OPEN") && path.getPathType().equals("ACCESS_PATH")) {
                logger.debug("???????????????");

                //?????????????????????longLinkStatus???????????????????????????????????????????????????FULLY_OPEN????????????????????????????????????????????????????????????
                List<PathDetailRO> deviceDetailList = path.getDeviceDetails();
                if(deviceDetailList != null && deviceDetailList.size() > 0) {
                    for (PathDetailRO pathDetailRO : deviceDetailList) {
                        String longLinkStatus = pathDetailRO.getLongLinkPathStatus();
                        if (longLinkStatus != null && !longLinkStatus.equals("FULLY_OPEN")) {
                            longlinkDeny = true;
                        }
                    }
                } else {
                    logger.info("Device List is empty!");
                }

                allNoAccess = false;
            } else {
                allNoAccess = false;
                fullAccess = false;
            }
        }

        if (allNoAccess) {
            if (StringUtils.isNotBlank(pathAnalyzeVO.getMessage()) && pathAnalyzeVO.getMessage().contains(SAME_SUBNET)) {
                return ReturnCode.SRC_DST_FROM_SAME_SUBNET;
            } else {
                return ReturnCode.PATH_NO_ACCESS;
            }
        }

        if (fullAccess) {
            if (longlinkDeny) {
                return ReturnCode.LONG_LINK_DENY;
            } else {
                simulationTaskDTO.setPathAnalyzeStatus(String.valueOf(PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FULL_ACCESS));
                return ReturnCode.PATH_FULL_ACCESS;
            }
        }

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * ????????????????????????
     * @param taskId ??????id
     * @param theme ???????????????????????????
     * @param pathId ??????id
     * @param rc ??????????????????
     * @param noPathDisplay ?????????????????????
     * @return ??????????????????
     */
    private int saveStatus(int taskId, String theme, int pathId, int rc,boolean noPathDisplay) {
        if (rc == ReturnCode.PATH_NO_ACCESS) {
            logger.info(String.format("??????(%d)[%s]??????%d?????????????????????,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_NO_ACCESS);
            if (noPathDisplay) {
                return ReturnCode.POLICY_MSG_OK;
            } else {
                return ReturnCode.FAILED;
            }
        } else if (rc == ReturnCode.PATH_NO_DATA) {
            logger.info(String.format("??????(%d)[%s]??????%d?????????????????????,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_ERROR);
            return ReturnCode.FAILED;
        } else if(rc == ReturnCode.FAILED) {
            logger.info(String.format("??????(%d)[%s]??????%d??????????????????...", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_ERROR);
            return ReturnCode.FAILED;
        } else if(rc == ReturnCode.PATH_FULL_ACCESS) {
            logger.info(String.format("??????(%d)[%s]??????%d?????????????????????,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FULL_ACCESS);
            return ReturnCode.POLICY_MSG_OK;
        } else if(rc == ReturnCode.LONG_LINK_DENY) {
            logger.info(String.format("??????(%d)[%s]??????%d?????????????????????,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_LONG_LINK_DENY);
            return ReturnCode.POLICY_MSG_OK;
        }  else if(rc == ReturnCode.BIG_INTERNET_NAT_ERROR) {
            logger.info(String.format("??????(%d)[%s]??????%d?????????????????????,?????????????????????nat,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, ReturnCode.BIG_INTERNET_NAT_ERROR);
            if (noPathDisplay) {
                return ReturnCode.POLICY_MSG_OK;
            } else {
                return ReturnCode.FAILED;
            }
        } else if(rc == ReturnCode.SRC_DST_FROM_SAME_SUBNET){
            logger.info(String.format("??????(%d)[%s]??????%d?????????????????????,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_SRC_DST_HAS_SAME_SUBNET);
            if (noPathDisplay) {
                return ReturnCode.POLICY_MSG_OK;
            } else {
                return ReturnCode.FAILED;
            }
        } else {
            logger.info(String.format("??????(%d)[%s]??????%d?????????????????????, ?????????????????????", taskId, theme, pathId));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FINISHED);
            return ReturnCode.POLICY_MSG_OK;
        }
    }
}
