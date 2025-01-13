package com.wjy.snmp.receive;

import com.wjy.snmp.SnmpHelper;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;

/**
 * snmp trap消息接收
 *
 * @author weijiayu
 * @date 2024/11/28 11:45
 */
@Slf4j
public class SnmpTrapReceiver implements CommandResponder {

    private BizSnmpTrapReceiverHandler handler;

    public SnmpTrapReceiver(BizSnmpTrapReceiverHandler handler) {
        this.handler = handler;
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        try {
            PDU pdu = event.getPDU();
            log.debug("snmp trap receiver addr={} pdu={}", event.getPeerAddress(), pdu);
            if (pdu == null) {
                return;
            }
            switch (pdu.getType()) {
                case PDU.TRAP:
                case PDU.V1TRAP:
                    handler.handlePdu(SnmpHelper.wrapperPduReqData(pdu, event.getPeerAddress()));
                default:
                    // not a Trap, ignore
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
