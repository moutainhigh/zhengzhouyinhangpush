package com.abtnetworks.totems.disposal.service.impl;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.disposal.dto.DisposalCommandDTO;
import com.abtnetworks.totems.disposal.service.DisposalCommandService;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author zc
 * @date 2019/11/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class DisposalCommandServiceImplTest {

    @Resource
    DisposalCommandService disposalCommandService;

    @Test
    public void generateCommand() {
        String c0t1 = "65922134cfb6464595b1dc996ab5eb37";
        String c0t2 = "191d84dd6afa433ba79f3d0e056cecea";
        String c1t1 = "e19ac4f0295a40bdb216779e7b297d72";
        String c1t2 = "6daa97c66a0b471c8d2cfe641164ee47";
        List<DisposalCommandDTO> disposalCommandDTOS = disposalCommandService
                .generateCommand(c0t1);
        System.out.println(JSONObject.toJSONString(disposalCommandDTOS, true));
    }
}