package com.abtnetworks.totems.common.enums;

import com.abtnetworks.totems.common.atomcommandline.bothnat.AtomBothNatF5;
import com.abtnetworks.totems.common.atomcommandline.bothnat.AtomBothVenustechVSOS;
import com.abtnetworks.totems.common.atomcommandline.dnat.*;
import com.abtnetworks.totems.common.atomcommandline.dnat.AtomDnatF5;
import com.abtnetworks.totems.common.atomcommandline.dnat.AtomDnatLeadSecPowerV30;
import com.abtnetworks.totems.common.atomcommandline.dnat.AtomDnatLongMa;
import com.abtnetworks.totems.common.atomcommandline.edit.*;
import com.abtnetworks.totems.common.atomcommandline.rollback.AtomRollbackSecurityH3cV7Op;
import com.abtnetworks.totems.common.atomcommandline.rollback.nat.AtomRollbackBothNatHillStone;
import com.abtnetworks.totems.common.atomcommandline.rollback.nat.AtomRollbackDstNatHillStone;
import com.abtnetworks.totems.common.atomcommandline.rollback.nat.AtomRollbackSrcNatHillStone;
import com.abtnetworks.totems.common.atomcommandline.routing.AtomRoutingLongMa;
import com.abtnetworks.totems.common.atomcommandline.security.*;
import com.abtnetworks.totems.common.atomcommandline.snat.AtomSnatLeadSecPowerV30;
import com.abtnetworks.totems.common.atomcommandline.snat.AtomSnatLongMa;
import com.abtnetworks.totems.common.atomcommandline.snat.AtomSnatVenustechVSOS;
import com.abtnetworks.totems.common.atomcommandline.staticnat.AtomStaticnatFortinetiOS;
import com.abtnetworks.totems.common.atomcommandline.staticnat.AtomStaticnatLeadSecPowerV30;
import com.abtnetworks.totems.common.atomcommandline.staticnat.AtomStaticnatLonaMa;
import com.abtnetworks.totems.common.atomcommandline.staticnat.AtomStaticnatVenustechVSOS;
import com.abtnetworks.totems.common.commandline.acl.AclCiscoIos;
import com.abtnetworks.totems.common.commandline.acl.AclH3cSecPathV7;
import com.abtnetworks.totems.common.commandline.acl.AclJuniper;
import com.abtnetworks.totems.common.commandline.acl.AclRuijie;
import com.abtnetworks.totems.common.commandline.bothnat.*;
import com.abtnetworks.totems.common.commandline.disable.DisableSecurityHillStoneR5;
import com.abtnetworks.totems.common.commandline.disable.DisableSecurityUsg6000;
import com.abtnetworks.totems.common.commandline.dnat.*;
import com.abtnetworks.totems.common.commandline.edit.*;
import com.abtnetworks.totems.common.commandline.editforbid.EditForbidHillStoneR5;
import com.abtnetworks.totems.common.commandline.editforbid.EditForbidUsg6000;
import com.abtnetworks.totems.common.commandline.enable.EnableSecurityHillStoneR5;
import com.abtnetworks.totems.common.commandline.enable.EnableSecurityUsg6000;
import com.abtnetworks.totems.common.commandline.forbid.ForbidHillStoneR5;
import com.abtnetworks.totems.common.commandline.rollback.*;
import com.abtnetworks.totems.common.commandline.rollback.acl.RollbackAclCiscoIos;
import com.abtnetworks.totems.common.commandline.rollback.acl.RollbackAclJuniper;
import com.abtnetworks.totems.common.commandline.rollback.acl.RollbackAclRuijie;
import com.abtnetworks.totems.common.commandline.rollback.security.RollbackTopsecTos010020;
import com.abtnetworks.totems.common.commandline.rollback.security.RollbackTopsecTosNg;
import com.abtnetworks.totems.common.commandline.routing.*;
import com.abtnetworks.totems.common.commandline.security.*;
import com.abtnetworks.totems.common.commandline.snat.*;
import com.abtnetworks.totems.common.commandline.staticnat.*;
import com.abtnetworks.totems.vender.abt.security.AclAbtImpl;

public  enum DeviceModelNumberEnum {
    CISCO(10, "Cisco ASA", "思科", SecurityCiscoASA.class, null, SnatCiscoASA.class, null, StaticCiscoASA.class, null, RoutingCiscoASA.class, RollbackSecurityCiscoASA.class, RollbackRoutingCiscoASA.class,RollbackSnatCiscoASA.class,null,RollbackStaticNatCiscoASA.class,null,null,null,null,null, EditCisocASA.class,null),

