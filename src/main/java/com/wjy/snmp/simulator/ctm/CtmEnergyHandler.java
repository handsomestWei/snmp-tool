package com.wjy.snmp.simulator.ctm;


import com.wjy.snmp.simulator.AbsSimulatorReceiveHandler;
import com.wjy.snmp.simulator.BaseOidInfo;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 自定义能耗产品模拟处理
 *
 * @author weijiayu
 * @date 2024/12/13 10:29
 */
public class CtmEnergyHandler extends AbsSimulatorReceiveHandler {

    // 电能标识：csv文件自定义oid数据第三列oid名称包含energy关键字
    private static final String ENERGY_KEY = "energy";

    public CtmEnergyHandler(String oidDataFilePath, Integer randomNetWorkDelayMsSeed,
                            Integer randomErrorSeed) {
        super(oidDataFilePath, randomNetWorkDelayMsSeed, randomErrorSeed);
    }

    @Override
    public BaseOidInfo<CtmOidInfo> parseOidInfo(String line) {
        BaseOidInfo<CtmOidInfo> baseOidInfo = new BaseOidInfo<>();
        String[] oidArray = line.split(",", -1);
        baseOidInfo.setOid(oidArray[0]);
        baseOidInfo.setVal(oidArray[1]);
        CtmOidInfo ctmInfo = new CtmOidInfo();
        baseOidInfo.setCtmInfo(ctmInfo);
        ctmInfo.setOidName(oidArray[2]);
        return baseOidInfo;
    }

    @Override
    public String handleGetOptValue(BaseOidInfo baseOidInfo) {
        CtmOidInfo ctmInfo = (CtmOidInfo) baseOidInfo.getCtmInfo();
        String oidVal = baseOidInfo.getVal();
        if (ctmInfo.getOidName() != null && ctmInfo.getOidName().contains(ENERGY_KEY)) {
            // 累加
            int increaseEnergy = ThreadLocalRandom.current().nextInt(1, 2);
            oidVal = String.valueOf(Double.parseDouble(oidVal != null ? oidVal : "0") + increaseEnergy);
            baseOidInfo.setVal(oidVal);
        }
        return oidVal;
    }
}
