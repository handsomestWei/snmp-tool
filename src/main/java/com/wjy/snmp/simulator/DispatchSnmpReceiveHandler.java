package com.wjy.snmp.simulator;

import com.wjy.snmp.receive.BizSnmpReceiveHandler;
import com.wjy.snmp.receive.PduReqData;
import com.wjy.snmp.receive.PduRspData;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/**
 * 请求分发处理
 *
 * @author weijiayu
 * @date 2025/1/9 10:38
 */
@Slf4j
public class DispatchSnmpReceiveHandler implements BizSnmpReceiveHandler {

    private HashMap<String, AbsSimulatorReceiveHandler> oidHdlMap = new HashMap<>();

    @Override
    public PduRspData handlePdu(PduReqData pduReqData) throws Exception {
        // 根据请求oid，分发给对应处理器
        String reqOid = pduReqData.getDataMap().keySet().iterator().next();
        boolean isRelay = pduReqData.getRelayInfo() != null;
        for (String rootOid : oidHdlMap.keySet()) {
            // 复制一份用于匹配
            String matchOid = rootOid;
            if (isRelay) {
                // 规约：转发模式，需要匹配rootOid的@符前的部分
                if (!matchOid.contains("@")) {
                    continue;
                }
                // eg. "1.3.6.1.4.1.20@" -> "1.3.6.1.4.1.20"
                matchOid = rootOid.substring(0, rootOid.indexOf("@"));
            }
            if (reqOid.startsWith(matchOid)) {
                return oidHdlMap.get(rootOid).handlePdu(pduReqData);
            }
        }
        return null;
    }

    public void addCtmReceiveHandler(String rootOid, AbsSimulatorReceiveHandler ctmHdl) {
        oidHdlMap.put(rootOid, ctmHdl);
    }

    public void addCtmReceiveHandler(HashMap<String, AbsSimulatorReceiveHandler> ctmOidHdlMap) {
        oidHdlMap.putAll(ctmOidHdlMap);
    }
}
