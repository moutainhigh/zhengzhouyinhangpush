package com.abtnetworks.totems.recommend.dto.push;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/7/21
 */
public class GenerateSimulationCmdDTO {

    private String policyCommand;

    private String simulationCommand;

    public String getPolicyCommand() {
        return policyCommand;
    }

    public void setPolicyCommand(String policyCommand) {
        this.policyCommand = policyCommand;
    }

    public String getSimulationCommand() {
        return simulationCommand;
    }

    public void setSimulationCommand(String simulationCommand) {
        this.simulationCommand = simulationCommand;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
