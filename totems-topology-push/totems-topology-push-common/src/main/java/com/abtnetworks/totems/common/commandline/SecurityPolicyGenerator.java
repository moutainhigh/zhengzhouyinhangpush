package com.abtnetworks.totems.common.commandline;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.utils.PortUtils;
import com.abtnetworks.totems.common.utils.ProtocolUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/4 17:34
 */
public abstract class SecurityPolicyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecurityPolicyGenerator.class);


    public abstract String generatePreCommandline(CommandlineDTO dto);

    public abstract String generateCommandline(CommandlineDTO dto);

    public abstract String generatePostCommandline(CommandlineDTO dto);

    

    public final int MAX_NAME_LENGTH = 24;

    public boolean USE_RAW_NAME = false;

    /**
     * 生成策略命令行的算法
     * @param dto
     * @return
     */
    public final String composite(CommandlineDTO dto){
        StringBuilder sb = new StringBuilder();

        String preCmd = generatePreCommandline(dto);
        sb.append(preCmd);

        String cmd = generateCommandline(dto);
        sb.append(cmd);

        String postCmd = generatePostCommandline(dto);
        sb.append(postCmd);

        return sb.toString();
    }


    private String getStartLineNumber(String lineNumberString) {
        lineNumberString = lineNumberString.replace("[", "");
        lineNumberString = lineNumberString.replace("]", "");
        String[] arr = lineNumberString.split("-");
        return arr[0].trim();
    }
    /**
     * 包含引号
     * @param objectName
     * @return
     */
    protected String containsQuotes(String objectName){
        if(StringUtils.isNotEmpty(objectName) && objectName.contains(" ")){
            objectName = "\""+objectName+"\"";
        }
        return objectName;
    }


    /**
     * 判断传进来的字符串，是否
     * 大于指定的字节，如果大于递归调用
     * 直到小于指定字节数 ，一定要指定字符编码，因为各个系统字符编码都不一样，字节数也不一样
     * @param s
     *            原始字符串
     * @param num
     *            传进来指定字节数
     * @return String 截取后的字符串

     * @throws
     */
    protected static String strSub(String s, int num, String charsetName){
        int len = 0;
        try{
            len = s.getBytes(charsetName).length;
        }catch (Exception e) {
            logger.error("字符串长度计算异常");
        }

        if (len > num) {
            s = s.substring(0, s.length() - 1);
            s = strSub(s, num, charsetName);
        }
        return s;
    }


    /**获取服务名称***/
    public String getServiceName(List<ServiceDTO> serviceDTOList){
        StringBuilder nameSb = new StringBuilder();
        int number = 0;
        for (ServiceDTO dto : serviceDTOList) {
            if (number != 0) {
                nameSb.append("_");
            }
            nameSb.append(getServiceName(dto));
            number++;
        }

        String name = nameSb.toString();
        if(name.length() > getMaxNameLength()) {
            String shortName = name.substring(0, getMaxNameLength()-4);
            name = String.format("%s_etc", shortName.substring(0, shortName.lastIndexOf("_")));
        }
        return name;
    }


    public String getServiceName(ServiceDTO dto) {
        StringBuilder sb = new StringBuilder();
        String protocolString = ProtocolUtils.getProtocolByValue(Integer.valueOf(dto.getProtocol()));
        sb.append(protocolString.toLowerCase());
        if (StringUtils.isBlank(dto.getDstPorts())) {
            return sb.toString();
        }
        if(protocolString.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ICMP)){
            return sb.toString();
        }
        if(dto.getDstPorts().equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY) || dto.getDstPorts().equals(PolicyConstants.PORT_ANY)){
            return sb.toString();
        }
        String[] dstPorts = dto.getDstPorts().split(",");
        for (String dstPort : dstPorts) {
            if (PortUtils.isPortRange(dstPort)) {
                String startPort = PortUtils.getStartPort(dstPort);
                String endPort = PortUtils.getEndPort(dstPort);
                sb.append(String.format("_%s_%s", startPort, endPort));
            } else {
                sb.append(String.format("_%s", dstPort));
            }
        }
        return sb.toString().toLowerCase();
    }

    public String getServiceNameByOne(String protocolString, String dstPort) {
        if(dstPort.equalsIgnoreCase(PolicyConstants.POLICY_STR_VALUE_ANY)){
            return protocolString;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s_%s", protocolString, dstPort));
        return sb.toString().toLowerCase();
    }

    public int getMaxNameLength() {
        return MAX_NAME_LENGTH;
    }

    /**
     * 记录创建对象的名称
     * 
     * @param srcAddressObject
     * @param dstAddressObject
     */
    protected void recordCreateAddrAndServiceObjectName(CommandlineDTO dto,PolicyObjectDTO srcAddressObject,PolicyObjectDTO dstAddressObject
            ,PolicyObjectDTO postSrcAddressObject,PolicyObjectDTO postDstAddressObject) {
        List<String> createAddressObjectNames = new ArrayList<>();
        List<String> createAddressGroupObjectNames = new ArrayList<>();
        List<String> createSrcAddressObjectName = null, createSrcAddressGroupObjectName = null,
                createDstAddressObjectName = null, createDstAddressGroupObjectName = null, createPostSrcAddressObjectName = null,
                createPostSrcAddressGroupObjectName = null, createPostDstAddressObjectName = null, createPostDstAddressGroupObjectName = null;

        if (null != srcAddressObject) {
            createSrcAddressObjectName = srcAddressObject.getCreateObjectName();
            createSrcAddressGroupObjectName = srcAddressObject.getCreateGroupObjectName();
        }
        if (null != dstAddressObject) {
            createDstAddressObjectName = dstAddressObject.getCreateObjectName();
            createDstAddressGroupObjectName = dstAddressObject.getCreateGroupObjectName();
        }
        if (null != postSrcAddressObject) {
            createPostSrcAddressObjectName = postSrcAddressObject.getCreateObjectName();
            createPostSrcAddressGroupObjectName = postSrcAddressObject.getCreateGroupObjectName();
        }
        if (null != postDstAddressObject) {
            createPostDstAddressObjectName = postDstAddressObject.getCreateObjectName();
            createPostDstAddressGroupObjectName = postDstAddressObject.getCreateGroupObjectName();
        }


        if(CollectionUtils.isNotEmpty(createSrcAddressObjectName)){
            createAddressObjectNames.addAll(createSrcAddressObjectName);
        }
        if(CollectionUtils.isNotEmpty(createDstAddressObjectName)){
            createAddressObjectNames.addAll(createDstAddressObjectName);
        }
        if(CollectionUtils.isNotEmpty(createPostSrcAddressObjectName)){
            createAddressObjectNames.addAll(createPostSrcAddressObjectName);
        }
        if(CollectionUtils.isNotEmpty(createPostDstAddressObjectName)){
            createAddressObjectNames.addAll(createPostDstAddressObjectName);
        }


        if(CollectionUtils.isNotEmpty(createSrcAddressGroupObjectName)){
            createAddressGroupObjectNames.addAll(createSrcAddressGroupObjectName);
        }
        if(CollectionUtils.isNotEmpty(createDstAddressGroupObjectName)){
            createAddressGroupObjectNames.addAll(createDstAddressGroupObjectName);
        }
        if(CollectionUtils.isNotEmpty(createPostSrcAddressGroupObjectName)){
            createAddressGroupObjectNames.addAll(createPostSrcAddressGroupObjectName);
        }
        if(CollectionUtils.isNotEmpty(createPostDstAddressGroupObjectName)){
            createAddressGroupObjectNames.addAll(createPostDstAddressGroupObjectName);
        }

        dto.setAddressObjectNameList(createAddressObjectNames);
        dto.setAddressObjectGroupNameList(createAddressGroupObjectNames);
    }


    /**
     * 记录创建服务对象名称
     *
     * @param dto
     * @param serviceObject
     */
    protected void recordCreateServiceObjectNames(CommandlineDTO dto, PolicyObjectDTO serviceObject) {
        if (null == serviceObject) {
            return;
        }
        List<String> createServiceObjectName = serviceObject.getCreateServiceObjectName();
        List<String> createServiceGroupObjectName = serviceObject.getCreateServiceGroupObjectNames();
        if (CollectionUtils.isNotEmpty(createServiceObjectName)) {
            dto.setServiceObjectNameList(createServiceObjectName);
        }
        if (CollectionUtils.isNotEmpty(createServiceGroupObjectName)) {
            dto.setServiceObjectGroupNameList(createServiceGroupObjectName);
        }
    }


    /**
     * 记录时间对象的名称
     *
     * @param dto
     * @param timeObject
     */
    protected void recordCreateTimeObjectName(CommandlineDTO dto, PolicyObjectDTO timeObject) {
        List<String> timeObjectNameList = new ArrayList<>();

        if (null != timeObject && StringUtils.isNotBlank(timeObject.getName())) {
            timeObjectNameList.add(timeObject.getName());
        }
        dto.setTimeObjectNameList(timeObjectNameList);
    }


}
