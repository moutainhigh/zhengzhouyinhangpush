package com.abtnetworks.totems.common.commandline.nat;


import com.abtnetworks.totems.common.commandline.NatPolicyFactory;
import com.abtnetworks.totems.common.commandline.NatPolicyGenerator;
import com.abtnetworks.totems.common.dto.commandline.StaticNatTaskDTO;
import org.junit.Before;
import org.junit.Test;

public class Cisco {

    NatPolicyGenerator generator;

    @Before
    public void createGeneator() {
        NatPolicyFactory factory = new NatPolicyFactory();
        generator = factory.natPolicyFactory("Cisco ASA");
    }

    @Test
    public void generateStaticNatCommandLine() {
        System.out.println("test generateStaticNatCommandLine");

        StaticNatTaskDTO task = new StaticNatTaskDTO();

        generator.generateStaticNatCommandLine(task);
    }

    void generateSNatCommandLine() {
    }

    void generateDNatCommandLine() {
    }

    void generateBothNatCommandLine() {
    }
}