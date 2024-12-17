package com.wjy.snmp.receive;

import org.snmp4j.PDU;

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
     * @param pdu
     * @return PduData
     * @date 2024/11/28 19:00
     */
    PDU handlePdu(PDU pdu);
}
