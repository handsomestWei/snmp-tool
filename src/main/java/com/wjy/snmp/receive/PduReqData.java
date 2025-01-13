package com.wjy.snmp.receive;

import lombok.Data;

import java.util.HashMap;

/**
 * @author weijiayu
 * @date 2024/12/6 16:43
 */
@Data
public class PduReqData {

    private String ip;
    private String port;
    private int optType;

    private int dataSize;
    private HashMap<String, String> dataMap;
}