    CISCO_S(11, "CiscoASA", "思科"),
    CISCO_IOS(12, "Cisco IOS", "思科IOS", null, AclCiscoIos.class, null, null, null, null, null,StaticRoutingCiscoIOS.class, null, RollbackRoutingCiscoIOS.class,RollbackAclCiscoIos.class, RollbackStaticRoutingCiscoIOS.class),
    CISCO_NX_OS(13, "Cisco NX-OS", "思科NX-OS", null, AclCiscoIos.class, null, null, null, null, null, null, null,RollbackAclCiscoIos.class),
    CISCO_ASA_86(14, "Cisco ASA 8.4", "思科", SecurityCiscoASA86.class, null, SnatCiscoASA.class, null, StaticCiscoASA.class, null, RoutingCiscoASA.class, RollbackSecurityCiscoASA.class, RollbackRoutingCiscoASA.class,RollbackSnatCiscoASA84.class,null,RollbackStaticNatCiscoASA.class,null,null,null,null,null, EditCisocASA86.class,null),
    CISCO_ASA_99(15, "Cisco ASA 9.9", "思科ASA9.9", SecurityCiscoASA99.class, null, SnatCiscoASA99.class, null, StaticCiscoASA99.class, BothCiscoASA99.class, RoutingCiscoASA.class,StaticRoutingCiscoASA99.class, RollbackSecurityCiscoASA.class, RollbackRoutingCiscoASA.class,RollbackStaticRoutingCiscoASA99.class,null,null,RollbackStaticNatCiscoASA99.class,RollbackBothNatCiscoASA99.class,null,null,null,null,null,null),

    HILLSTONE(20, "HillstoneStoneOS", "山石", SecurityHillStoneR5.class, null, SnatHillstoneR5.class, DnatHillstoneR5.class, null, BothHillstoneR5.class, RoutingHillstone.class, RollbackSecurityHillstoneR5.class, RollbackRoutingHillstone.class, AtomRollbackSrcNatHillStone.class, AtomRollbackDstNatHillStone.class, null, AtomRollbackBothNatHillStone.class, ForbidHillStoneR5.class, EditForbidHillStoneR5.class, DisableSecurityHillStoneR5.class, EnableSecurityHillStoneR5.class,EditHillStone.class,null),
    HILLSTONE_R5(21, "HillStoneR5", "山石R5",SecurityHillStoneR5.class, null, SnatHillstoneR5.class, DnatHillstoneR5.class, null, BothHillstoneR5.class, RoutingHillstone.class, RollbackSecurityHillstoneR5.class, RollbackRoutingHillstone.class, AtomRollbackSrcNatHillStone.class, AtomRollbackDstNatHillStone.class, null, AtomRollbackBothNatHillStone.class, ForbidHillStoneR5.class, EditForbidHillStoneR5.class, DisableSecurityHillStoneR5.class, EnableSecurityHillStoneR5.class, EditHillStone.class,null),
    HILLSTONE_V5(22, "HillstoneStoneOS V5.5", "山石V5.5", SecurityHillStoneV5.class, null, SnatHillstoneV5.class, DnatHillstoneV5.class, null, BothHillstoneV5.class, RoutingHillstone.class,StaticRoutingHillStoneV5.class, RollbackSecurityHillstoneR5.class, RollbackRoutingHillstone.class,RollbackStaticRoutingHillStoneV5.class, AtomRollbackSrcNatHillStone.class, AtomRollbackDstNatHillStone.class, null, AtomRollbackBothNatHillStone.class, ForbidHillStoneR5.class, EditForbidHillStoneR5.class, DisableSecurityHillStoneR5.class, EnableSecurityHillStoneR5.class,null,null),

    ABTNETWORKS_BLIST(25, "abtnetworks blist", "安博通 blist", AtomSecurityAbtBList.class, AclAbtImpl.class,null,null,null,null,null,null,null,null),
    ABTNETWORKS(26, "abtnetworks", "安博通", AtomSecurityAbt.class, null, null, null, null, null, null, RollbackSecurityAbtnetworks.class, null, null ,null, null,null,null, null,null,null,null,null),
    WESTONE(29, "V2.1.5i-s", "龙马卫士", AtomSecurityLongMa.class, null, AtomSnatLongMa.class, AtomDnatLongMa.class, AtomStaticnatLonaMa.class, null,null, AtomRoutingLongMa.class,  RollbackSecurityAbtnetworks.class, null ,RollbackStaticRoutingLongMa.class, RollbackSnatLongMa.class,RollbackDnatLongMa.class,RollbackStaticnatLongMa.class, null,null,null,null,null,null,null),
    ANHENG(27, "anheng", "安恒", AtomSecurityAbt.class, null, null, null, null, null, null, RollbackSecurityAbtnetworks.class, null, null ,null, null,null,null, null,null,null,null,null),
    MAIPUMSGFIREWALL(28, "MaipuMSGFirewall", "迈普", AtomSecurityAbt.class, null, null, null, null, null, null, RollbackSecurityAbtnetworks.class, null, null ,null, null,null,null, null,null,null,null,null),

