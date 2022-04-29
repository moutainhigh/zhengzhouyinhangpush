package com.abtnetworks.totems.common.executor;

import com.abtnetworks.totems.common.config.ThreadPoolExecutorConfig;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author hw
 * @Description
 * @Date 17:16 2019/3/26
 */
@Component
public class ExtendedExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ExtendedExecutor.class);

    private final static String stop = "stop";

    @Autowired
    private ApplicationContext context;

    /**
     * 获取所有线程信息
     * @return
     */
    public static Map<String, ExecutorDto> getInfoMap(){
        return ThreadPoolExecutorConfig.INFO_MAP;
    }

    /**
     * 判断是否存在相同的线程id
     * @param taskId
     * @return true：存在相同的线程id，或者线程id为空，请勿执行execute。 false：正常执行
     */
    public static boolean containsKey(String taskId) {
        if (AliStringUtils.isEmpty(taskId)){
            logger.error("ExtendedExecutor containsKey：线程Id为空，请命名启动线程的id！");
            return true;
        }
        if (ThreadPoolExecutorConfig.RUNNING_MAP.containsKey(taskId)
                && ThreadPoolExecutorConfig.INFO_MAP.containsKey(taskId)){
            logger.error("ExtendedExecutor containsKey：线程Id重复，请勿重复提交相同的线程id！");
            return true;
        }
        return false;
    }

    /**
     * 终止线程池中的某一个线程任务
     * @param taskId taskId任务id
     * @param stopType stop：thread.stop(); 不建议。interrupt：thread.interrupt(); 异常标识逻辑中断
     * @return
     */
    public static boolean stop(String taskId, String stopType) {
        if (AliStringUtils.isEmpty(taskId)){
            return false;
        }
        if (ThreadPoolExecutorConfig.RUNNING_MAP.containsKey(taskId) && ThreadPoolExecutorConfig.INFO_MAP.containsKey(taskId)){
            Thread thread = ThreadPoolExecutorConfig.RUNNING_MAP.get(taskId);
            ExecutorDto dto = ThreadPoolExecutorConfig.INFO_MAP.get(taskId);

            if (stop.equalsIgnoreCase(stopType)){
                thread.stop();
            }else {
                thread.interrupt();
            }
            ThreadPoolExecutorConfig.RUNNING_MAP.remove(taskId);
            ThreadPoolExecutorConfig.INFO_MAP.remove(taskId);
            logger.info("线程池中线程taskId成功："+dto.toString()+"，stopType:"+stopType);
            return true;
        }else {
            return false;
        }

    }
}
