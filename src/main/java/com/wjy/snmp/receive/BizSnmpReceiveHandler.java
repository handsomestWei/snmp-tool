package com.wjy.snmp.receive;

import org.snmp4j.PDU;

import java.util.HashMap;

/**
 * snmp接收报文业务处理接口
 *
 * @author weijiayu
 * @date 2024/11/28 19:07
 */
public interface BizSnmpReceiveHandler {

    /**
     * 非确认类型的pdu报文，接口返回可为空
     *
     * @param pduData
     * @return HashMap<String, String>
     * @date 2024/11/28 19:00
     */
    HashMap<String, String> handlePdu(PduData pduData);
}
