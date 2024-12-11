package com.wjy.snmp.receive;

import com.wjy.snmp.SnmpHelper;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.mp.StatusInformation;

import java.util.HashMap;

/**
 * snmp消息接收
 *
 * @author weijiayu
 * @date 2024/11/28 19:09
 */
@Slf4j
public class SnmpReceiver implements CommandResponder {

    private BizSnmpReceiveHandler handler;

    public SnmpReceiver(BizSnmpReceiveHandler handler) {
        this.handler = handler;
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        try {
            PDU pdu = event.getPDU();
            log.debug("snmp receiver addr={} pdu={}", event.getPeerAddress(), pdu);
            if (pdu == null) {
                return;
            }
            PDU rspPdu = null;
            if (handler != null) {
                HashMap<String, String> oidValueMap = handler.handlePdu(SnmpHelper.wrapperPduData(pdu,
                        event.getPeerAddress()));
                rspPdu = SnmpHelper.wrapperPdu(oidValueMap);
            }
            if (pdu.isConfirmedPdu()) {
                // 需要应答
                sendResponse(event, rspPdu);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendResponse(CommandResponderEvent event, PDU pdu) throws MessageException {
        pdu.setErrorIndex(0);
        pdu.setErrorStatus(0);
        pdu.setType(PDU.RESPONSE);
        StatusInformation statusInformation = new StatusInformation();
        event.getMessageDispatcher().returnResponsePdu(event.getMessageProcessingModel(),
                event.getSecurityModel(), event.getSecurityName(),
                event.getSecurityLevel(),
                pdu, event.getMaxSizeResponsePDU(), event.getStateReference(), statusInformation);
    }
}
