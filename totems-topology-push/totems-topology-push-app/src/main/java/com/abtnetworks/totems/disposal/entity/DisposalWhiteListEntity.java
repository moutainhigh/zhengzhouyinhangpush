package com.abtnetworks.totems.disposal.entity;

import com.abtnetworks.totems.disposal.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 白名单管理
 * Created by hw on '2019-11-11 16:43:02'.
 */
@Data
public class DisposalWhiteListEntity extends BaseEntity {

    private static final long serialVersionUID = 3851505337922946668L;

    /**
     * 主键id，自增
     */
    private Long id;

    /**
     * UUID
     */
    private String uuid;

    /**
     * 白名单名称
     */
    private String name;

    /**
     * 类型：0策略、1路由
     */
    private Integer type;

    /**
     * 源IP，可填多个，逗号隔开
     */
    private String srcIp;

    /**
     * 目的地址，可填多个，逗号隔开
     */
    private String dstIp;

    /**
     * 服务组
     */
    private String serviceList;

    /**
     * 路由IP，可填多个，逗号隔开
     */
    private String routingIp;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 逻辑删除标识
     */
    private Boolean deleted;

    /**
     * 创建人员
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建时间  字符串格式
     */
    private String createTimeDesc;

    /**
     * 修改人员
     */
    private String modifiedUser;

    /**
     * 修改时间
     */
    private Date modifiedTime;

    /**IP类型**/
    private boolean ipv6;

}