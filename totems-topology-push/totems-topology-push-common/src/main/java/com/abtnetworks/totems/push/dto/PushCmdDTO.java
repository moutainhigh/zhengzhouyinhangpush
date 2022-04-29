package com.abtnetworks.totems.push.dto;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.issued.dto.MoveParamDTO;
import com.abtnetworks.totems.issued.dto.PythonPushDTO;
import com.abtnetworks.totems.issued.dto.RecommendTask2IssuedDTO;
import com.abtnetworks.totems.issued.dto.SpecialParamDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/***
 *
 * @author zhangyang
 */
@Data
@ApiModel("下发入口时需要的参数")
public class PushCmdDTO {
    @ApiModelProperty("设备类型枚举")
    DeviceModelNumberEnum deviceModelNumberEnum;
    @ApiModelProperty("设备名称")
    String deviceName;
    @ApiModelProperty("设备下发的命令行")
    String commandline;
    @ApiModelProperty("设备登陆用户")
    String username;
    @ApiModelProperty("设备登陆密码")
    String password;
    @ApiModelProperty("设备登陆二次验证用户")
    String enableUsername;
    @ApiModelProperty("设备登陆二次验证密码")
    String enablePassword;
    @ApiModelProperty("设备ip")
    String deviceManagerIp;
    @ApiModelProperty("设备连接端口")
    Integer port;
    @ApiModelProperty("用户是否编辑")
    boolean userEdit;
    @ApiModelProperty("连接设备的协议类型  如ssh和telnet")
    String executorType;

    @ApiModelProperty("编码格式")
    String charset;
    @ApiModelProperty("支持移动策略")
    MoveParamDTO moveParamDTO;
    @ApiModelProperty("策略标识（可以是名称等）")
    String policyFlag;
    @ApiModelProperty("是否回滚")
    Boolean revert ;
    @ApiModelProperty("是否虚设备")
    Boolean isVSys ;
    @ApiModelProperty("虚墙名称")
    String vSysName;
    @ApiModelProperty("下发特例所需参数放对象里")
    SpecialParamDTO specialParamDTO;
    @ApiModelProperty("下发里的随机数记录下发回显map中的key")
    String randomKey;
    @ApiModelProperty("将工单表中有用的信息放在这里给下发用")
    RecommendTask2IssuedDTO recommendTask2IssuedDTO;

    @ApiModelProperty("封禁下发特殊处理")
    PushForbidDTO pushForbidDTO;

    @ApiModelProperty("下发策略类型")
    Integer taskType;
	
	@ApiModelProperty("飞塔管理平台API下发回显")
    private String fortinetPlatformEcho;



    @ApiModelProperty("前置命令行,目前用到查询主备信息")
    String queryBeforeCommandLine;

    @ApiModelProperty("前置命令行,目前用到查询主备信息")
    Boolean haveQueryActive;

    @ApiModelProperty("是否是主 true:主,false:备")
    Boolean isMaster;

    @ApiModelProperty("前置命令执行步骤,目前用到查询主备信息，前面备用设备执行的命令")
    String beforeCmdEcho;


    @ApiModelProperty("合并的字段属性")
    private Integer mergeProperty;
    @ApiModelProperty("凭据名称")
    private String credentialName;
    @ApiModelProperty("回滚命令行 用于天融信下发的时候去回滚命令行里面取已经拼好的show policy id的命令行")
    private String commandlineRevert;

    @ApiModelProperty("间隔时间")
    private Integer interval;

    @ApiModelProperty("飞塔特殊下发策略id")
    List<String> policyIdList = new ArrayList<>();

    @ApiModelProperty("执行验证逻辑标识")
    Boolean verifyFlag = false;

    @ApiModelProperty("py下发DTO")
    PythonPushDTO pythonPushDTO = new PythonPushDTO();

    @ApiModelProperty("是否需要判断主备")
    Boolean needJudgeStandby = false;

    @ApiModelProperty("是否通过py下发")
    Boolean pushByPython = false;
}
