package com.abtnetworks.totems.push.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author zc
 * @date 2019/05/08
 */
@Entity
//@Table(name = "mail_server_conf")
@Data
@ApiModel("邮件服务器配置信息")
public class MailServerConfDTO implements Serializable {
    private static final long serialVersionUID = 8624835393816027108L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @ApiModelProperty("smtp服务器，例：smtp.qq.com")
    private String smtpName;

    @ApiModelProperty("发件人账号")
    private String userName;

    @JsonIgnore
    @ApiModelProperty("发件人密码")
    private String password;

    @ApiModelProperty("端口")
    private Integer port;

    @ApiModelProperty("邮件服务器是否启用ssl验证")
    private Boolean mailSsl;
}
