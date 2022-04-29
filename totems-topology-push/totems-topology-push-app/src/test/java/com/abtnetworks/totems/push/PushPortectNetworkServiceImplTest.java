package com.abtnetworks.totems.push;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.auto.dao.mysql.ProtectNetworkConfigDetailMapper;
import com.abtnetworks.totems.auto.dao.mysql.ProtectNetworkConfigMapper;
import com.abtnetworks.totems.auto.entity.ProtectNetworkConfigDetailEntity;
import com.abtnetworks.totems.auto.entity.ProtectNetworkConfigEntity;
import com.abtnetworks.totems.auto.utils.IpAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @desc    防护网段配置测试类
 * @author liuchanghao
 * @date 2021-06-10 16:25
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class PushPortectNetworkServiceImplTest {
    @Resource
    ProtectNetworkConfigDetailMapper protectNetworkConfigDetailMapper;

    @Resource
    ProtectNetworkConfigMapper protectNetworkConfigMapper;

    @Test
    public void insert() {
        ProtectNetworkConfigDetailEntity record = new ProtectNetworkConfigDetailEntity();
        long[] ipStartEnd = IpAddress.getIpStartEnd("10.10.10.15-10.10.10.17");
        record.setId(4l);
        record.setIpType(0);
        record.setCreateTime(new Date());
        record.setConfigId(1l);

        record.setIpv4Start(ipStartEnd[0]);
        record.setIpv4End(ipStartEnd[1]);

        int num  = protectNetworkConfigDetailMapper.insert(record);
        System.out.println(num);
    }

    @Test
    public void findList() {
        ProtectNetworkConfigEntity record = new ProtectNetworkConfigEntity();
        record.setIpType(0);
        record.setIpv4Start(168430080l);
        record.setIpv4End(168430335l);
        List<ProtectNetworkConfigEntity> entityList = protectNetworkConfigMapper.findList(record);
        System.out.println(entityList);
    }

}
