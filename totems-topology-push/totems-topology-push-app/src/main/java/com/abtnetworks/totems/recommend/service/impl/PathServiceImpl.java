package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.IPTypeEnum;
import com.abtnetworks.totems.common.enums.RecommendTypeEnum;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedExecutor;
import com.abtnetworks.totems.common.executor.ExtendedLatchRunnable;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.common.utils.InputValueUtils;
import com.abtnetworks.totems.common.utils.ServiceDTOUtils;
import com.abtnetworks.totems.recommend.dto.task.ElapseTime;
import com.abtnetworks.totems.recommend.dto.task.PathInfoTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.entity.PathInfoEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.manager.nginz.PolicyBaseInfoManager;
import com.abtnetworks.totems.recommend.service.CommandSimulationCommonService;
import com.abtnetworks.totems.recommend.service.PathService;
import com.abtnetworks.totems.recommend.vo.SubnetSearchResultDTO;
import com.abtnetworks.totems.whale.baseapi.ro.SubnetRO;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.abtnetworks.totems.common.constants.PolicyConstants.POLICY_STR_NETWORK_TYPE_IP4;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV4;
import static com.abtnetworks.totems.common.enums.IpTypeEnum.IPV6;

@Service
public class PathServiceImpl implements PathService {
    private static Logger logger = LoggerFactory.getLogger(PathServiceImpl.class);

    private static final String STR_SEPERATOR = ",";

    private static final String EMPTY_STRING = "";

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    RecommendTaskManager recommendTaskManager;

    @Value("${push.internet.dnsIp}")
    private String internetDstIp;

    @Autowired
    PolicyBaseInfoManager policyBaseInfoManager;

    @Autowired
    @Qualifier(value = "pathExecutor")
    private Executor pushExecutor;

    @Autowired
    CommandSimulationCommonService commandSimulationCommonService;

