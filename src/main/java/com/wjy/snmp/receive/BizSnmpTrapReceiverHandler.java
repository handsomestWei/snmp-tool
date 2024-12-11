package com.wjy.snmp.receive;

import org.snmp4j.PDU;

/**
 * snmp trap接收报文业务处理接口
 *
 * @author weijiayu
 * @date 2024/11/28 14:03
 */
public interface BizSnmpTrapReceiverHandler {

    /**
     * 不需要应答
     *
     * @param pduData
     * @return void
     * @date 2024/11/28 19:31
     */
    void handlePdu(PduData pdu);
}
