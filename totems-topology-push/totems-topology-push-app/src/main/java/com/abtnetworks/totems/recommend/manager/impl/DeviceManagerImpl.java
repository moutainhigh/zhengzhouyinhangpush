package com.abtnetworks.totems.recommend.manager.impl;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.recommend.manager.DeviceManager;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "default")
public class DeviceManagerImpl implements DeviceManager {

    @Autowired
    RecommendTaskManager recommendTaskManager;

    @Autowired
    WhaleManager whaleManager;

    @Override
    public void getDeviceInfo(CmdDTO cmdDTO) {

    }
}
