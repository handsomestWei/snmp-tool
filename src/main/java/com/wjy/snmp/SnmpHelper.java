package com.wjy.snmp;

import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.TableEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        HashMap<String, String> vbMap = new HashMap<>();
        if (vbs == null) {
            return vbMap;
        }
        for (VariableBinding vb : vbs) {
            vbMap.putAll(convertVb(vb));
        }
        return vbMap;
    }

    public static HashMap<String, String> convertTbEventList(List<TableEvent> tableEventList) {
        HashMap<String, String> vbMap = new HashMap<>();
        if (tableEventList == null) {
            return vbMap;
        }
        for (TableEvent tableEvent : tableEventList) {
            VariableBinding[] vbs = tableEvent.getColumns();
            if (vbs != null && vbs.length > 0) {
                vbMap.putAll(convertVb(vbs[0]));
            }
        }
        return vbMap;
    }

    public static HashMap<String, String> convertPdu(PDU pdu) {
        int size = pdu.getVariableBindings().size();
        VariableBinding[] vbs = new VariableBinding[size];
        vbs = pdu.getVariableBindings().toArray(vbs);
        return convertVbArray(vbs);
    }

    public static PDU wrapperPdu(HashMap<String, String> oidValueMap) {
        PDU pdu = new PDU();
        if (oidValueMap == null) {
            return pdu;
        }
        for (Map.Entry<String, String> entry : oidValueMap.entrySet())
            pdu.add(new VariableBinding(new OID(entry.getKey()),
                    new OctetString(entry.getValue())));
        return pdu;
    }
}
