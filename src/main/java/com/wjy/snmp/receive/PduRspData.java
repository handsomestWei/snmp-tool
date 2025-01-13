package com.wjy.snmp.receive;

import lombok.Data;

import java.util.LinkedHashMap;

/**
 * @author weijiayu
 * @date 2024/12/13 9:23
 */
@Data
public class PduRspData {

    private LinkedHashMap<String, String> dataMap;
    private int hdlStatus;
}
