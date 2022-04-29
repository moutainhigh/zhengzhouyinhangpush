package com.abtnetworks.totems.recommend.dto.global;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class PlanServiceVO implements Comparable<PlanServiceVO>{
    private String srcPort;
    private String dstPort;
    private String protocol;

    @Override
    public int compareTo(PlanServiceVO o) {
        //其他类型的协议没有protocol和l4_protocol
        if(StringUtils.isEmpty(protocol)){
            return -1;
        }
        if(StringUtils.isEmpty(o.getProtocol())){
            return 1;
        }
        return (protocol+srcPort+dstPort).compareTo(o.getProtocol()+o.getSrcPort()+o.getDstPort());
    }
}
