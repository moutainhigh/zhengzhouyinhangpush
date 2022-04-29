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

    private final static String  SAME_SUBNET ="来源于同一个子网";

    @Override
    public int analyzePathByPathInfo(PathInfoTaskDTO task, SimulationTaskDTO simulationTaskDTO) {
        int pathId = task.getId();
        int taskId = task.getTaskId();
        String theme = task.getTheme();
        logger.info(String.format("任务(%d)[%s]路径%d开始路径分析...", taskId, theme, pathId));

        //组装路径查询对象
        PathAnalyzeDTO pathAnalyzeDTO = null;
        if (isNginZ == null || !isNginZ) {
            pathAnalyzeDTO = whaleManager.getAnylizePathDTO(task.getSrcIp(), task.getDstIp(), task.getServiceList(),
                    task.getSrcNodeUuid(), task.getDstNodeUuid(), task.getWhatIfCaseUuid(), task.getIdleTimeout());
        }else{
            // 青提组装查询对象
            pathAnalyzeDTO = whaleManager.getAnylizePathDTOByQT(task,simulationTaskDTO);
        }
        logger.info("pathAnalyzeDTO is \n" + JSONObject.toJSONString(pathAnalyzeDTO));

        String detailedPathString = null;
        try {
            detailedPathString = externalManager.getDetailPath(pathAnalyzeDTO);
        } catch(Exception e) {
            logger.error(String.format("任务(%d)[%s]路径%d获取路径详情异常...", taskId, theme, pathId), e);
            return saveStatus(taskId, theme, pathId, ReturnCode.FAILED,noPathDisplay);
        }
        if(StringUtils.isBlank(detailedPathString)){
            //如果路径返回为空，则仿真失败
            logger.error(String.format("任务(%d)[%s]路径%d获取路径详情为空", taskId, theme, pathId));
            return saveStatus(taskId, theme, pathId, ReturnCode.FAILED,noPathDisplay);
        }
        JSONObject detailPathObject = JSONObject.parseObject(detailedPathString);
        logger.info(String.format("任务(%d)[%s]路径%d分析数据为:\n", taskId, theme, pathId) + JSONObject.toJSONString(detailPathObject, false));

        //存储路径详情, MySQL调用
        recommendTaskManager.saveAnalyzeDetailPath(pathId, detailedPathString);

        //解析路径详情数据为对象
        PathAnalyzeRO pathAnalyzeRO = detailPathObject.toJavaObject(PathAnalyzeRO.class);

        //保存路径详情数据到对象中，供后续策略生成使用
        task.setPathAnalyzeRO(pathAnalyzeRO);

        //检查路径状态
        int rc = checkPathStatus(pathAnalyzeRO,simulationTaskDTO);

        return saveStatus(taskId, theme, pathId, rc,noPathDisplay);
    }

    /**
     * 检查路径状态
     * @param pathAnalyzeVO 查询路径结果数据
     * @param simulationTaskDTO 仿真数据
     * @return 分析结果值
     */
    private int checkPathStatus(PathAnalyzeRO pathAnalyzeVO, SimulationTaskDTO simulationTaskDTO) {
        logger.debug("检查路径状态..." + JSONObject.toJSONString(pathAnalyzeVO));
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
        // 如果是目的ip为单ip没有其他路径产生
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
                    //针对大网段开通，不能让他们经过nat
                    logger.info("针对大网段开通，不能让他们经过nat{}",JSONObject.toJSONString(pathFlowROS));
                    return ReturnCode.BIG_INTERNET_NAT_ERROR;
                }
            }

            if (path.getPathStatus().equals("FULLY_CLOSED") && path.getPathType().equals("NO_PATH")) {
                logger.debug("没有通路");
                if (StringUtils.isNotBlank(pathAnalyzeVO.getMessage()) && pathAnalyzeVO.getMessage().contains(SAME_SUBNET)) {
                } else {
                    fullAccess = false;
                }
            } else if (path.getPathStatus().equals("FULLY_OPEN") && path.getPathType().equals("ACCESS_PATH")) {
                logger.debug("通路已存在");

                //遍历所有的路的longLinkStatus字段，查看长连接的状态，若状态不为FULLY_OPEN即长连接不为通，即设置为长连接未放通状态
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
     * 保存路径分析状态
     * @param taskId 任务id
     * @param theme 任务主题（工单号）
     * @param pathId 路径id
     * @param rc 路径分析结果
     * @param noPathDisplay 无路径是否查路
     * @return 路径分析结果
     */
    private int saveStatus(int taskId, String theme, int pathId, int rc,boolean noPathDisplay) {
        if (rc == ReturnCode.PATH_NO_ACCESS) {
            logger.info(String.format("任务(%d)[%s]路径%d路径分析已完成,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_NO_ACCESS);
            if (noPathDisplay) {
                return ReturnCode.POLICY_MSG_OK;
            } else {
                return ReturnCode.FAILED;
            }
        } else if (rc == ReturnCode.PATH_NO_DATA) {
            logger.info(String.format("任务(%d)[%s]路径%d路径分析已完成,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_ERROR);
            return ReturnCode.FAILED;
        } else if(rc == ReturnCode.FAILED) {
            logger.info(String.format("任务(%d)[%s]路径%d路径分析异常...", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_ERROR);
            return ReturnCode.FAILED;
        } else if(rc == ReturnCode.PATH_FULL_ACCESS) {
            logger.info(String.format("任务(%d)[%s]路径%d路径分析已完成,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FULL_ACCESS);
            return ReturnCode.POLICY_MSG_OK;
        } else if(rc == ReturnCode.LONG_LINK_DENY) {
            logger.info(String.format("任务(%d)[%s]路径%d路径分析已完成,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_LONG_LINK_DENY);
            return ReturnCode.POLICY_MSG_OK;
        }  else if(rc == ReturnCode.BIG_INTERNET_NAT_ERROR) {
            logger.info(String.format("任务(%d)[%s]路径%d路径分析已完成,存在大网段经过nat,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, ReturnCode.BIG_INTERNET_NAT_ERROR);
            if (noPathDisplay) {
                return ReturnCode.POLICY_MSG_OK;
            } else {
                return ReturnCode.FAILED;
            }
        } else if(rc == ReturnCode.SRC_DST_FROM_SAME_SUBNET){
            logger.info(String.format("任务(%d)[%s]路径%d路径分析已完成,%s", taskId, theme, pathId, ReturnCode.getMsg(rc)));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_SRC_DST_HAS_SAME_SUBNET);
            if (noPathDisplay) {
                return ReturnCode.POLICY_MSG_OK;
            } else {
                return ReturnCode.FAILED;
            }
        } else {
            logger.info(String.format("任务(%d)[%s]路径%d路径分析已完成, 需要开通策略！", taskId, theme, pathId));
            recommendTaskManager.updatePathAnalyzeStatus(pathId, PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_FINISHED);
            return ReturnCode.POLICY_MSG_OK;
        }
    }
}