    @Override
    public List<PathInfoTaskDTO> findPath(SimulationTaskDTO task) {
        logger.info(String.format("任务(%d)[%s]开始拆分策略开通任务为路径...", task.getId(), task.getTheme()));
        logger.debug("任务信息为：\n" + JSONObject.toJSONString(task));
        String srcIpStr = task.getSrcIp();
        String dstIpStr = task.getDstIp();
        ElapseTime findSubnetTime = new ElapseTime();

        //将具有多个子网以及没有子网的IP地址合并到一对起始地址中
        StringBuilder srcInvalidIpSb = new StringBuilder();
        Map<String, String> srcIpSubnetMap = new HashMap<>();

        //对于每个IP都进行子网查找，对于存在相同子网的IP则合并为一个IP组，对于不存在子网或者存在多个子网的IP者加入INVALID组
        String[] srcIps = srcIpStr.split(STR_SEPERATOR);
        for (String srcIp : srcIps) {
            Long start = new Date().getTime();
            List<String> uuidList = whaleManager.getSubnetUuidList(srcIp);
            Long end = new Date().getTime();
            findSubnetTime.setMaxTime(end - start);
            findSubnetTime.setMinTime(end - start);
            findSubnetTime.setTotal(end - start);
            findSubnetTime.setCount();

            //2020-07-03 修bug=4358，若根据源IP查询不到子网，则视为不可信内网地址，查询所有级别=不可信内网的子网
            if (uuidList == null || uuidList.isEmpty()) {
                logger.warn("根据源IP查询所属子网返回空,srcIp:{}", srcIp);
                uuidList = whaleManager.getSubnetListByUntrusted();
                if (uuidList == null || uuidList.isEmpty()) {
                    logger.warn("查询所有“不可信”子网，返回空");
                }
            }

            logger.debug(String.format("IP地址(%s)查询到子网为: %s", srcIp, JSONObject.toJSONString(uuidList)));
            if (uuidList == null || uuidList.size() == 0) {
                srcInvalidIpSb.append(STR_SEPERATOR);
                srcInvalidIpSb.append(srcIp);
                continue;
            }

            for (String uuid : uuidList) {
                if (srcIpSubnetMap.containsKey(uuid)) {
                    String str = srcIpSubnetMap.get(uuid);
                    str = str + STR_SEPERATOR + srcIp;
                    srcIpSubnetMap.put(uuid, str);
                } else {
                    srcIpSubnetMap.put(uuid, srcIp);
                }
            }
        }
        logger.debug(String.format("任务(%d)[%s]时间统计[子网查询]：", task.getId(), task.getTheme()) + findSubnetTime.toString());

        String[] dstIps = dstIpStr.split(STR_SEPERATOR);
        List<List<PathInfo>> pathInfoListList = new ArrayList<>();
        List<List<PathInfo>> invalidListList = new ArrayList<>();
        List<ElapseTime> elapseTimeList = new ArrayList<>();
        Set<String> srcNodeUuidSet = srcIpSubnetMap.keySet();
        int dstIpLength = dstIps.length;
        int srcIpLength = srcNodeUuidSet.size();
        int listSize = dstIpLength * srcIpLength;

        //初始化列表的列表，用以保存结果
        for (int index = 0; index < listSize; index++) {
            List<PathInfo> pathInfoList = new ArrayList<>();
            List<PathInfo> invalidList = new ArrayList<>();
            ElapseTime elapseTime = new ElapseTime();
            pathInfoListList.add(pathInfoList);
            invalidListList.add(invalidList);
            elapseTimeList.add(elapseTime);
        }

        CountDownLatch latch = new CountDownLatch(listSize);
        int index = 0;
        for (String srcNodeUuid : srcNodeUuidSet) {
            String srcIpString = srcIpSubnetMap.get(srcNodeUuid);
            for (String dstIp : dstIps) {
                List<PathInfo> pathInfoList = pathInfoListList.get(index);
                List<PathInfo> invalidList = invalidListList.get(index);
                ElapseTime elapseTime = elapseTimeList.get(index);
                index++;
                String id = "subnet_" + task.getId() + "_" + index;
                if (ExtendedExecutor.containsKey(id)) {
                    logger.error(String.format("寻找路径任务(%s)已经存在！任务不重复添加", id));
                    //线程添加失败latch也应该减一，以防死锁
                    latch.countDown();
                    continue;
                }

                pushExecutor.execute(new ExtendedLatchRunnable(new ExecutorDto(id, "", "", new Date()), latch) {
                    @Override
                    protected void start() throws InterruptedException, Exception {
                        logger.debug(String.format("开始寻找路径任务: %s...", id));
                        List<ServiceDTO> serviceList = task.getServiceList();
                        Long start = new Date().getTime();
                        List<String> uuidList = new ArrayList<>();
                        try {
                            uuidList = whaleManager.getDetailPathSubnetList(srcNodeUuid, srcIpString, dstIpStr, serviceList, task.getWhatIfCaseUuid());
                        } catch (Exception e) {
                            logger.error(String.format("任务(%d)[%s]查找落地子网异常！", task.getId(), task.getTheme()), e);
                        }
                        Long end = new Date().getTime();
                        elapseTime.setMaxTime(end - start);
                        elapseTime.setMinTime(end - start);
                        elapseTime.setTotal(end - start);
                        elapseTime.setCount();
                        if (uuidList.size() == 0) {
                            PathInfo pathInfo = new PathInfo();
                            pathInfo.setSrcIp(srcIpString);
                            pathInfo.setDstIp(dstIp);
                            pathInfo.setSrcNodeUuid(srcNodeUuid);
                            pathInfo.setDstNodeUuid("");
                            pathInfo.setServiceList(serviceList);
                            invalidList.add(pathInfo);
                            return;
                        }

                        for (String uuid : uuidList) {
                            PathInfo pathInfo = new PathInfo();
                            pathInfo.setSrcIp(srcIpString);
                            pathInfo.setSrcNodeUuid(srcNodeUuid);
                            pathInfo.setDstIp(dstIp);
                            pathInfo.setDstNodeUuid(uuid);
                            pathInfo.setServiceList(serviceList);
                            pathInfoList.add(pathInfo);
                        }
                    }
                });
            }
        }

        try {
            latch.await();
        } catch (Exception e) {
            logger.error(String.format("任务(%d)[%s]等待落地子网查询子线程完成异常", task.getId(), task.getTheme()), e);
        }

        ElapseTime findDstSubnetTime = new ElapseTime();
        for (ElapseTime elapseTime : elapseTimeList) {
            Long elapse = elapseTime.getTotal();
            findDstSubnetTime.setMaxTime(elapse);
            findDstSubnetTime.setMinTime(elapse);
            findDstSubnetTime.setTotal(elapse);
            findDstSubnetTime.setCount();
        }
        logger.debug(String.format("任务(%d)[%s]时间统计[落地子网查询]：", task.getId(), task.getTheme()) + findDstSubnetTime.toString());

        List<PathInfo> pathInfoList = new ArrayList<PathInfo>();
        for (List<PathInfo> list : pathInfoListList) {
            pathInfoList.addAll(list);
        }

        List<PathInfo> invalidList = new ArrayList<PathInfo>();
        for (List<PathInfo> list : invalidListList) {
            invalidList.addAll(list);
        }

        pathInfoList = mergePathList(pathInfoList);
        invalidList = mergePathList(invalidList);

        List<PathInfoEntity> validPathList = savePathInfoSaveEntity(task, pathInfoList, invalidList, srcInvalidIpSb.toString());

        List<PathInfoTaskDTO> list = getPathInfoDTOList(task, validPathList);

        //若没有路，则分析完成。设置任务状态为仿真已完成。。。
        if (list.size() == 0) {
            recommendTaskManager.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_TASK_TYPE_ANALYZED);
        }

