package com.wjy.snmp.simulator.ctm;

import com.wjy.snmp.simulator.AbsSimulatorReceiveHandler;
import com.wjy.snmp.simulator.BaseOidInfo;

/**
 * 默认模拟处理
 *
 * @author weijiayu
 * @date 2024/12/13 14:52
 */
public class BaseReceiveHandler extends AbsSimulatorReceiveHandler {

    public BaseReceiveHandler(String oidDataFilePath, Integer randomNetWorkDelayMsSeed,
                              Integer randomErrorSeed) {
        super(oidDataFilePath, randomNetWorkDelayMsSeed, randomErrorSeed);
    }

    @Override
    public BaseOidInfo parseOidInfo(String line) {
        return super.defaultParseOidInfo(line);
    }

    @Override
    public String handleGetOptValue(BaseOidInfo baseOidInfo) {
        return baseOidInfo.getVal();
    }
}