    TOPSEC_TOS_005(30, "Topsec TOS 005", "天融信", SecurityTopsec.class, null, SnatTopsecTOS005.class, DnatTopsecTOS005.class, StaticTopsec.class, BothTopsecTOS005.class, RoutingTopsec.class, RollbackTopsecTos010020.class, null, RollbackSnatTopsecTos010020.class,RollbackDnatTopsecTos010020.class,null,RollbackBothTopsecTos010020.class,null,null,null,null, EditTopsec005.class,null),
    TOPSEC_TOS_010_020(31, "Topsec TOS 010-020", "天融信", SecurityTopsecTos010020.class, null, SnatTopsecTOS010020.class, DnatTopsecTOS010020.class, StaticTopsec.class, BothTopsecTOS010020.class, RoutingTopsec.class, StaticRoutingTopsec010.class, RollbackTopsecTos010020.class, null,RollbackStaticRoutingTopsec010.class, RollbackSnatTopsecTos010020.class,RollbackDnatTopsecTos010020.class,null,RollbackBothTopsecTos010020.class,null,null,null,null, EditTopsec010.class,null),
    TOPSEC_NG(32, "Topsec NG", "天融信", SecurityTopsecNG.class, null, SnatTopsecNG.class, DnatTopsecNG.class, StaticTopsec.class, BothTopsecNG.class, RoutingTopsec.class, RollbackTopsecTosNg.class, null, RollbackSnatTopsecTos010020.class,RollbackDnatTopsecTos010020.class,null,RollbackBothTopsecTos010020.class,null,null,null,null, EditTopsecNG.class,null),
    TOPSEC_NG2(33, "Topsec NG v3.2242-2294", "天融信", SecurityTopsecNG2.class, null, SnatTopsecNG2.class, DnatTopsecNG2.class, StaticTopsecNg2.class, BothTopsecNG2.class, RoutingTopsec.class, RollbackTopsecTosNg.class, null, RollbackSnatTopsecTos010020.class,RollbackDnatTopsecTos010020.class,null,RollbackBothTopsecTos010020.class,null,null,null,null, EditTopsecNG2.class,null),
    TOPSEC_NG3(34, "Topsec NG v3.2242-2294-name",  "天融信", SecurityTopsecNG3.class, null, SnatTopsecNG2.class, DnatTopsecNG2.class, StaticTopsecNg2.class, BothTopsecNG2.class, RoutingTopsec.class, RollbackSecurityTopsecNG3.class,null,null,null,null,null,null,null,null,null,EditTopsecNG2.class,null),
    TOPSEC_NG4(35, "Topsec NG v3.2073", "天融信", SecurityTopsecNG4.class, null, SnatTopsecNG2.class, DnatTopsecNG2.class, StaticTopsecNg2.class, BothTopsecNG2.class, RoutingTopsec.class, RollbackTopsecTos010020.class, null, null,null,null,null,null,null,null,null,EditTopsecNG4.class,null),

