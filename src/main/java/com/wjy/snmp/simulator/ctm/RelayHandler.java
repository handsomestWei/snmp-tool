package com.wjy.snmp.simulator.ctm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wjy.snmp.SnmpHelper;
import com.wjy.snmp.receive.PduReqData;
import com.wjy.snmp.receive.PduRspData;
import com.wjy.snmp.receive.RelayInfo;
import com.wjy.snmp.simulator.AbsSimulatorReceiveHandler;
import com.wjy.snmp.simulator.BaseOidInfo;

import org.snmp4j.smi.Variable;
import org.snmp4j.util.TableEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * 转发处理
 *
 * @author weijiayu
 * @date 2025/10/16 14:52
 */
@Slf4j
public class RelayHandler extends AbsSimulatorReceiveHandler {

    public RelayHandler(String oidDataFilePath, Integer randomNetWorkDelayMsSeed,
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

    @Override
    public void handleGetOpt(PduReqData pduReqData, PduRspData pduRspData) {
        RelayInfo relayInfo = pduReqData.getRelayInfo();
        for (String oid : pduReqData.getDataMap().keySet()) {
            Variable variable = this.getSnmpV2().get(relayInfo.getIp(),
                    Integer.parseInt(relayInfo.getPort()), relayInfo.getCommunityName(),
                    oid);
            pduRspData.getDataMap().put(oid, SnmpHelper.getVariableValue(variable));
        }
    }

    @Override
    public void handleGetBulkOpt(PduReqData pduReqData, PduRspData pduRspData) {
        RelayInfo relayInfo = pduReqData.getRelayInfo();
        List<String> oidList = new ArrayList<>(pduReqData.getDataMap().keySet());
        List<TableEvent> events = this.getSnmpV2().getBulkTable(relayInfo.getIp(),
                Integer.parseInt(relayInfo.getPort()), relayInfo.getCommunityName(),
                oidList, pduReqData.getDataSize());
        HashMap<String, String> vbMap = SnmpHelper.convertTbEventList(events);
        if (vbMap != null) {
            pduRspData.getDataMap().putAll(vbMap);
        }
    }

    @Override
    public void handleSetOpt(PduReqData pduReqData, PduRspData pduRspData) {
        RelayInfo relayInfo = pduReqData.getRelayInfo();
        for (Map.Entry<String, String> entry : pduReqData.getDataMap().entrySet()) {
            String oid = entry.getKey();
            this.getSnmpV2().set(relayInfo.getIp(),
                    Integer.parseInt(relayInfo.getPort()), relayInfo.getCommunityName(),
                    oid, entry.getValue());
        }
        pduRspData.getDataMap().putAll(pduReqData.getDataMap());
    }

    @Override
    public LinkedHashMap<String, BaseOidInfo> loadOidCsvData(String oidDataFilePath) {
        return new LinkedHashMap<String, BaseOidInfo>();
    }

}
