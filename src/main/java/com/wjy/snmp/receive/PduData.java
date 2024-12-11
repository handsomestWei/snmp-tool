package com.wjy.snmp.receive;

import lombok.Data;

import java.util.HashMap;

@Data
public class PduData {

    private String ip;
    private String port;
    private HashMap<String, String> dataMap;
}
