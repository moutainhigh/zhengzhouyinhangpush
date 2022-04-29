package com.abtnetworks.totems.common.enums;

public enum BalanceTypeEnum {

    ROUNDROBIN("Round Robin","round-robin"),
    RATIOMEMBER("Ratio (member)","ratio-member"),
    LEASTCONNECTIONSMEMBER("Least Connections (member)","least-connections-member"),
    OBSERVEDMEMBER("Observed(member)","observed-member"),
    PREDICTIVEMEMBER("Predictive (member)","predictive-member"),
    RATIONODE("Ratio (node)","ratio-node"),
    LEASTCONNECTIONSNODE("Least Connections (node)","least-connections-node"),
    FASTESTNODE("Fastest (node)","fastest-node"),
    OBSERVEDNODE("Observed (node)","observed-node"),
    PREDICTIVENODE("Predictive (node)","predictive-node"),
    DYNAMICRATIONODE("Dynamic Ratio (node)","dynamic-ratio-node"),
    FASTESTAPPLICATION("Fastest (application)","fastest-application"),
    LEASTSESSIONS("Least Sessions","least-sessions"),
    DYNAMICRATIOMEMBER("Dynamic Ratio (member)","dynamic-ratio-member"),
    WEIGHTEDLEASTCONNECTIONSMEMBER("Weighted Least Connections (member)","weighted-least-connections-member"),
    WEIGHTEDLEASTCONNECTIONSNODE("Weighted Least Connections (node)","weighted-least-connections-node"),
    RATIOSESSION("Ratio (session)","ratio-session"),
    RATIOLEASTCONNECTIONSMEMBER("Ratio Least Connections (member)","ratio-least-connections-member"),
    RATIOLEASTCONNECTIONSNODE("Ratio Least Connections (node)","ratio-least-connections-node"),
    ;
    private String key;

    private String name;

    public String getKey() {
        return key;
    }


    public String getName() {
        return name;
    }

    BalanceTypeEnum(String key,String name){
        this.key = key;
        this.name = name;
    }

    public static String getNameByKey(String key){
        for (BalanceTypeEnum BalanceTypeEnum: BalanceTypeEnum.values() ) {
            if(BalanceTypeEnum.getKey().equalsIgnoreCase(key)){
                return BalanceTypeEnum.getName();
            }
        }
        return null;
    }
}
