package com.wjy.snmp.simulator;

import lombok.Data;

/**
 * @author weijiayu
 * @date 2024/12/13 9:33
 */
@Data
public class BaseOidInfo<T> {

    private String oid;
    private String val;
    private T ctmInfo;
}