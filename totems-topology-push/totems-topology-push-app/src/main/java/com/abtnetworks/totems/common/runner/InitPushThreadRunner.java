package com.abtnetworks.totems.common.runner;

import com.abtnetworks.totems.push.service.task.PushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitPushThreadRunner implements ApplicationRunner {

    @Autowired
    PushTaskService pushTaskService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始循环检查定时下发任务....");
        pushTaskService.pushPeriod();
    }
}