    //    U6000(40, "USG6000", "华为U6000", SecurityUsg6000.class, SnatUsg6000.class, DnatUsg6000.class, StaticUsg6000.class, BothUsg6000.class, RoutingHuaweiUsg6000.class, null, RollbackRoutingHuaweiUsg6000.class),
    DPTECHR004(50, "DPTech Firewall R004", "迪普R004", SecurityDpTechR004.class, null, SnatDpTechR004.class, DnatDpTechR004.class, null, BothDpTechR004.class, RoutingDPTechFirewall.class, RollbackSecurityDpTechR004.class, RollbackRoutingDPTechFirewall.class,null,null,null,null,null,null,null,null, EditDptechr004.class,null),
    DPTECHR003(51, "DPTech Firewall R003", "迪普R003", SecurityDpTechR003.class, null, null, null, null, null, RoutingDPTechFirewall.class, RollbackSecurityDpTechR003.class, RollbackRoutingDPTechFirewall.class,null,null,null,null,null,null,null,null,EditDptechr003.class,null),
    FORTINET(60, "FortinetFortiOS", "飞塔", AtomSecurityFortinetFortiOS.class, null, SnatFortinet.class, DnatFortinet.class, StaticFortinet.class,BothFortinet.class,null, StaticRoutingFortinet.class,RollbackSecurityFotinetForiOS.class, null,RollbackStaticRoutingFortinet.class,RollbackSnatFotineForisOS.class,RollbackDnatFortineForisOS.class,RollbackStaticFortinet.class,RollbackBothFortineForisOS.class,null,null,null,null, EditFortinetFortiOS.class,null),
    FORTINET_V5(61, "FortinetFortiOS V5", "飞塔v5", AtomSecurityFortinetFortiOS.class, null, SnatFortinet.class, DnatFortinet.class,StaticFortinet.class,BothFortinet.class,null, StaticRoutingFortinet.class,RollbackSecurityFotinetForiOS.class, null,RollbackStaticRoutingFortinet.class,RollbackSnatFotineForisOS.class,RollbackDnatFortineForisOS.class,RollbackStaticFortinet.class,RollbackBothFortineForisOS.class,null,null,null,null, EditFortinetFortiOS.class,null),
    FORTINET_V5_2(62, "FortinetFortiOS V5.2", "飞塔v5.2", AtomSecurityFortinetV5.class, null, SnatFortinetV5.class, DnatFortinetV5.class,StaticFortinetV5.class,BothFortinetV5.class,null, StaticRoutingFortinet.class,RollbackSecurityFotinetForiOS.class, null,RollbackStaticRoutingFortinet.class,RollbackSnatFotineForisOS.class,RollbackDnatFortineForisOS.class,RollbackStaticFortinet.class,RollbackBothFortineForisOS.class,null,null,null,null, EditFortinetFortiOS.class,null),
    H3CV5(70, "H3C SecPath V5", "华三v5", SecurityH3cSecPathV5.class, null, null, null, null, null, RoutingH3C.class,RollbackSecurityH3cSecPathV5.class,RoutingH3C.class,null,null,null,null,null,null,null,null, EditH3cSecPathV5.class, null),
    H3CV7(80, "H3C SecPath V7", "华三v7", SecurityH3cSecPathV7.class, AclH3cSecPathV7.class, SnatH3cSecPathV7.class, DnatH3cSecPathV7.class, StaticH3cSecPathV7.class, BothH3cSecPathV7.class, RoutingH3C.class,StaticRoutingH3cSecPathV7.class, RollbackSecurityH3cSecPathV7.class, RollbackRoutingH3C.class,RollbackStaticRoutingH3cSecPathV7.class, RollbackSnatH3cSecPathV7.class,RollbackDnatH3cSecPathV7.class,RollbackStaticH3cSecPathV7.class,RollbackBothH3cSecPathV7.class,null,null,null,null, EditH3cSecPathV7.class,null),
    H3CV7_OP(81, "H3C SecPath V7 OP", "华三v7 op", SecurityH3cSecPathV7OP.class, null, null, null, null, null, RoutingH3C.class, AtomRollbackSecurityH3cV7Op.class, RoutingH3C.class),
    H3CV7_ZONE_PAIR_ACL(84, "H3C SecPath V7 zone-pair ACL", "华三v7 zone-pair ACL", SecurityH3cSecPathV7ZonePair.class, SecurityH3cSecPathV7ZonePair.class, null, null, null, null, null, RollbackSecurityH3cSecPathV7ZonePair.class,null),
    H3C_Comware(82, "H3C Comware", "华三路由交换", null, null, null, null, null, null, RoutingH3C.class, null, RoutingH3C.class, null),
    SRX(90, "JuniperSRX", "Juniper SRX", SecurityJuniperSrx.class, AclJuniper.class, SnatFortinetForJuniper.class,  SnatFortinetForJuniper.class, null,  SnatFortinetForJuniper.class, RoutingJuniperSrx.class,StaticRoutingJuniperSRX.class, RollbackSecurityJuniperSrx.class, RollbackRoutingJuniperSrx.class,RollbackStaticRoutingJuniperSRX.class,null,null,null,null, RollbackAclJuniper.class,null,null,null,null, EditJuniperSrx.class,null),
    SRX_NoCli(91, "JuniperSRX NoCli", "Juniper SRX NoCli", SecurityJuniperSrx.class, AclJuniper.class, null, null, null, null, RoutingJuniperSrx.class,StaticRoutingJuniperSRX.class, RollbackSecurityJuniperSrx.class, RollbackRoutingJuniperSrx.class,RollbackStaticRoutingJuniperSRX.class,null,null,null,null,RollbackAclJuniper.class, null,null,null,null, EditJuniperSrxNoCli.class,null),
    JUNIPER_ROUTER(92, "Juniper Router", "Juniper Router", null, AclJuniper.class, null, null, null, null, null, null, null, null, RollbackAclJuniper.class, null),

