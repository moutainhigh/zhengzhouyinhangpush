package com.abtnetworks.totems.common.config;

import com.abtnetworks.totems.common.dto.JumpMachineDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lifei
 * @desc 跳板机配置
 * @date 2021/12/7 10:40
 */
@Configuration
public class JumpMachineConfig {

    @Value("${jump_machine_a_ip:''}")
    private String machineAIp;

    @Value("${jump_machine_a_port:''}")
    private String machineAPort;

    @Value("${jump_machine_a_username:''}")
    private String machineAUserName;

    @Value("${jump_machine_a_password:''}")
    private String machineAPassword;


    @Value("${jump_machine_b_ip:''}")
    private String machineBIp;

    @Value("${jump_machine_b_port:''}")
    private String machineBPort;

    @Value("${jump_machine_b_username:''}")
    private String machineBUserName;

    @Value("${jump_machine_b_password:''}")
    private String machineBPassword;

    @Value("${local_machine_port:''}")
    private String localPort;

    @Bean
    public JumpMachineDTO  getMachineA(){
        JumpMachineDTO jumpMachineADTO = new JumpMachineDTO();
        jumpMachineADTO.setIp(machineAIp);
        jumpMachineADTO.setPort(machineAPort);
        jumpMachineADTO.setUsername(machineAUserName);
        jumpMachineADTO.setPassword(machineAPassword);
        jumpMachineADTO.setIPort(localPort);
        return jumpMachineADTO;
    }

    @Bean
    public JumpMachineDTO  getMachineB(){
        JumpMachineDTO jumpMachineBDTO = new JumpMachineDTO();
        jumpMachineBDTO.setIp(machineBIp);
        jumpMachineBDTO.setPort(machineBPort);
        jumpMachineBDTO.setUsername(machineBUserName);
        jumpMachineBDTO.setPassword(machineBPassword);
        jumpMachineBDTO.setIPort(localPort);
        return jumpMachineBDTO;
    }


}