        return list;
    }

    //之后可删除
    @Deprecated
    @Override
    public List<PathInfoTaskDTO> qtFindPath(SimulationTaskDTO task) {
        logger.info(String.format("青提-任务(%d)[%s]开始拆分策略开通任务为路径...", task.getId(), task.getTheme()));
        logger.debug("任务信息为：\n" + JSONObject.toJSONString(task));
        //根据源IP拆分子网，组成路径列表
        String srcIpStr = task.getSrcIp();
        String[] srcIps = srcIpStr.split(STR_SEPERATOR);

        //将具有多个子网以及没有子网的IP地址合并到一对起始地址中
        StringBuilder srcInvalidIpSb = new StringBuilder();

        //key=子网uuid、value=多个输入的IP地址，以逗号隔开
        Map<String, String> srcIpSubnetMap = new HashMap<>();

        for (String srcIp : srcIps) {
            //根据源IP获取所属子网列表
            List<String> uuidList = whaleManager.getSubnetUuidList(srcIp);
            //2020-07-03 修bug4358=，若根据源IP查询不到子网，则视为不可信内网地址，查询所有级别=不可信内网的子网
            if (uuidList == null || uuidList.isEmpty()) {
                logger.warn("根据源IP查询所属子网返回空,srcIp:{}", srcIp);
                uuidList = whaleManager.getSubnetListByUntrusted();
                if (uuidList == null || uuidList.isEmpty()) {
                    logger.warn("查询所有“不可信”子网，返回空");
                }
            }

            logger.debug(String.format("IP地址(%s)查询到子网为: %s", srcIp, JSONObject.toJSONString(uuidList)));
            if (uuidList == null || uuidList.size() == 0) {
                srcInvalidIpSb.append(STR_SEPERATOR);
                srcInvalidIpSb.append(srcIp);
                continue;
            }

            for (String uuid : uuidList) {
                if (srcIpSubnetMap.containsKey(uuid)) {
                    String str = srcIpSubnetMap.get(uuid);
                    str = str + STR_SEPERATOR + srcIp;
                    srcIpSubnetMap.put(uuid, str);
                } else {
                    srcIpSubnetMap.put(uuid, srcIp);
                }
            }
        }
        logger.debug(String.format("任务(%d)[%s]时间统计[子网查询]：", task.getId(), task.getTheme()));

        String dstIpStr = task.getDstIp();
        String[] dstIps = dstIpStr.split(STR_SEPERATOR);

        List<PathInfo> pathInfoList = new ArrayList<>();
        Set<String> srcNodeUuidSet = srcIpSubnetMap.keySet();

        //将源子网、目的地址进行组合
        for (String srcNodeUuid : srcNodeUuidSet) {
            //同子网下，输入的源IP
            String srcIpString = srcIpSubnetMap.get(srcNodeUuid);

            //此处直接关联，不查落地子网
            PathInfo pathInfo = new PathInfo();
            pathInfo.setSrcIp(srcIpString);
            pathInfo.setSrcNodeUuid(srcNodeUuid);
            pathInfo.setDstIp(task.getDstIp());
            pathInfo.setDstNodeUuid("");
            pathInfo.setServiceList(task.getServiceList());
            pathInfoList.add(pathInfo);

        }

        logger.info("源子网、目的地址进行组合,taskId:{}, pahtInfoList:{}", task.getId(), JSONObject.toJSONString(pathInfoList));

        /*************以上地址拆分，仅仅是为了适配仿真的业务代码，后期可能会改**********************/

        //入库  注：不查子网，就不存在无效路径的
        List<PathInfoEntity> validPathList = savePathInfoSaveEntity(task, pathInfoList, Collections.EMPTY_LIST, srcInvalidIpSb.toString());

        //获取路径参数拼接
        List<PathInfoTaskDTO> list = getPathInfoDTOList(task, validPathList);

        //若没有路，则分析完成。设置任务状态为仿真已完成。。。
        if (list.size() == 0) {
            recommendTaskManager.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_TASK_TYPE_ANALYZED);
        }

        return list;
    }

    private String getSubnetString(String subnetUuid) {
        String subnetIp = whaleManager.getSubnetStringByUuid(subnetUuid);
        return subnetIp;
    }

    private List<PathInfo> mergePathList(List<PathInfo> list) {
        List<PathInfo> mergedList = new ArrayList<>();
        for (PathInfo pathInfo : list) {
            boolean merged = false;
            for (PathInfo mergedPath : mergedList) {
                //源uuid，服务，目的子网一样才合并目的地址
                if (mergedPath.getSrcNodeUuid().equals(pathInfo.getSrcNodeUuid()) && mergedPath.getDstNodeUuid().equals(pathInfo.getDstNodeUuid())
                        && mergedPath.equalsServiceList(pathInfo)) {
                    String dstIp = mergedPath.getDstIp() + STR_SEPERATOR + pathInfo.getDstIp();
                    mergedPath.setDstIp(dstIp);
                    merged = true;
                }
            }

            if (!merged) {
                mergedList.add(pathInfo);
            }
        }

        return mergedList;
    }

    @Data
    @ApiModel("路径数据对象")
    public class PathInfo {

        @ApiModelProperty("源地址")
        String srcIp;

        @ApiModelProperty("源地址所在子网")
        String srcNodeUuid;

        @ApiModelProperty("目的地址")
        String dstIp;

        @ApiModelProperty("目的地址所在子网")
        String dstNodeUuid;

        @ApiModelProperty("服务列表")
        List<ServiceDTO> serviceList;
        @ApiModelProperty("ip类型")
        Integer ipType;


        boolean equalsServiceList(PathInfo pathInfo) {
            //先比较大小，大小不一致则不相等
            if (pathInfo.getServiceList().size() != this.serviceList.size()) {
                return false;
            }

            for (ServiceDTO service : this.serviceList) {
                boolean hasSame = false;
                for (ServiceDTO pathInfoService : pathInfo.getServiceList()) {
                    if (pathInfoService.equals(service)) {
                        //找到相同的设置标志位并退出内层循环
                        hasSame = true;
                        break;
                    }
                }

                //有没有找到的则说明不完全相等，返回
                if (hasSame != true) {
                    return false;
                }
            }

            for (ServiceDTO service : this.serviceList) {
                boolean hasSame = false;
                for (ServiceDTO pathInfoService : pathInfo.getServiceList()) {
                    if (pathInfoService.equals(service)) {
                        //找到相同的设置标志位并退出内层循环
                        hasSame = true;
                        break;
                    }
                }

                if (hasSame != true) {
                    return false;
                }
            }

            return true;
        }
    }

    @Deprecated
    @Override
    public List<PathInfoTaskDTO> findInternetPath(SimulationTaskDTO task) {
        logger.info(String.format("互联网开通任务(%d)[%s]开始拆分任务为路径...", task.getId(), task.getTheme()));

        //获取互联网开通中子网和对应IP地址Map
        Map<String, String> srcIpSubnetMap = getSubnetStringByIp(task.getSrcIp());
        List<String> dstSubnetList = parseListString(task.getDstSubnet());
        Map<String, String> dstIpSubnetMap = new HashMap<>();
        for (String uuid : dstSubnetList) {
            dstIpSubnetMap.put(uuid, EMPTY_STRING);
        }

        //获取路径信息列表
        List<PathInfo> pathInfoList = getPathInfoList(srcIpSubnetMap, dstIpSubnetMap, task);

        //根据路径信息获取路径数据对象
        List<PathInfoEntity> pathInfoEntityList = getPathInfoEntityList(pathInfoList, task);

        //存储路径信息数据，放在获取路径分析任务之前是为了获取pathId存储到pathInfoTaskDTO中
        savePathInfoEntityList(pathInfoEntityList);

        //根据路径信息保存路径分析任务
        List<PathInfoTaskDTO> pathInfoTaskDTOList = getPathInfoTaskDTOList(pathInfoEntityList, task);

        return pathInfoTaskDTOList;
    }

    /**
     * 将分隔符分隔字符串变成字符串列表
     *
     * @param listString 逗号分隔字符串
     * @param seperator  分隔符
     * @return 字符串列表
     */
    private List<String> parseListString(String listString, String seperator) {
        List<String> list = new ArrayList<>();
        if (AliStringUtils.isEmpty(listString)) {
            return list;
        }

        String[] strs = listString.split(",");
        for (String str : strs) {
            list.add(str);
        }

        return list;
    }

    /**
     * 将逗号符分隔字符串变成字符串列表
     *
     * @param listString 逗号分隔字符串
     * @return 字符串列表
     */
    private List<String> parseListString(String listString) {
        return parseListString(listString, STR_SEPERATOR);
    }

    /**
     * 将Key，Value添加到map，若map中有值该key，则将value添加到现有value之后，通过分隔符分割。
     *
     * @param map       需要增加值的map
     * @param key       键
     * @param value     值
     * @param seperator 分隔符
     */
    private void putValueToMap(Map<String, String> map, String key, String value, String seperator) {
        if (map.containsKey(key)) {
            String str = map.get(key);
            str = str + seperator + value;
            map.put(key, str);
        } else {
            map.put(key, value);
        }
    }

    /**
     * 将Key，Value添加到map，若map中有值该key，则将value添加到现有value之后，通过分隔符分割。
     *
     * @param map   需要增加值的map
     * @param key   键
     * @param value 值
     */
    private void putValueToMap(Map<String, String> map, String key, String value) {
        putValueToMap(map, key, value, STR_SEPERATOR);
    }

    /**
     * 根据IP地址查询所在子网和IP地址对应Map
     *
     * @param ipAddressesString ip地址字符串，逗号分隔
     * @return 子网uuid和ip地址字符串对应Map，若IP地址找不到子网，则uuid为空字符串
     */
    private Map<String, String> getSubnetStringByIp(String ipAddressesString) {
        String[] ipAddresses = ipAddressesString.split(",");

        Map<String, String> srcIpSubnetMap = new HashMap<>();
        for (String ipAddress : ipAddresses) {
            List<String> list = whaleManager.getSubnetUuidList(ipAddress);
            if (list != null && list.size() > 0) {
                for (String uuid : list) {
                    putValueToMap(srcIpSubnetMap, uuid, ipAddress);
                }
            } else {
                putValueToMap(srcIpSubnetMap, EMPTY_STRING, ipAddress);
            }
        }
        return srcIpSubnetMap;
    }

    /**
     * 获取路径信息列表
     *
     * @param srcIpSubnetMap 源IP子网对应列表
     * @param dstIpSubnetMap 目的IP子网对应列表
     * @param task           仿真任务信息
     * @return 路径信息列表
     */
    List<PathInfo> getPathInfoList(Map<String, String> srcIpSubnetMap, Map<String, String> dstIpSubnetMap, SimulationTaskDTO task) {
        Set<String> srcSubnetKey = srcIpSubnetMap.keySet();
        List<PathInfo> pathInfoList = new ArrayList<>();

        for (String srcUuid : srcSubnetKey) {
            if (AliStringUtils.isEmpty(srcUuid)) {
                PathInfo pathInfo = new PathInfo();
                pathInfo.setSrcIp(srcIpSubnetMap.get(srcUuid));
                pathInfo.setSrcNodeUuid(srcUuid);
                pathInfo.setDstIp(task.getDstIp());
                pathInfo.setDstNodeUuid("");
                pathInfo.setServiceList(task.getServiceList());
                pathInfoList.add(pathInfo);
                logger.debug(String.format("互联网开通任务(%d)[%s]拆分路径为：%s", task.getId(), task.getTheme(), JSONObject.toJSONString(pathInfo)));
            } else {
                Set<String> dstSubnetKey = dstIpSubnetMap.keySet();
                for (String dstUuid : dstSubnetKey) {
                    PathInfo pathInfo = new PathInfo();
                    pathInfo.setSrcIp(srcIpSubnetMap.get(srcUuid));
                    pathInfo.setSrcNodeUuid(srcUuid);
                    pathInfo.setDstIp(dstIpSubnetMap.get(dstUuid));
                    pathInfo.setDstNodeUuid(dstUuid);
                    pathInfo.setServiceList(task.getServiceList());
                    pathInfoList.add(pathInfo);
                    logger.debug(String.format("互联网开通任务(%d)[%s]拆分路径为：%s", task.getId(), task.getTheme(), JSONObject.toJSONString(pathInfo)));
                }
            }
        }
        return pathInfoList;
    }

    /**
     * 获取路径数据对象列表，用以存储到push_path_info表
     *
     * @param pathInfoList 路径信息列表
     * @param task         仿真任务信息
     * @return 路径数据对象列表，用以存储数据库
     */
    List<PathInfoEntity> getPathInfoEntityList(List<PathInfo> pathInfoList, SimulationTaskDTO task) {
        List<PathInfoEntity> pathInfoEntityList = new ArrayList<>();

        List<PathInfoEntity> invalid = new ArrayList<>();
        for (PathInfo pathInfo : pathInfoList) {
            PathInfoEntity pathInfoEntity = new PathInfoEntity();

            pathInfoEntity.setTaskId(task.getId());
            pathInfoEntity.setSrcIp(pathInfo.getSrcIp());
            String srcNodeUuid = pathInfo.getSrcNodeUuid();
            pathInfoEntity.setSrcNodeUuid(pathInfo.getSrcNodeUuid());
            if (!AliStringUtils.isEmpty(srcNodeUuid)) {
                pathInfoEntity.setSrcNodeDevice(whaleManager.getSubnetStringByUuid(pathInfo.getSrcNodeUuid()));
            }
            pathInfoEntity.setDstIp(pathInfo.getDstIp());
            pathInfoEntity.setDstNodeUuid(pathInfo.getDstNodeUuid());
            if (!AliStringUtils.isEmpty(pathInfo.getDstNodeUuid())) {
                pathInfoEntity.setDstNodeDevice(whaleManager.getSubnetStringByUuid(pathInfo.getDstNodeUuid()));
            }
            if (pathInfo.getServiceList() != null) {
                pathInfoEntity.setService(JSONObject.toJSONString(pathInfo.getServiceList()));
            }

            //源子网不存在，目的子网不存在，或者源/目的子网相同的路径，设置相应的路径分析状态，并将路径设置为不可用，加入invalid列表
            if (AliStringUtils.isEmpty(pathInfoEntity.getSrcNodeUuid())) {
                pathInfoEntity.setEnablePath(PolicyConstants.PATH_ENABLE_DISABLE);
                pathInfoEntity.setAnalyzeStatus(PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_SRC_ADDRESS_HAS_NO_SUBNET);
                invalid.add(pathInfoEntity);
            } else if (AliStringUtils.isEmpty(pathInfoEntity.getDstNodeUuid())) {
                pathInfoEntity.setEnablePath(PolicyConstants.PATH_ENABLE_DISABLE);
                pathInfoEntity.setAnalyzeStatus(PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_DST_ADDRESS_HAS_NO_SUBNET);
                invalid.add(pathInfoEntity);
            } else if (pathInfoEntity.getSrcNodeUuid().equals(pathInfoEntity.getDstNodeUuid())) {
                pathInfoEntity.setEnablePath(PolicyConstants.PATH_ENABLE_DISABLE);
                pathInfoEntity.setAnalyzeStatus(PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_SUBNET_CANNOT_BE_THE_SAME);
                invalid.add(pathInfoEntity);
            } else {
                pathInfoEntity.setEnablePath(PolicyConstants.PATH_ENABLE_ENABLE);
                pathInfoEntityList.add(pathInfoEntity);
            }
        }

        //找不到子网的路径放到所有路径最后
        pathInfoEntityList.addAll(invalid);

        return pathInfoEntityList;
    }

    /**
     * 存储路径信息数据
     *
     * @param pathInfoEntityList
     */
    private void savePathInfoEntityList(List<PathInfoEntity> pathInfoEntityList) {
        for (PathInfoEntity entity : pathInfoEntityList) {
            recommendTaskManager.addPathInfo(entity);
        }
    }

    /**
     * 获取路径任务数据列表，用以后续开通任务对路径进行仿真使用
     *
     * @param pathInfoEntityList 路径数据对象列表
     * @param task               仿真任务信息
     * @return 路径任务数据列表
     */
    List<PathInfoTaskDTO> getPathInfoTaskDTOList(List<PathInfoEntity> pathInfoEntityList, SimulationTaskDTO task) {
        List<PathInfoTaskDTO> pathInfoTaskDTOList = new ArrayList<>();

        for (PathInfoEntity entity : pathInfoEntityList) {
            if (entity.getEnablePath() == PolicyConstants.PATH_ENABLE_DISABLE) {
                //不启用的路径，要么为，不加入分析队列
                continue;
            }
            PathInfoTaskDTO pathInfoTaskDTO = new PathInfoTaskDTO();

            pathInfoTaskDTO.setId(entity.getId());
            pathInfoTaskDTO.setSrcIp(entity.getSrcIp());
            pathInfoTaskDTO.setSrcNodeUuid(entity.getSrcNodeUuid());
            pathInfoTaskDTO.setSrcNodeSubnet(entity.getSrcNodeDevice());
            pathInfoTaskDTO.setDstIp(entity.getDstIp());
            pathInfoTaskDTO.setDstNodeUuid(entity.getDstNodeUuid());
            pathInfoTaskDTO.setDstNodeSubnet(entity.getDstNodeDevice());

            pathInfoTaskDTO.setServiceList(task.getServiceList());
            pathInfoTaskDTO.setTaskId(task.getId());
            pathInfoTaskDTO.setTheme(task.getTheme());
            pathInfoTaskDTO.setOrderNumber(task.getOrderNumber());
            pathInfoTaskDTO.setDescription(task.getDescription());
            pathInfoTaskDTO.setStartTime(task.getStartTime());
            pathInfoTaskDTO.setEndTime(task.getEndTime());
            pathInfoTaskDTO.setIdleTimeout(task.getIdleTimeout());
            //青提需要赋值，whale就为空，不用使用这个字段
            pathInfoTaskDTO.setDeviceWhatifs(task.getDeviceWhatifs());
            pathInfoTaskDTOList.add(pathInfoTaskDTO);
        }

        return pathInfoTaskDTOList;
    }

    /**
     * 存储路径查找结果，返回有效路径Entity对象列表
     *
     * @param validPathInfoList   有效路径列表
     * @param invalidPathInfoList 无效路径列表
     * @param invalidSrcIp        无效源地址组（找不到子网源地址组）
     * @return 有效路径Entity对象列表
     */
    List<PathInfoEntity> savePathInfoSaveEntity(SimulationTaskDTO task, List<PathInfo> validPathInfoList, List<PathInfo> invalidPathInfoList, String invalidSrcIp) {
        List<PathInfoEntity> validList = new ArrayList<>();
        List<PathInfoEntity> allList = new ArrayList<>();

        //将有效路径转换成entity加入两个列表，valid用来返回，all用来存储，获取数据库自增id后返回
        for (PathInfo pathInfo : validPathInfoList) {
            PathInfoEntity entity = getPathInfoEntity(pathInfo);

            entity.setTaskId(task.getId());

            //validPathInfoList中的路径默认为enable
            entity.setEnablePath(PolicyConstants.PATH_ENABLE_ENABLE);

            entity.setAnalyzeStatus(PolicyConstants.POLICY_INT_RECOMMEND_TASK_NO_STARTED);

            validList.add(entity);
            allList.add(entity);
        }

        for (PathInfo pathInfo : invalidPathInfoList) {
            PathInfoEntity entity = getPathInfoEntity(pathInfo);

            entity.setTaskId(task.getId());

            //invalidPathInfoList中的路径默认为disable
            entity.setEnablePath(PolicyConstants.PATH_ENABLE_DISABLE);

            entity.setAnalyzeStatus(PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_DST_ADDRESS_HAS_NO_SUBNET);

            allList.add(entity);
        }

        if (!AliStringUtils.isEmpty(invalidSrcIp)) {
            PathInfoEntity entity = new PathInfoEntity();

            entity.setTaskId(task.getId());

            entity.setSrcIp(invalidSrcIp);
            entity.setDstIp(task.getDstIp());
            if (CollectionUtils.isNotEmpty(task.getServiceList())) {
                entity.setService(JSONObject.toJSONString(task.getServiceList()));
            }


            entity.setEnablePath(PolicyConstants.PATH_ENABLE_DISABLE);
            entity.setAnalyzeStatus(PolicyConstants.POLICY_INT_RECOMMEND_ANALYZE_SRC_ADDRESS_HAS_NO_SUBNET);

            allList.add(entity);
        }

        int rc = recommendTaskManager.addPathInfoList(allList);
        if (rc != ReturnCode.POLICY_MSG_OK) {
            logger.warn(String.format("任务(%d)[%s]存储路径系信息错误！%s", task.getId(), task.getTheme(), ReturnCode.getMsg(rc)));
        }

        return validList;
    }

    /**
     * 基于PathInfo新建一个PathInfoEntity对象
     *
     * @param pathInfo 路径信息数据
     * @return PathInfoEntity对象
     */
    PathInfoEntity getPathInfoEntity(PathInfo pathInfo) {
        PathInfoEntity entity = new PathInfoEntity();

        entity.setSrcIp(pathInfo.getSrcIp());
        entity.setSrcNodeUuid(pathInfo.getSrcNodeUuid());

        entity.setDstIp(pathInfo.getDstIp());
        entity.setDstNodeUuid(pathInfo.getDstNodeUuid());

        //放在此处为了减少冗余代码
        entity.setSrcNodeDevice(StringUtils.isEmpty(entity.getSrcNodeUuid()) ? "" : getSubnetString(entity.getSrcNodeUuid()));
        entity.setDstNodeDevice(StringUtils.isEmpty(entity.getDstNodeUuid()) ? "" : getSubnetString(entity.getDstNodeUuid()));
        if (CollectionUtils.isNotEmpty(pathInfo.getServiceList())) {
            entity.setService(JSONObject.toJSONString(pathInfo.getServiceList()));
        }


        return entity;
    }

    /**
     * 将PathInfoEntity列表转换为PathInfoDTO列表
     *
     * @param task       任务信息
     * @param entityList PathInfoEntity列表
     * @return PathInfoTaskDTO列表
     */
    List<PathInfoTaskDTO> getPathInfoDTOList(SimulationTaskDTO task, List<PathInfoEntity> entityList) {
        List<PathInfoTaskDTO> taskList = new ArrayList<>();
        if (entityList.size() == 0) {
            return taskList;
        }

        for (PathInfoEntity entity : entityList) {
            PathInfoTaskDTO dto = new PathInfoTaskDTO();

            //用以关联的id数据
            dto.setId(entity.getId());
            dto.setTaskId(entity.getTaskId());

            //其他数据
            dto.setTheme(task.getTheme());
            dto.setOrderNumber(task.getOrderNumber());
            dto.setDescription(task.getDescription());
            dto.setStartTime(task.getStartTime());
            dto.setEndTime(task.getEndTime());

            //源相关数据
            dto.setSrcIp(entity.getSrcIp());
            dto.setSrcNodeUuid(entity.getSrcNodeUuid());
            dto.setSrcNodeSubnet(entity.getSrcNodeDevice());
            dto.setIpType(task.getIpType());
            //目的相关数据
            String dstIp;
            if (task.getTaskType() == PolicyConstants.IN2OUT_INTERNET_RECOMMEND && StringUtils.isBlank(entity.getDstIp())) {
                dstIp = internetDstIp;
            } else {
                dstIp = entity.getDstIp();
            }
            dto.setDstIp(dstIp);
            dto.setDstNodeUuid(entity.getDstNodeUuid());
            dto.setDstNodeSubnet(entity.getDstNodeDevice());

            //服务
            dto.setServiceList(ServiceDTOUtils.toList(entity.getService()));

            //模拟变更场景数据
            dto.setWhatIfCaseUuid(task.getWhatIfCaseUuid());
            dto.setDeviceWhatifs(task.getDeviceWhatifs());

            //设置长链接超时时间
            dto.setIdleTimeout(task.getIdleTimeout());
            taskList.add(dto);
        }
        return taskList;
    }

    @Override
    public List<PathInfoTaskDTO> qtBusinessFindPath(SimulationTaskDTO task) {
        logger.info(String.format("青提-任务(%d)[%s]开始拆分策略业务开通任务为路径...", task.getId(), task.getTheme()));
        logger.debug("任务信息为：\n" + JSONObject.toJSONString(task));
        //根据源IP拆分子网，组成路径列表

        //将具有多个子网以及没有子网的IP地址合并到一对起始地址中

        StringBuilder srcInvalidIpSb = new StringBuilder();
        List<PathInfo> pathInfoList = getPathInfo(task, srcInvalidIpSb);
        //key=子网uuid、value=多个输入的IP地址，以逗号隔开
        logger.info("源子网、目的地址进行组合,taskId:{}, pahtInfoList:{}", task.getId(), JSONObject.toJSONString(pathInfoList));

        /*************以上地址拆分，仅仅是为了适配仿真的业务代码，后期可能会改**********************/
        //入库  注：不查子网，就不存在无效路径的
        List<PathInfoEntity> validPathList = savePathInfoSaveEntity(task, pathInfoList, Collections.EMPTY_LIST, srcInvalidIpSb.toString());

        //获取路径参数拼接
        List<PathInfoTaskDTO> list = getPathInfoDTOList(task, validPathList);

        //若没有路，则分析完成。设置任务状态为仿真已完成。。。
        if (list.size() == 0) {
            recommendTaskManager.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_TASK_TYPE_ANALYZED);
        }
        return list;


    }

    /***
     * 业务开通
     * @param task
     * @param srcInvalidIpSb
     * @return
     */
    private List<PathInfo> getPathInfo(SimulationTaskDTO task, StringBuilder srcInvalidIpSb) {
        List<PathInfo> pathInfoList = new ArrayList<>();
        //查询源ip的子网信息设置在subnetSearchResultDTOS中
        List<SubnetSearchResultDTO> subnetSearchResultDTOS = policyBaseInfoManager.subnetSearchWith(task);

        //目的地址可能包含Ip及域名，需要区分开，ip为一组，每个域名都单独建一个对象，取出域名的ip地址，多个域名如何遍历？

//目的地址ip组
        List<String> dscIpList = new ArrayList<>();
        Map<String, String> domainMap = new HashMap<>();
        StringBuilder isConvertFail = new StringBuilder();
        //将目的地址中的域名转换为ip
        Boolean hasDomain = commandSimulationCommonService.convertDomainToIp(task,dscIpList,domainMap,isConvertFail);
        if (StringUtils.isNotEmpty(isConvertFail.toString())) {
            return pathInfoList;
        }
        //将解析后域名对应的ipList存在task对象中
        task.setDomainConvertIp(domainMap);

        //获取ip组的目的地址,做去重处理
        dscIpList = dscIpList.stream().distinct().collect(Collectors.toList());
        String dstIps = dscIpList.stream().collect(Collectors.joining(","));

        if (CollectionUtils.isNotEmpty(subnetSearchResultDTOS)) {
            for (SubnetSearchResultDTO subnetSearchResultDTO : subnetSearchResultDTOS) {
                List<SubnetRO> subnetROS = subnetSearchResultDTO.getSrcSubnetRO();//源子网信息
                String srcIpStr = subnetSearchResultDTO.getSrcIp();
                if (CollectionUtils.isNotEmpty(subnetROS)) {
                    for (SubnetRO subnetRO : subnetROS) {
                        //目的地址中ip组不为空加入路径
                        if (StringUtils.isNotEmpty(dstIps)) {
                            PathInfo pathInfo = getPathInfoForDstIp(subnetRO, task.getServiceList(), srcIpStr, dstIps);
                            pathInfoList.add(pathInfo);
                        }
                        if (hasDomain) {
                            //获取域名组的目的地址
                            for (Map.Entry<String, String> entry : domainMap.entrySet()) {
                                String domainDstStr = entry.getKey();
                                String domain = entry.getValue();
                                PathInfo domainPathInfo = getPathInfoForDstIp(subnetRO, task.getServiceList(), srcIpStr, domainDstStr);
                                pathInfoList.add(domainPathInfo);
                            }
                        }

                        if (StringUtils.isEmpty(dstIps) && !hasDomain) {
                            //如果目的地址既没有ip又没输入域名，说明目的地址为空，则生成一条目的地址为空的路径，后续代码会替换为114.114.114.114
                            PathInfo pathInfo = getPathInfoForDstIp(subnetRO, task.getServiceList(), srcIpStr, "");
                            pathInfoList.add(pathInfo);
                        }
                    }
                } else {
                    if(StringUtils.isNotBlank(srcIpStr)){
                        String[] srcIps = srcIpStr.split(STR_SEPERATOR);
                        for (String srcIp : srcIps) {
                            srcInvalidIpSb.append(STR_SEPERATOR);
                            srcInvalidIpSb.append(srcIp);
                        }
                        if (srcInvalidIpSb.indexOf(STR_SEPERATOR) == 0) {
                            srcInvalidIpSb.deleteCharAt(0);
                        }
                    }

                }
            }
        } else {
            return pathInfoList;
        }
        return pathInfoList;
    }


    /**
     * 根据传入的参数获取路径对象
     *
     * */
    private PathInfo getPathInfoForDstIp (SubnetRO subnetRO,List<ServiceDTO> serviceList, String srcIpStr, String dstIps) {
        //此处直接关联，不查落地子网
        PathInfo pathInfo = new PathInfo();
        //同子网下，输入的源IP
        String ipType = subnetRO.getIpType();
        StringBuffer srcIpString = new StringBuffer();
        if (StringUtils.isNotBlank(ipType) && POLICY_STR_NETWORK_TYPE_IP4.equalsIgnoreCase(ipType)) {
            if (StringUtils.isNotBlank(subnetRO.getIp4BaseAddress()) && StringUtils.isNotBlank(subnetRO.getIp4MaskLength())) {
                srcIpString.append(subnetRO.getIp4BaseAddress()).append("/").append(subnetRO.getIp4MaskLength());
            }
            pathInfo.setIpType(IPV4.getCode());
        } else {
            if (StringUtils.isNotBlank(subnetRO.getIp6BaseAddress()) && StringUtils.isNotBlank(subnetRO.getIp6MaskLength())) {
                srcIpString.append(subnetRO.getIp6BaseAddress()).append("/").append(subnetRO.getIp6MaskLength());
            }
            pathInfo.setIpType(IPV6.getCode());
        }
        //这里是将子网和子网uuid都组装起来，给青提
        pathInfo.setSrcIp(srcIpStr);

        pathInfo.setSrcNodeUuid(subnetRO.getUuid());
        pathInfo.setDstIp(dstIps);
        pathInfo.setDstNodeUuid("");
        pathInfo.setServiceList(serviceList);

        return pathInfo;
    }

    @Deprecated
    @Override
    public List<PathInfoTaskDTO> qtInternetFindPath(SimulationTaskDTO task) {
        int taskType = task.getTaskType();
        logger.info(String.format("%s开通任务(%d)[%s]开始拆分任务为路径...", RecommendTypeEnum.getRecommendTypeByTypeCode(taskType).getDesc(), task.getId(), task.getTheme()));

        StringBuilder srcInvalidIpSb = new StringBuilder();

        //互联网内到外 用选择的标签+源IP做and或or的匹配，得到原子网；未选择标签，则按原逻辑处理；用选择的标签+源IP做and或or的匹配，得到原子网；未选择标签，则按原逻辑处理；

        List<PathInfo> pathInfoList = getPathInfo(task, srcInvalidIpSb);
        //入库  注：不查子网，就不存在无效路径的
        List<PathInfoEntity> validPathList = savePathInfoSaveEntity(task, pathInfoList, Collections.EMPTY_LIST, srcInvalidIpSb.toString());
        //获取路径参数拼接
        List<PathInfoTaskDTO> list = getPathInfoDTOList(task, validPathList);
        //若没有路，则分析完成。设置任务状态为仿真已完成。。。
        if (list.size() == 0) {
            recommendTaskManager.updateTaskStatus(task.getId(), PolicyConstants.POLICY_INT_TASK_TYPE_ANALYZED);
        }
        return list;


    }
}