    SSG(100, "JuniperSSG", "Juniper SSG", SecurityJuniperSsg.class, null, null, null, null, null, RoutingJuniperSSG.class, null, RollbackRoutingJuniperSSG.class,null,null,null,null,null,null,null,null, EditJuniperSsg.class,null),
    USG2000(110, "USG2000", "华为USG2000", SecurityUsg2100.class, null, null, null, null, null, null, null, null),
    USG6000(111, "USG6000", "华为U6000", SecurityUsg6000.class, null, SnatUsg6000.class, DnatUsg6000.class, StaticUsg6000.class, BothUsg6000.class, RoutingHuaweiUsg6000.class,StaticRoutingUsg6000.class, RollbackSecurityHuaweiUsg6000.class, RollbackRoutingHuaweiUsg6000.class,RollbackStaticRoutingUsg6000.class, RollbackSnatUsg6000.class, RollbackDnatUsg6000.class, RollbackStaticNatUsg6000.class,RollbackBothNatUsg6000.class, SecurityUsg6000.class, EditForbidUsg6000.class, DisableSecurityUsg6000.class, EnableSecurityUsg6000.class, EditUsg6000.class,null),
    USG6000_NO_TOP(112, "USG6000 NoTop", "华为U6000 NoTop", SecurityUsg6000NoTop.class, null, SnatUsg6000.class, DnatUsg6000.class, StaticUsg6000.class, BothUsg6000.class, RoutingHuaweiUsg6000.class, RollbackSecurityHuaweiUsg6000.class, RollbackRoutingHuaweiUsg6000.class, RollbackSnatUsg6000.class, RollbackDnatUsg6000.class,RollbackStaticNatUsg6000.class,RollbackBothNatUsg6000.class, SecurityUsg6000.class, EditForbidUsg6000.class, DisableSecurityUsg6000.class, EnableSecurityUsg6000.class,null,null),
    VENUSTECHVSOS(120, "Venustech VSOS", "启明星辰VSOS", SecurityVenustechVSOS.class, null, AtomSnatVenustechVSOS.class, AtomDnatVenustechVSOS.class, AtomStaticnatVenustechVSOS.class, AtomBothVenustechVSOS.class, null, null, null, null, null, null, null, null ,null, null, null , null,null),
    VENUSTECHVSOS_V263(121, "V2.6.3+", "启明星辰VSOSV2.6.3", SecurityVenustechVSOSV263.class, null, null, null, null, null, null, null, null, null, null, null, null, null ,null, null, null ,EditVenustechVSOSV263.class ,null),
    VENUSTECHPOWERV(122, "Venustech PowerV", "启明星辰PowerV", SecurityVenustechPowerV.class, null, SnatVenustechPowerV.class, DnatVenustechPowerV.class, StaticVenustechPowerV.class, null, null, null, null, null, null, null, null),
    ZTE_ROUTER(130,"ZTERouter","中兴路由", null, null, null, null, null, null, null, null, RollbackRoutingZTERouter.class),
    ZTE_SWITCH(131,"ZTESwitch","中兴交换机"),
    LEGEND_SEC_GATE(140,"Legendsec SecGate","网神SecGate",SecurityLegendSec.class,null, SnatLegendSec.class, DnatLegendSec.class, StaticLegendSec.class, null, null, RollbackSecurityLegendSec.class, null, RollbackSnatLegendSec.class,  RollbackDnatLegendSec.class,RollbackStaticLegendSec.class,null),
    LEGEND_SEC_NSG(141,"Legendsec NSG","网神NSG",SecurityLegendSec.class,null, SnatLegendSec.class, DnatLegendSec.class, StaticLegendSec.class, null, null, RollbackSecurityLegendSec.class, null, RollbackSnatLegendSec.class,  RollbackDnatLegendSec.class,RollbackStaticLegendSec.class,null),
    LEGEND_SEC_NSG_V40(142,"Legendsec NSG 4.0","网神NSG 4.0",SecurityLegendSecV40.class,null, SnatLegendSecV40.class, DnatLegendSecV40.class, StaticLegendSecV40.class, null, null, RollbackSecurityLegendSec.class, null, RollbackSnatLegendSec.class,  RollbackDnatLegendSec.class,RollbackStaticLegendSec.class,null),
    LEAD_SEC_POWER_V(150,"Leadsec PowerV","网御",SecurityLeadSecPowerV.class,null, SnatLeadSecPowerV.class, DnatLeadSecPowerV.class, StaticLeadSecPowerV.class, null, null, null, null, null,  null,null,null),
    LEAD_SEC_POWER_V_30(151,"Leadsec PowerV V3.0","网御V3.0", AtomSecurityLeadSecPowerV30.class,null, AtomSnatLeadSecPowerV30.class, AtomDnatLeadSecPowerV30.class, AtomStaticnatLeadSecPowerV30.class,null,null,null,null),
    CHECK_POINT(160,"checkpoint","checkpoint",SecurityCheckPoint.class,null,null,null,null,null,null,null,null),
    SANG_FOR_IMAGE(170,"Firewall af v8.0.25","深信服v8.0.25",SecuritySangfor.class,null,SnatSangFor.class,DnatSangFor.class,StaticSangFor.class,BothSangFor.class,null,RollbackSecuritySangfor.class,null,RollbackSnatSangfor.class,RollbackDnatSangfor.class,RollbackStaticSangfor.class,RollbackBothNatSangfor.class,null,null,null,null,EditSangfor.class,null),
    PALO_ALTO(180, "Palo Alto Firewall", "paloalto", SecurityPaloAlto.class, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, EditPaloAlto.class, null),
    NET_POWER_JM(190, "NSFW-6000-NF3860-JM", "中科网威", SecurityNetPowerNGFW.class, null, null, null, null, null, null, null, null),
    NET_POWER(191, "NetPower NGFW", "中科网威", SecurityNetPowerNGFW.class, null, null, null, null, null, null, null, null),
    KFW(200, "Kill Firewall", "冠群金辰", SecurityKFW.class, null, SnatKFW.class, DnatKFW.class, null, null, null, RollbackSecurityKFW.class, null),
    DEFAULTMODEL(65535, "Default", "默认设备"),
    NSFOCUS(210,"Nsfocus NF","绿盟",null,null,null,null,null,null,null,null,null),

