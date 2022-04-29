package com.abtnetworks.totems.push;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.common.tools.queue.PushBlockingQueueTool;
import com.abtnetworks.totems.common.tools.vo.PushQueueVo;
import com.abtnetworks.totems.push.dto.CommandTaskDTO;
import com.abtnetworks.totems.push.service.task.PushTaskService;
import com.abtnetworks.totems.recommend.dao.mysql.RecommendTaskMapper;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/6/16
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class PushTaskServiceImplTest {
    @Resource
    PushTaskService pushTaskService;

    @Autowired
    private RecommendTaskMapper recommendTaskMapper;

    @Autowired
    private PushBlockingQueueTool queueTool;

    @Test
    public void addCommandTaskList() {
        List<CommandTaskDTO> taskList =  new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            CommandTaskDTO commandTaskDTO = new CommandTaskDTO();
        }
//        pushTaskService.addCommandTaskList(taskList);
    }
    public static void main(String[] args) throws UnsupportedEncodingException {

        String aaa = Charset.defaultCharset().name();
        System.out.println(aaa);
        String text = "??????杈??ワ?瀵硅薄绫诲????璇???娌℃???惧?版?ゅ?硅薄";

        System.out.println(text);

        String gbk_text = new String(text.getBytes("GBK"), "UTF-8");
        System.out.println(gbk_text);

        String utf_text = new String(gbk_text.getBytes("UTF-8"), "GB2312");
        System.out.println(utf_text);

    }

    @Test
    public void query() {
        Map<String, Object> params = new HashMap<>();
        params.put("taskIds", null);
        params.put("id", "470");
        params.put("branchLevel","00%");
        List<RecommendTaskEntity> list = recommendTaskMapper.searchNatTask(params);
        System.out.println(list);

    }

    @Test
    public void newPush() throws Exception {

        PushQueueVo pushQueueVo = new PushQueueVo();
        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        ids.add(2);
        pushQueueVo.setTaskIds(ids);
        queueTool.addQueue(pushQueueVo);

        PushQueueVo pushQueueVo2 = new PushQueueVo();
        List<Integer> ids2 = new ArrayList<>();
        ids2.add(4);
        pushQueueVo2.setTaskIds(ids2);
        queueTool.addQueue(pushQueueVo2);

        queueTool.addQueue(pushQueueVo2);
        queueTool.addQueue(pushQueueVo2);
        System.out.println(queueTool.getQueueSize());
        queueTool.execute();

        List<PushQueueVo>  tools = queueTool.getAllDataFromQueue();
        System.out.println(queueTool.getQueueSize());
        queueTool.addQueue(pushQueueVo2);

    }

}
