/**
 * 策略开通任务服务
 *
 * @Author WenJiachang
 */
package com.abtnetworks.totems.recommend.manager;

import com.abtnetworks.totems.common.dto.commandline.DNatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.NatPolicyDTO;
import com.abtnetworks.totems.common.dto.commandline.SNatPolicyDTO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.push.vo.PushTaskVO;
import com.abtnetworks.totems.recommend.dto.push.TaskStatusBranchLevelsDTO;
import com.abtnetworks.totems.recommend.dto.task.RecommendPolicyDTO;
import com.abtnetworks.totems.recommend.dto.task.SearchRecommendTaskDTO;
import com.abtnetworks.totems.recommend.entity.*;
import com.abtnetworks.totems.recommend.vo.BatchTaskVO;
import com.abtnetworks.totems.recommend.vo.PathDetailVO;
import com.abtnetworks.totems.recommend.vo.PolicyTaskDetailVO;
import com.abtnetworks.totems.recommend.vo.RecommendPolicyVO;
import com.abtnetworks.totems.recommend.vo.TaskStatusVO;
import com.github.pagehelper.PageInfo;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface RecommendTaskManager {



    /**
     * 根据设备uuid获取采集状态
     * @param uuid
     * @return
     */
    int getGatherStateByDeviceUuid(String uuid);




    /**根据设备uuid 获取设备详情**/
    NodeEntity getTheNodeByUuid(String deviceUuid);



    /**
     * 获取设备型号
     * @param uuid 设备uuid
     * @return 品牌id
     */
    String getDeviceModelNumber(String uuid);



    /**
     * 获取设备名称
     * @param uuid 设备uuid
     * @return 设备名称
     */
    String getDeviceName(String uuid);

    /**
     * 根据设备uuid获取设备采集端口
     * @param uuid 设备uuid
     * @return 设备采集端口
     */
    Integer getDeviceGatherPort(String uuid);


    List<RiskRuleInfoEntity> getRiskInfoBySecondSortId(int secondSortId);

    /**
     * 根据ruleId获取风险分析详情数据
     * @param ruleId 策略ID
     * @return
     */
    RiskRuleDetailEntity getRiskDetailEntityByRuleId(String ruleId);

    /**
     * 根据策略开通任务id，查询生成的命令行信息
     * @param ids
     * @return
     */
    List<CommandTaskEditableEntity> listPolicyRecommendPolicyByTaskIds(String ids);

    /**
     * 根据策略开通任务id，获取生成的命令行压缩包
     * @param ids
     * @return
     */
    String getRecommendZip(String ids, String pathPrefix) ;

    ///////////////////////////////策略开通优化/新增数据库处理API//////////////////////////////

    /**
     * 根据任务Id获取路径分析信息列表
     * @param taskId 任务Id
     * @param page 页数
     * @param psize 每页条数
     * @return 路径分析信息分页数据
     */
    PageInfo<PathInfoEntity> getAnalyzePathInfoVOList(int taskId, int page, int psize);

    /**
     * 根据任务Id获取路径验证信息列表
     * @param taskId 任务Id
     * @param page 页数
     * @param psize 每页条数
     * @return 路径验证信息分页数据
     */
    PageInfo<PathInfoEntity> getVerifyPathInfoVOList(int taskId, int page, int psize);

    /**
     * 查询策略开通任务接口
     * @param searchRecommendTaskDTO 主题名称

     * @return
     */
    PageInfo<RecommendTaskEntity> getTaskList(SearchRecommendTaskDTO searchRecommendTaskDTO);


    PageInfo<PolicyTaskDetailVO> getNatPolicyTaskList(String theme, String type, int page, int psize, String taskIds, Integer id, String userName,String deviceUuid,String status ,  Authentication authentication);

    List<PolicyTaskDetailVO> getNatTaskList(String theme, String type, String taskIds, Integer id, String userName,String deviceUuid, String startTime, String endTime, Authentication authentication);

    PageInfo<PolicyTaskDetailVO> getSecurityPolicyTaskList(String theme, int page, int psize, String userName,String deviceUuid,String status , Authentication authentication);

    List<PolicyTaskDetailVO> getSecurityTaskList(String theme, String userName,String deviceUuid, String startTime, String endTime, Authentication authentication);

    PageInfo<PolicyTaskDetailVO> getCustomizeCmdTaskList(String theme, int page, int psize, String userName, String deviceUuid, Integer status, Authentication authentication);

    /**
     * 获取任务状态信息
     * @param taskId 任务id
     * @return 任务状态信息数据
     */
    TaskStatusVO getTaskStatusByTaskId(int taskId);

    /**
     *  获取路径详情数据
     * @param pathInfoId 路径信息ID
     * @param isVerifyData 是否为验证数据
     * @return 路径详情数据
     */
    PathDetailVO getPathDetail(int pathInfoId, boolean isVerifyData);

    /**
     * 获取路径设备详情
     * @param pathInfoId 路径信息id
     * @param deviceUuid 设备uuid
     * @param isVerifyData 是否为验证数据
     * @return
     */
    PathDeviceDetailEntity getDevieceDetail(int pathInfoId, String deviceUuid, boolean isVerifyData, String index);

    /**
     * 获取业务相关风险列表
     * @param pathInfoId 路径信息id
     * @return
     */
    List<PolicyRiskEntity> getRiskByPathInfoId(int pathInfoId);

    /**
     * 获取路径生成策略列表
     * @param pathInfoId 路径信息id
     * @return
     */
    List<RecommendPolicyVO> getPolicyByPathInfoId(int pathInfoId);


    List<RecommendPolicyVO> getMergedPolicyByTaskId(int taskId);



    /**
     * 获取策略检查结果
     * @param policyId 策略id
     * @return
     */
    List<CheckResultEntity> getCheckResultByPolicyId(int policyId);

    /**
     * 创建源nat策略
     * @param sNatPolicyDTO
     * @param authentication
     * @return
     */
    int insertSrcNatPolicy(SNatPolicyDTO sNatPolicyDTO,Authentication authentication);

    /**
     * 创建目的nat策略
     * @param dNatPolicyDTO
     * @param authentication
     * @return
     */
    int insertDstNatPolicy(DNatPolicyDTO dNatPolicyDTO, Authentication authentication);

    /**
     * 创建双向BothNat策略
     * @param policyDTO
     * @param auth
     * @return
     */
    int insertBothNatPolicy(NatPolicyDTO policyDTO, Authentication auth);

    /**
     * 添加策略开通任务
     * @param list 策略开通任务列表
     * @return
     */
    int insertRecommendTaskList(List<RecommendTaskEntity> list);

    /**
     * 根据任务id获取任务数据
     * @param taskId 任务id
     * @return 策略开通任务数据
     */
    RecommendTaskEntity getRecommendTaskByTaskId(int taskId);

    /**
     * 根据任务id获取任务可编辑任务数据
     * @param Id 任务id
     * @return 策略下发任务数据
     */
    CommandTaskEditableEntity getRecommendTaskById(int Id);

    /**
     * 添加路径信息
     * @return
     */
    int addPathInfo(PathInfoEntity entity);

    /**
     * 添加生成策略数据
     * @param entityList 策略生成数据
     * @return
     */
    int addRecommendPolicyList(List<RecommendPolicyEntity> entityList);

    /**
     * 添加策略检查结果
     * @param entity 策略检查结果数据
     * @return
     */
    int addCheckResult(CheckResultEntity entity);

    /**
     * 添加命令行下发任务
     * @param entity 命令行下发数据
     * @return
     */
    int addCommandTaskEditableEntity(CommandTaskEditableEntity entity);

    /**
     * 根据任务id获取路径信息列表
     * @param taskId
     * @return
     */
    List<PathInfoEntity> getPathInfoByTaskId(int taskId);


    PathInfoEntity getPathInfoByPathInfoId(int id);

    /**
     * 根据路径信息id获取策略列表
     * @param pathInfoId 路径信息id
     * @return
     */
    List<RecommendPolicyEntity> getPolicyListByPathInfoId(int pathInfoId);

    /**
     * 添加路径设备详情数据
     * @param entity 路径详情数据
     * @return
     */
    int savePathDeviceDetail(PathDeviceDetailEntity entity);



    /**
     * 添加设备详情列表
     * @param list 设备详情列表
     * @return 插入结果
     */
    int insertpathDeviceDetailList(List<PathDeviceDetailEntity> list);

    /**
     * 保存路径详情数据
     * @param pathInfoId
     * @param detailPath
     * @return
     */
    int saveVerifyDeitailPath(int pathInfoId, String detailPath);

    /**
     * 保存路径详情数据
     * @param pathInfoId
     * @param detailPath
     * @return
     */
    void saveAnalyzeDetailPath(int pathInfoId, String detailPath);

    /**
     * 批量更新路径状态信息
     * @param entity 路径信息对象
     * @return
     */
    int updatePathStatus(PathInfoEntity entity);

    /**
     * 更新任务状态
     * @param taskId 任务状态
     * @param status
     */
    void updateTaskStatus(int taskId, int status);

    /**
     * 更新任务状态
     * @param  entity 任务实体
     */
    void updateTaskByEntity(RecommendTaskEntity entity);

    /**
     * 更新路径状态
     * @param pathInfoId 路径id
     * @param status 路径状态
     */
    void updatePathPathStatus(int pathInfoId, int status);

    void updatePathAnalyzeStatus(int pathInfoId, int status);

    /**
     * 更新路径策略建议状态
     * @param pathInfoId 路径id
     * @param status 路径状态
     */
    void updatePathAdviceStatus(int pathInfoId, int status);

    int updatePathCheckStatus(int pathInfoId, int status);

    void updatePathRiskStatus(int pathInfoId, int status);

    void updatePathCmdStatusByTaskId(int taskId, int status);

    int updatePathPushStatus(int pathInfoId, int status);

    int updatePathGatherStatus(int pathInfoId, int status);



    int updatePathVerifyStatus(int pathInfoId, int status);


    void getAdvancedSettings(RecommendPolicyEntity entity);

    /**
     * 获取策略下发任务列表分页数据
     * @param theme 工单号
     * @param page 页数
     * @param psize 页面数据大小
     * @return 页面数据
     */
    PageInfo<PushTaskVO> getPushTaskList(String taskId, String theme, String taskType, String status, String pushStatus, String revertStatus, int page, int psize, String userName,String branchLevel);

    int updateCommandTaskStatus(int taskId, int status);

    int updateCommandTaskStatus(List<CommandTaskEditableEntity> list,int status);

    int updateCommandTaskPushStatus(int taskId, int status);

    int updateCommandTaskRevertStatus(int taskId, int status);

    int updateCommandTaskPushOrRevertStatus(List<CommandTaskEditableEntity> list, int status,boolean isRever);

    int updateCommandTaskStatusById(int id, int status);

    int updateCommandTaskRevertStatusById(int id, int status);

    /**
     * 停止下发更新状态时清空下发计划
     * @param taskId
     * @param status
     * @return
     */
    int updateStopTaskPushStatus(int taskId, int status);

    void addTaskRisk(int pathInfoId, String riskId);


    /**
     * 根据任务id列表删除策略生成和下发任务列表
     * @param list 任务id列表
     */
    void removePolicyTasks(List<Integer> list);

    /**
     * 根据任务id列表删除策略开通仿真相关任务
     * @param list 任务id列表
     */
    void deleteTasks(List<Integer> list ,int type);

    RecommendPolicyEntity getPolicyByPolicyId(Integer policyId);

    int setPathEnable(Integer pathInfoId, String enable);





    boolean isCheckRule();

    boolean isCheckRisk();

    List<RecommendPolicyEntity> getMergedPolicyList(int taskId);

    int addMergedPolicyList(List<RecommendPolicyEntity> list);

    int addMergedPolicy(RecommendPolicyEntity entity);

    boolean isUseCurrentObject();

    boolean isUseCurrentAddressObject();

    void removeCommandsByTask(int taskId);

    void updateTaskById(RecommendTaskEntity entity);

    List<DeviceDimension> searchDeviceDimensionByTaskId(Integer taskId);

    List<RecommendPolicyEntity> selectByDeviceDimension(DeviceDimension deviceDimension, Integer taskId);


    /**
     * 添加路径信息列表
     * @return
     */
    int addPathInfoList(List<PathInfoEntity> list);

    int getAclDirection(String deviceUuid);

    NodeEntity getDeviceByManageIp(String deviceIp);

    PageInfo<BatchTaskVO> searchBatchTaskList(String theme, String userName, int page, int psize);

    /**
     * 通过id查询批量导入信息
     * @param id
     * @return
     */
    RecommendTaskCheckEntity selectBatchTaskById(Integer id);

    void addBatchTask(RecommendTaskCheckEntity entity);

    void updateBatchTask(RecommendTaskCheckEntity entity);

    /**
     * 根据规则Id获取规则信息
     * @param ruleId
     */
    RiskRuleInfoEntity getRiskInfoByRuleId(String ruleId);

    List<NodeEntity> getNodeList();

    TaskStatusBranchLevelsDTO getPushTaskStatusList(String userName);

    /**
     * 根据开始时间和结束时间查询仿真列表
     * @param startTime
     * @param endTime
     * @return
     */
    List<RecommendTaskEntity> getTaskListByTime(String startTime, String endTime, Authentication authentication);

    /**
     * 查找策略开通执行中任务
     * @return
     */
    List<RecommendTaskEntity> selectExecuteRecommendTask();

    /**
     * 通过本次下发任务返回对应下发显示状态
     * 按照逻辑顺序进入对应分支
     * <1>.下发工单中所有设备功能状态都一致，即为该状态
     * <2>.下发工单中存在执行中的状态，即为执行中状态
     * <3.1>.下发工单中存在一个设备状态为未开始，一个为失败，即为失败
     * <3.2>.以上三种情况都不是则为部分成功
     * @param taskEntityList
     * @return
     */
    int getPushStatusInTaskList(List<CommandTaskEditableEntity> taskEntityList);

    /**
     * 通过下发汇总状态更新仿真状态
     * 1.下发状态为成功--->仿真状态为下发成功
     * 2.下发状态为失败--->仿真状态为下发失败
     * 3.其他都为下发部分成功
     * @param pushStatus
     * @return
     */
    int getPolicyStatusByPushStatus (int pushStatus);

    /**
     * 重置仿真路径的采集、验证、路径状态，更新下发状态
     * @param gatherStatus
     * @param verifyStatus
     * @param pathStatus
     */
    void setPathInfoStatus(Integer id, Integer gatherStatus, Integer verifyStatus, Integer pathStatus, Integer pushStatus);

    void updateWeTaskId(RecommendTaskEntity entity);
}