    SDNWARE(220, "sdnware", "云融", AtomSecurityAbt.class, null, null, null, null, null, null, RollbackSecurityAbtnetworks.class, null, null ,null, null,null,null, null,null,null,null,null),
    F5(230,"F5 BIGIP-LTM","F5", AtomDnatF5.class, AtomBothNatF5.class,RollbackDnatF5.class,RollbackBothNatF5.class),
    RUIJIE(240,"Ruijie RGOS","锐捷", null, AclRuijie.class, null, null, null, null, null, null, null, RollbackAclRuijie.class),
    ;

    private int code;
    private String key;
    private String desc;
    private Class securityClass;
    private Class aclClass;
    private Class snatClass;
    private Class dnatClass;
    private Class staticNatClass;
    private Class bothNatClass;
    private Class routingClass;
    private Class staticRoutingClass;
    private Class rollbackSecurityClass;
    private Class rollbackAclClass;
    private Class rollbackSnatClass;
    private Class rollbackDnatClass;
    private Class rollbackStaticNatClass;
    private Class rollbackBothNatClass;
    private Class rollbakcRoutingClass;
    private Class rollbackStaticRoutingClass;

    /**封禁：大部分厂商与安全策略兼容，小部分不兼容，比如：山石地址对象**/
    private Class forbidClass;

    /**编辑封禁**/
    private Class editForbidClass;

    /** 启用策略 **/
    private Class disableSecurityClass;

    /** 禁用策略 **/
    private Class enableSecurityClass;

    /**
     * 编辑安全策略
     */
    private Class editSecurityClass;

    /**
     * 编辑回滚安全策略
     */
    private Class editRollbackSecurityClass;

    /**
     * 负载均衡DNat策略
     */
    private Class balanceDnatClass;

    /**
     * 负载均衡BothNat策略
     */
    private Class balanceBothNatClass;

    /**
     * 回滚负载均衡DNat策略
     */
    private Class rollbackBalanceDnatClass;


    /**
     * 回滚负载均衡BothNat策略
     */
    private Class rollbackBalanceBothNatClass;


