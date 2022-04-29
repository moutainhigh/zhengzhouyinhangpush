package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.recommend.dto.task.SimulationTaskDTO;
import com.abtnetworks.totems.recommend.dto.task.WhatIfNatDTO;
import com.abtnetworks.totems.recommend.dto.task.WhatIfRouteDTO;
import com.abtnetworks.totems.whale.baseapi.ro.WhatIfRO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface WhatIfService {

    /**
     * 创建模拟变更环境
     * @param natList nat列表
     * @param name 主题/工单号
     * @param description 描述
     * @return 模拟变更场景IDc
     */
    WhatIfRO createWhatIfCase(List<WhatIfNatDTO> natList, List<WhatIfRouteDTO> routeList, String name, String description);
}
