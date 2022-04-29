package com.abtnetworks.totems.push.service;

import com.abtnetworks.totems.push.vo.CheckRelevancyNatOrderVO;
import com.abtnetworks.totems.push.vo.FivePushInfoVo;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/2/24
 */
public interface PushBussExtendService {
    /**
     * 检查下发工单是否关联nat策略
     * @param ids
     * @return
     */
    List<CheckRelevancyNatOrderVO> checkRelevancyNatOrder(String ids);


    /**
     * 查询pool池名称和snatPool池名称(F5负载均衡设备)
     * @param deviceUuid
     * @return
     */
    FivePushInfoVo queryPoolNameForFive(String deviceUuid);


    /**
     * 查询pool池名称和snatPool池名称(F5负载均衡设备)
     * @param deviceUuid
     * @return
     */
    FivePushInfoVo queryProfileName(String deviceUuid);

    /**
     * 查询路由名称(静态路由设备)
     * @param deviceUuid
     * @return
     */
    List<String> queryRouteNames(String deviceUuid);

    /**
     * 查询接口名称(静态路由设备)
     * @param deviceUuid
     * @return
     */
    List<String> queryInterfaceNames(String deviceUuid);
}
