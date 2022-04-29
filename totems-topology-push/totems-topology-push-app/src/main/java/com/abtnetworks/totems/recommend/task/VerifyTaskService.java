package com.abtnetworks.totems.recommend.task;

import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VerifyTaskService {

    /**
     * 开始验证策略开通任务
     * @param list
     * @return
     */
    int startVerify(List<RecommendTaskEntity> list);

    /**
     * 策略验证是否正在运行
     * @return 是则返回true，否则false;
     */
    boolean isVerifyRunning();
}
