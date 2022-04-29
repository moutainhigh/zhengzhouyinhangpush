package com.abtnetworks.totems.generate.manager;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ProcedureDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;

/**
 * 根据设备类型获取不同的设备相关信息
 */
public interface VendorManager {

    /**
     * 获取设备相关信息
     * @param cmdDTO
     */
    void getVendorInfo(CmdDTO cmdDTO);

    /**
     * 获取设备命令行生成器
     * @param policyType 策略类型
     * @param device 设备信息
     * @param procedure 流程数据
     */
    void getGenerator(PolicyEnum policyType, DeviceDTO device, ProcedureDTO procedure);

    /**
     * 该设备是否对IP地址进行拆解
     * @param modelNumber 设备类型
     * @return true:拆解 false:不拆解
     */
    boolean isDisassembleIps(DeviceModelNumberEnum modelNumber);

    /**
     * 获取地址复用解析对象
     * @param modelNumber 设备类型
     * @return SubServiceEnum枚举code
     */
    Integer getSearchAddressObject(DeviceModelNumberEnum modelNumber);

    /**
     * 获取服务复用解析对象
     * @param modelNumber 设备类型
     * @return SubServiceEnum枚举code
     */
    Integer getSearchExistServiceProcedure(DeviceModelNumberEnum modelNumber);
}
