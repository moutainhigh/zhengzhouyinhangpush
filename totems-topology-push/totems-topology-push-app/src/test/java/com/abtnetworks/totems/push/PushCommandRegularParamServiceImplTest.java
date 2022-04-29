package com.abtnetworks.totems.push;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.issued.business.entity.PushCommandRegularParamEntity;
import com.abtnetworks.totems.issued.business.service.CommandRegularParamService;
import com.abtnetworks.totems.issued.business.service.RegularParamForMatchService;
import com.abtnetworks.totems.issued.dto.CommandRegularParamPageDTO;
import com.abtnetworks.totems.issued.dto.CommandRegularUpdateDTO;
import com.github.pagehelper.PageInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Author: Administrator
 * @Date: 2019/12/18
 * @desc: 请写类注释
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class PushCommandRegularParamServiceImplTest {


    @Resource
    CommandRegularParamService pushCommandRegularParamService;

    @Resource
    RegularParamForMatchService regularParamForMatchService;

    @Test
    public void addCommandRegularParam() {
        PushCommandRegularParamEntity pushCommandRegularParamEntity = new PushCommandRegularParamEntity();
        pushCommandRegularParamEntity.setCreateEmp("admin");
        pushCommandRegularParamEntity.setUpdateEmp("admin");
        pushCommandRegularParamEntity.setCreateTime(new Date());
        pushCommandRegularParamEntity.setModelNumber("HillstoneStoneOS");
        pushCommandRegularParamEntity.setPromptErrorInfo("{\"Error\":\"错误\"}");
        pushCommandRegularParamEntity.setPromptRegCommand("%");
        pushCommandRegularParamEntity.setUpdateTime(new Date());
        pushCommandRegularParamEntity.setType("0");
        pushCommandRegularParamEntity.setVendorName("huawei");
        int count = pushCommandRegularParamService.addCommandRegularParam(pushCommandRegularParamEntity);
        Assert.assertEquals(1, count);
    }

    @Test
    public void deleteCommandRegParamById() {

        int count = pushCommandRegularParamService.deleteCommandRegParamById("1,6");
        Assert.assertEquals(2, count);
    }

    @Test
    public void updateCommandRegularParamById() {
        CommandRegularUpdateDTO commandRegularUpdateDTO = new CommandRegularUpdateDTO();
        commandRegularUpdateDTO.setId(2);
        commandRegularUpdateDTO.setUpdateEmp("super");
        commandRegularUpdateDTO.setModelNumber("USG6100");
        commandRegularUpdateDTO.setPromptRegCommand("#");
        int count = pushCommandRegularParamService.updateCommandRegularParamById(commandRegularUpdateDTO);
        Assert.assertEquals(1, count);
    }

    @Test
    public void getCommandRegularParamList() {
        CommandRegularParamPageDTO pushCommandRegularParamDTO = new CommandRegularParamPageDTO();
        pushCommandRegularParamDTO.setPageSize(10);
        pushCommandRegularParamDTO.setCurrentPage(1);
//        pushCommandRegularParamDTO.setType("0");
        pushCommandRegularParamDTO.setUpdateEmp("super");
//        pushCommandRegularParamDTO.setSearchName("k");
        PageInfo<PushCommandRegularParamEntity> pushCommandRegularParamEntityPageInfo = pushCommandRegularParamService.getCommandRegularParamList(pushCommandRegularParamDTO);
        Assert.assertNotNull(pushCommandRegularParamEntityPageInfo);
    }

    @Test
    public void testFortress(){
        String zy = regularParamForMatchService.python2Fortress("1.1.1.1", "zy");
    }
}
