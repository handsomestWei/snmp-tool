package com.wjy.snmp.receive;

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
     * @param pduReqData
     * @return void
     * @date 2024/11/28 19:31
     */
    void handlePdu(PduReqData pduReqData);
}
