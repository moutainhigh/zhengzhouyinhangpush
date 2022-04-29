package com.abtnetworks.totems.push;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.dao.mysql.PushForbidIpMapper;
import com.abtnetworks.totems.push.dto.CommandTaskDTO;
import com.abtnetworks.totems.push.entity.PushForbidIpEntity;
import com.abtnetworks.totems.push.service.PushForbidIpService;
import com.abtnetworks.totems.push.service.task.PushTaskService;
import com.github.pagehelper.PageInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @desc    封禁IP 测试类
 * @author liuchanghao
 * @date 2020-09-11 11:25
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class PushForbidIpServiceImplTest {
    @Resource
    PushForbidIpService pushForbidIpService;

    @Resource
    PushForbidIpMapper pushForbidIpMapper;


    @Test
    public void addOrUpdate() {
        PushForbidIpEntity entity = new PushForbidIpEntity();
        entity.setSrcIp("1.2.3.4,6.6.6.6");
        entity.setScenesUuidArray(new String[]{"46181abff8a940488592b0a47ba6698d","46181abff8a940488592b0a47ba6698d"});
        pushForbidIpService.addOrUpdate(entity);
    }

    @Test
    public void enable() {
        ReturnT<String> stringReturnT = pushForbidIpService.enable(1,"N","superadmin");
        System.out.println(stringReturnT);
    }

    @Test
    public void findList() {
        PushForbidIpEntity entity = new PushForbidIpEntity();
        entity.setStatus(0);
        PageInfo<PushForbidIpEntity> pageList = pushForbidIpService.findList(entity,1,20);
        System.out.println(pageList);
    }

    @Test
    public void findSerialNumber() {
        List<PushForbidIpEntity> list = pushForbidIpMapper.findSerialNumber("DI200912");
        System.out.println(list);
    }

}
