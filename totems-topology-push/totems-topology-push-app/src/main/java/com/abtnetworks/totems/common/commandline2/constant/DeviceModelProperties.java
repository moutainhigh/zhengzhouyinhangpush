package com.abtnetworks.totems.common.commandline2.constant;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zc
 * @date 2020/01/02
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "device-model")
public class DeviceModelProperties {

    private List<CommandTemplate> modelList;

    private Map<String, CommandTemplate> modelMap;

    public void setModelList(List<CommandTemplate> modelList) {
        this.modelList = modelList;
    }

    public List<CommandTemplate> getModelList() {
        return Collections.unmodifiableList(this.modelList);
    }

    public Map<String, CommandTemplate> getModelMap() {
        if (this.modelMap == null) {
            log.debug("初始化modelMap");
            Map<String, CommandTemplate> map = this.modelList.stream()
                    .collect(Collectors.toMap(CommandTemplate::getModelNumber, s -> s));
            this.modelMap = Collections.unmodifiableMap(map);
        }
        return this.modelMap;
    }

    /**
     * 命令行字符串
     */
    @Data
    public static class CommandTemplate {

        /**
         * 设备型号的名字
         */
        private String modelNumber;

        /**
         * 前置命令行
         */
        private String preCommand;

        /**
         * 前置虚设备命令行
         */
        private String preVsysCommand;

        /**
         * 后置命令行
         */
        private String postCommand;

        private AddressCommand addressCommand;

        private RouterCommand routerCommand;

        private ServiceCommand serviceCommand;

        private TimeCommand timeCommand;

        private String securityPolicyCommand;

    }

    /**
     * （ipv4、ipv6）的（创建、删除）命令行
     */
    @Data
    public static class RouterCommand {
        private String ipv4Create;
        private String ipv6Create;
        private String ipv4Delete;
        private String ipv6Delete;
    }

    /**
     * 地址对象
     */
    @Data
    public static class AddressCommand {
        private String ipv4Host;
        private String ipv4Mask;
        private String ipv4Range;
        private String addressGroup;
    }

    /**
     * 时间对象
     */
    @Data
    public static class ServiceCommand {
        private String tcpPortRange;
        private String udpPortRange;
        private String icmpPortRange;
        private String serviceGroup;
    }

    /**
     * 时间对象
     */
    @Data
    public static class TimeCommand {
        /**
         * 绝对时间范围的命令行
         */
        private String absoluteRange;
        /**
         * 绝对时间范围的时间的格式
         */
        private String absoluteTimeFormat;
    }
}
