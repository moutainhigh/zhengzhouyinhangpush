package com.abtnetworks.totems.push.service;

import com.abtnetworks.totems.push.dto.BatchCommandTaskDTO;
import com.abtnetworks.totems.push.dto.CommandTaskDTO;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.entity.PushPwdStrategyEntity;
import com.abtnetworks.totems.push.service.executor.Executor;
import com.abtnetworks.totems.push.vo.PushPwdStrategyVO;
import org.springframework.stereotype.Service;

@Service
public interface PushService {

    /**
     * 策略下发
     * @param commandTaskDTO 下发任务
     * @return 下发结果
     */
    int pushCommand(CommandTaskDTO commandTaskDTO);

    /**
     * 策略下发
     * @param batchCommandTaskDTO 下发任务
     * @return 下发结果
     */
    int pushCommandV2(BatchCommandTaskDTO batchCommandTaskDTO);

    /**
     * 单设备策略下发
     * @param commandTaskDTO 下发任务
     * @return 下发结果
     */
    int pushCommandDevice(CommandTaskDTO commandTaskDTO);


    /***
     * 下发执行器
     * @param pushCmdDTO
     * @return
     */
    Executor getExecutor(PushCmdDTO pushCmdDTO);

    /***
     * 构建下发参数
     * @param deviceUuid
     * @param commandLines
     * @return
     */
    PushCmdDTO buildPushCmdDTO(String deviceUuid, String commandLines);

    /**
     * 密码策略操作
     * @param pwdStrategyVO
     * @return
     */
    int pwdStrategyOperation(PushPwdStrategyVO pwdStrategyVO);

    /**
     * 策略展示
     * @return
     */
    PushPwdStrategyEntity searchCmdDevicelist();

    /**
     * 获取主设备uuid
     * @param deviceUuid
     * @return
     */
    String getRootDeviceUuid(String deviceUuid);
}
