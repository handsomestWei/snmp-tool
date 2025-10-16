package com.wjy.snmp;

import com.wjy.snmp.receive.PduReqData;
import com.wjy.snmp.receive.PduRspData;
import com.wjy.snmp.receive.RelayInfo;

import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.smi.*;
import org.snmp4j.util.TableEvent;

import java.util.*;

/**
 * snmp4j类库数据类型转换为通用类型
 *
 * @author weijiayu
 * @date 2024/11/27 16:24
 */
public class SnmpHelper {

    public static String getVariableValue(Variable variable) {
        if (variable == null) {
            return null;
        }
        String value = variable.toString();
        return "noSuchObject".equals(value) ? null : value;
    }

    public static String getVbValue(VariableBinding vb) {
        if (vb == null || vb.getVariable() == null) {
            return null;
        }
        String value = vb.getVariable().toString();
        if ("noSuchObject".equals(value)) {
            return null;
        }
        return value;
    }

    public static HashMap<String, String> convertVb(VariableBinding vb) {
        HashMap<String, String> vbMap = new HashMap<>();
        if (vb == null) {
            return vbMap;
        }
        vbMap.put(vb.getOid().toString(), getVbValue(vb));
        return vbMap;
    }

    public static HashMap<String, String> convertVbArray(VariableBinding[] vbs) {
        LinkedHashMap<String, String> vbMap = new LinkedHashMap<>();
        if (vbs == null) {
            return vbMap;
        }
        for (VariableBinding vb : vbs) {
            vbMap.putAll(convertVb(vb));
        }
        return vbMap;
    }

    public static HashMap<String, String> convertTbEventList(List<TableEvent> tableEventList) {
        LinkedHashMap<String, String> vbMap = new LinkedHashMap<>();
        if (tableEventList == null) {
            return vbMap;
        }
        for (TableEvent tableEvent : tableEventList) {
            VariableBinding[] vbs = tableEvent.getColumns();
            vbMap.putAll(convertVbArray(vbs));
        }
        return vbMap;
    }

    public static HashMap<String, String> convertPdu(PDU pdu) {
        int size = pdu.getVariableBindings().size();
        VariableBinding[] vbs = new VariableBinding[size];
        vbs = pdu.getVariableBindings().toArray(vbs);
        return convertVbArray(vbs);
    }

    // key=ip, value=port
    public static Map.Entry<String, String> parseIpAddress(Address address) {
        HashMap<String, String> ipAddrMap = new HashMap<>();
        String[] addrArray = address.toString().split("/");
        ipAddrMap.put(addrArray[0], addrArray[1]);
        return ipAddrMap.entrySet().iterator().next();
    }

    public static PduReqData wrapperPduReqData(PDU pdu, Address address) {
        PduReqData pduReqData = new PduReqData();
        try {
            String[] addrArray = address.toString().split("/");
            pduReqData.setIp(addrArray[0]);
            pduReqData.setPort(addrArray[1]);
        } catch (Exception ignored) {
        }
        pduReqData.setOptType(pdu.getType());
        pduReqData.setDataSize(pdu.getMaxRepetitions());
        pduReqData.setDataMap(convertPdu(pdu));
        return pduReqData;
    }

    public static PDU wrapperRspDataToPdu(PduRspData pduRspData, PDU pdu) {
        if (pduRspData == null) {
            return pdu;
        }
        Vector<VariableBinding> vbs = new Vector<>();
        HashMap<String, String> oidValueMap = pduRspData.getDataMap();
        if (oidValueMap != null) {
            for (Map.Entry<String, String> entry : oidValueMap.entrySet()) {
                String val = entry.getValue();
                VariableBinding vb = new VariableBinding();
                vb.setOid(new OID(entry.getKey()));
                if (val != null && !val.isEmpty()) {
                    vb.setVariable(new OctetString(val));
                } else {
                    vb.setVariable(new Null());
                }
                vbs.add(vb);
            }
        }
        pdu.setVariableBindings(vbs);
        return pdu;
    }

    public static RelayInfo parseRelayInfo(CommandResponderEvent event) {
        try {
            String communityName = new String(event.getSecurityName());
            // eg. "public@192.168.0.193@161"
            String[] communityNameArray = communityName.split("@");
            RelayInfo relayInfo = new RelayInfo();
            relayInfo.setIp(communityNameArray[1]);
            relayInfo.setPort(communityNameArray[2]);
            relayInfo.setCommunityName(communityNameArray[3]);
            return relayInfo;
        } catch (Exception ignored) {
            return null;
        }
    }
}