    DeviceModelNumberEnum(int code, String key, String desc) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = null;
        this.aclClass = null;
        this.snatClass = null;
        this.dnatClass = null;
        this.staticNatClass = null;
        this.bothNatClass = null;
        this.routingClass = null;
        this.rollbackSecurityClass = null;
        this.rollbackSnatClass = null;
        this.rollbackDnatClass = null;
        this.rollbackStaticNatClass = null;
        this.rollbackBothNatClass = null;
        this.rollbakcRoutingClass = null;
    }

    DeviceModelNumberEnum(int code, String key, String desc, Class securityClass, Class aclClass, Class snatClass, Class dnatClass, Class staticNatClass,
                          Class bothNatClass, Class routingClass, Class rollbackSecurityClass, Class rollbakcRoutingClass  ) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = securityClass;
        this.aclClass = aclClass;
        this.snatClass = snatClass;
        this.dnatClass = dnatClass;
        this.staticNatClass = staticNatClass;
        this.bothNatClass = bothNatClass;
        this.routingClass = routingClass;
        this.rollbackSecurityClass = rollbackSecurityClass;
        this.rollbakcRoutingClass = rollbakcRoutingClass;

    }

    DeviceModelNumberEnum(int code, String key, String desc, Class securityClass, Class aclClass, Class snatClass, Class dnatClass, Class staticNatClass,
                          Class bothNatClass, Class routingClass, Class rollbackSecurityClass, Class rollbakcRoutingClass , Class rollbackAclClass ) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = securityClass;
        this.aclClass = aclClass;
        this.snatClass = snatClass;
        this.dnatClass = dnatClass;
        this.staticNatClass = staticNatClass;
        this.bothNatClass = bothNatClass;
        this.routingClass = routingClass;
        this.rollbackSecurityClass = rollbackSecurityClass;
        this.rollbakcRoutingClass = rollbakcRoutingClass;
        this.rollbackAclClass = rollbackAclClass;
    }

    DeviceModelNumberEnum(int code, String key, String desc, Class securityClass, Class aclClass, Class snatClass, Class dnatClass, Class staticNatClass,
                          Class bothNatClass, Class routingClass,Class staticRoutingClass, Class rollbackSecurityClass, Class rollbakcRoutingClass , Class rollbackAclClass ,Class rollbackStaticRoutingClass) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = securityClass;
        this.aclClass = aclClass;
        this.snatClass = snatClass;
        this.dnatClass = dnatClass;
        this.staticNatClass = staticNatClass;
        this.bothNatClass = bothNatClass;
        this.routingClass = routingClass;
        this.staticRoutingClass = staticRoutingClass;
        this.rollbackSecurityClass = rollbackSecurityClass;
        this.rollbakcRoutingClass = rollbakcRoutingClass;
        this.rollbackAclClass = rollbackAclClass;
        this.rollbackStaticRoutingClass = rollbackStaticRoutingClass;
    }

    DeviceModelNumberEnum(int code, String key, String desc, Class securityClass, Class aclClass, Class snatClass, Class dnatClass, Class staticNatClass,
                          Class bothNatClass, Class routingClass, Class rollbackSecurityClass, Class rollbakcRoutingClass, Class rollbackSnatClass, Class rollbackDnatClass,Class rollbackStaticNatClass,Class rollbackBothNatClass) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = securityClass;
        this.aclClass = aclClass;
        this.snatClass = snatClass;
        this.dnatClass = dnatClass;
        this.staticNatClass = staticNatClass;
        this.bothNatClass = bothNatClass;
        this.routingClass = routingClass;
        this.rollbackSecurityClass = rollbackSecurityClass;
        this.rollbakcRoutingClass = rollbakcRoutingClass;
        this.rollbackSnatClass = rollbackSnatClass;
        this.rollbackDnatClass = rollbackDnatClass;
        this.rollbackStaticNatClass = rollbackStaticNatClass;
        this.rollbackBothNatClass = rollbackBothNatClass;
    }



    DeviceModelNumberEnum(int code, String key, String desc, Class securityClass, Class aclClass, Class snatClass, Class dnatClass, Class staticNatClass,
                          Class bothNatClass, Class routingClass, Class rollbackSecurityClass, Class rollbakcRoutingClass, Class rollbackSnatClass,
                          Class rollbackDnatClass,Class rollbackStaticNatClass,Class rollbackBothNatClass,
                          Class forbidClass, Class editForbidClass, Class disableSecurityClass, Class enableSecurityClass,
                          Class editSecurityClass, Class editRollbackSecurityClass) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = securityClass;
        this.aclClass = aclClass;
        this.snatClass = snatClass;
        this.dnatClass = dnatClass;
        this.staticNatClass = staticNatClass;
        this.bothNatClass = bothNatClass;
        this.routingClass = routingClass;
        this.rollbackSecurityClass = rollbackSecurityClass;
        this.rollbakcRoutingClass = rollbakcRoutingClass;
        this.rollbackSnatClass = rollbackSnatClass;
        this.rollbackDnatClass = rollbackDnatClass;
        this.rollbackStaticNatClass = rollbackStaticNatClass;
        this.rollbackBothNatClass = rollbackBothNatClass;
        this.forbidClass = forbidClass;
        this.editForbidClass = editForbidClass;
        this.disableSecurityClass = disableSecurityClass;
        this.enableSecurityClass = enableSecurityClass;
        this.editSecurityClass = editSecurityClass;
        this.editRollbackSecurityClass = editRollbackSecurityClass;
    }


    DeviceModelNumberEnum(int code, String key, String desc,Class balanceDnatClass,Class balanceBothNatClass,Class rollbackBalanceDnatClass,Class rollbackBalanceBothNatClass){
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.balanceDnatClass = balanceDnatClass;
        this.balanceBothNatClass = balanceBothNatClass;
        this.rollbackBalanceDnatClass = rollbackBalanceDnatClass;
        this.rollbackBalanceBothNatClass = rollbackBalanceBothNatClass;
    }

    DeviceModelNumberEnum(int code, String key, String desc, Class securityClass, Class aclClass, Class snatClass, Class dnatClass, Class staticNatClass,
                          Class bothNatClass, Class routingClass,Class staticRoutingClass, Class rollbackSecurityClass, Class rollbakcRoutingClass,Class rollbackStaticRoutingClass, Class rollbackSnatClass,
                          Class rollbackDnatClass,Class rollbackStaticNatClass,Class rollbackBothNatClass,
                          Class forbidClass, Class editForbidClass, Class disableSecurityClass, Class enableSecurityClass,
                          Class editSecurityClass, Class editRollbackSecurityClass) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = securityClass;
        this.aclClass = aclClass;
        this.snatClass = snatClass;
        this.dnatClass = dnatClass;
        this.staticNatClass = staticNatClass;
        this.bothNatClass = bothNatClass;
        this.routingClass = routingClass;
        this.staticRoutingClass = staticRoutingClass;
        this.rollbackSecurityClass = rollbackSecurityClass;
        this.rollbakcRoutingClass = rollbakcRoutingClass;
        this.rollbackStaticRoutingClass = rollbackStaticRoutingClass;
        this.rollbackSnatClass = rollbackSnatClass;
        this.rollbackDnatClass = rollbackDnatClass;
        this.rollbackStaticNatClass = rollbackStaticNatClass;
        this.rollbackBothNatClass = rollbackBothNatClass;
        this.forbidClass = forbidClass;
        this.editForbidClass = editForbidClass;
        this.disableSecurityClass = disableSecurityClass;
        this.enableSecurityClass = enableSecurityClass;
        this.editSecurityClass = editSecurityClass;
        this.editRollbackSecurityClass = editRollbackSecurityClass;
    }

    DeviceModelNumberEnum(int code, String key, String desc, Class securityClass, Class aclClass, Class snatClass, Class dnatClass, Class staticNatClass,
                          Class bothNatClass, Class routingClass,Class staticRoutingClass, Class rollbackSecurityClass, Class rollbakcRoutingClass,Class rollbackStaticRoutingClass, Class rollbackSnatClass,
                          Class rollbackDnatClass,Class rollbackStaticNatClass,Class rollbackBothNatClass, Class rollbackAclClass,
                          Class forbidClass, Class editForbidClass, Class disableSecurityClass, Class enableSecurityClass,
                          Class editSecurityClass, Class editRollbackSecurityClass) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.securityClass = securityClass;
        this.aclClass = aclClass;
        this.snatClass = snatClass;
        this.dnatClass = dnatClass;
        this.staticNatClass = staticNatClass;
        this.bothNatClass = bothNatClass;
        this.routingClass = routingClass;
        this.staticRoutingClass = staticRoutingClass;
        this.rollbackSecurityClass = rollbackSecurityClass;
        this.rollbakcRoutingClass = rollbakcRoutingClass;
        this.rollbackStaticRoutingClass = rollbackStaticRoutingClass;
        this.rollbackSnatClass = rollbackSnatClass;
        this.rollbackDnatClass = rollbackDnatClass;
        this.rollbackStaticNatClass = rollbackStaticNatClass;
        this.rollbackBothNatClass = rollbackBothNatClass;
        this.rollbackAclClass = rollbackAclClass;
        this.forbidClass = forbidClass;
        this.editForbidClass = editForbidClass;
        this.disableSecurityClass = disableSecurityClass;
        this.enableSecurityClass = enableSecurityClass;
        this.editSecurityClass = editSecurityClass;
        this.editRollbackSecurityClass = editRollbackSecurityClass;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getKey() {
        return key;
    }

    public Class getSecurityClass() {
        return securityClass;
    }

    public Class getAclClass() {
        return aclClass;
    }

    public Class getSnatClass() {
        return snatClass;
    }

    public Class getDnatClass() {
        return dnatClass;
    }

    public Class getStaticNatClass() {
        return staticNatClass;
    }

    public Class getBothNatClass() {
        return bothNatClass;
    }

    public Class getRoutingClass() {
        return routingClass;
    }

    public Class getRollbackSecurityClass() {
        return rollbackSecurityClass;
    }

    public Class getRollbackAclClass() {
        return rollbackAclClass;
    }

    public Class getRollbackSnatClass() {
        return rollbackSnatClass;
    }

    public Class getRollbackDnatClass() {
        return rollbackDnatClass;
    }

    public Class getRollbackStaticNatClass() {
        return rollbackStaticNatClass;
    }

    public Class getRollbackBothNatClass() {
        return rollbackBothNatClass;
    }

    public Class getRollbakcRoutingClass() {
        return rollbakcRoutingClass;
    }

    public Class getForbidClass() {
        return forbidClass;
    }

    public Class getEditForbidClass() {
        return editForbidClass;
    }

    public Class getDisableSecurityClass() {
        return disableSecurityClass;
    }

    public Class getEnableSecurityClass() {
        return enableSecurityClass;
    }

    public Class getEditSecurityClass() {
        return editSecurityClass;
    }

    public Class getEditRollbackSecurityClass() {
        return editRollbackSecurityClass;
    }

    public Class getBalanceDnatClass() {
        return balanceDnatClass;
    }

    public Class getBalanceBothNatClass() {
        return balanceBothNatClass;
    }

    public Class getRollbackBalanceDnatClass() {
        return rollbackBalanceDnatClass;
    }

    public Class getRollbackBalanceBothNatClass() {
        return rollbackBalanceBothNatClass;
    }

    public Class getStaticRoutingClass() {
        return staticRoutingClass;
    }

    public Class getRollbackStaticRoutingClass() {
        return rollbackStaticRoutingClass;
    }

    public static DeviceModelNumberEnum fromString(String text) {
        for (DeviceModelNumberEnum modelNumberEnum : DeviceModelNumberEnum.values()) {
            if (modelNumberEnum.key.equals(text)) {
                return modelNumberEnum;
            }
        }
        return DEFAULTMODEL;
    }

    /**
     * 是否在思科内的设备
     * @param code
     * @return
     */
    public static boolean isRangeCiscoCode(Integer code){
        boolean isCisco = CISCO.getCode()  <= code &&  code <= CISCO_ASA_99.getCode();
        return isCisco;
    }


    /**
     * 是否在天融信内的设备
     * @param code
     * @return
     */
    public static boolean isRangeTopSecCode(Integer code){
        boolean isTopSec = TOPSEC_TOS_005.getCode()  <= code &&  code <= TOPSEC_NG2.getCode();
        return isTopSec;
    }

    /**
     * 是否在天融信内的设备
     * @param code
     * @return
     */
    public static boolean isRangeHillStoneCode(Integer code){
        boolean isHillStone = HILLSTONE.getCode()  <= code &&  code <= HILLSTONE_V5.getCode();
        return isHillStone;
    }

    /**
     * 是否是飞塔的设备
     * @param code
     * @return
     */
    public static boolean isRangeFortCode(Integer code){
        // 是的是的
        return FORTINET.getCode()  <= code &&  code <= FORTINET_V5_2.getCode();
    }
}
