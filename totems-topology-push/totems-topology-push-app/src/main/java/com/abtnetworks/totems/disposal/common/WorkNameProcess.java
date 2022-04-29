package com.abtnetworks.totems.disposal.common;

import com.abtnetworks.totems.disposal.entity.DisposalBranchEntity;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author luwei
 * @date 2019/11/15
 */
@Slf4j
@Component
public class WorkNameProcess implements CommandLineRunner {

    @Value("${push.work.name}")
    private String workName;

    @Value("${push.work.ip}")
    private String workIp;

    @Autowired
    private KafkaTemplate kafkaTemplate;


    @Override
    public void run(String... args) throws Exception {
        log.info("服务重启后，发送单位名称及IP地址,workName:{},workIp:{}", workName, workIp);
        DisposalBranchEntity entity = new DisposalBranchEntity();
        entity.setName(workName);
        entity.setIp(workIp);
        kafkaTemplate.send("workQueue", entity);

    }
}
