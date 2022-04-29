package com.abtnetworks.totems.common.dto.generate;

import lombok.Data;

import java.util.Set;


/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/5/13
 */
@Data
public class PolicyServiceTupleDTO {

    private Set<String> dstPortSet;


    private Set<String> protocolSet;

    public PolicyServiceTupleDTO() {

    }

    @Override
    public int hashCode() {
        byte byteNum = 1;
        int result = 31 * byteNum + (this.dstPortSet == null ? 0 : this.dstPortSet.hashCode());
        result = 31 * result + (this.protocolSet == null ? 0 : this.protocolSet.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        PolicyServiceTupleDTO serviceTuple = (PolicyServiceTupleDTO) obj;

        //目的端口
        if (this.dstPortSet == null) {
            if (serviceTuple.dstPortSet != null) {
                return false;
            }
        } else if (!this.dstPortSet.equals(serviceTuple.dstPortSet)) {
            return false;
        }

        //协议
        if (this.protocolSet == null) {
            if (serviceTuple.protocolSet != null) {
                return false;
            }
        } else if (!this.protocolSet.equals(serviceTuple.protocolSet)) {
            return false;
        }


        return true;
    }
}
