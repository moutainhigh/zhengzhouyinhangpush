package com.abtnetworks.totems.issued.particular;

import com.abtnetworks.totems.issued.dto.ParticularDTO;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/6/12
 */
public interface SendParticularPolicyService {


    /**
     * 移动的特例处理
     *
     * @param particularDTO
     * @throws Exception
     */
    void deviceParticularByRule(ParticularDTO particularDTO) throws Exception;
}
