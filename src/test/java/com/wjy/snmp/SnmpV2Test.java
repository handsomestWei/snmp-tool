package com.wjy.snmp;

import com.wjy.snmp.receive.BizSnmpTrapReceiverHandler;
import com.wjy.snmp.receive.PduReqData;
import com.wjy.snmp.receive.SnmpTrapReceiver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.TableEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class SnmpV2Test {

    private SnmpV2 snmpV2;

    @Before
    public void setUp() {
        // open snmp service in your machine
        int poolSize = 2;
        String listenAddr = "udp:0.0.0.0/163";
        snmpV2 = new SnmpV2(poolSize, listenAddr);
        snmpV2.addCommandResponder(new SnmpTrapReceiver(new BizSnmpTrapReceiverHandler() {
            @Override
            public void handlePdu(PduReqData pduReqData) {
            }
        }));
    }

    @After
    public void tearDown() throws Exception {
        snmpV2.close();
    }

    @Test
    public void getBulkTable_1() {
        List<TableEvent> tableEventList = snmpV2.getBulkTable("127.0.0.1", 161, "public",
                "1.3.6.1.2.1.1", null, null);

        HashMap<String, String> vbMap = SnmpHelper.convertTbEventList(tableEventList);
        System.out.println(vbMap);
        Assert.assertNotNull(vbMap);
    }

    @Test
    public void getBulkTable_2() {
        List<String> oidList = Arrays.asList("1.3.6.1.2.1.1.1", "1.3.6.1.2.1.1.2");
        List<TableEvent> tableEventList = snmpV2.getBulkTable("127.0.0.1", 161, "public",
                oidList);

        HashMap<String, String> vbMap = SnmpHelper.convertTbEventList(tableEventList);
        System.out.println(vbMap);
        Assert.assertNotNull(vbMap);
    }

    @Test
    public void getBulkTableAsync_1() throws Exception {
        Consumer<HashMap<String, String>> asyncRspHdl = (vbMap) -> {
            for (String oid : vbMap.keySet()) {
                System.out.println(oid + "," + vbMap.get(oid));
            }
            System.out.println(vbMap.size());
        };

        List<String> oidList = new ArrayList<>();
        oidList.add("1.3.6.1.2.1.1");
        snmpV2.getBulkTableAsync("127.0.0.1", 161, "public",
                oidList, 100, asyncRspHdl);
        Thread.currentThread().sleep(3 * 1000);
    }

    @Test
    public void receiveTrap() throws Exception {
        sendTestTrap();
    }

    private void sendTestTrap() throws Exception {
        TransportMapping<?> transport = new DefaultUdpTransportMapping();
        transport.listen();
        Snmp snmp = new Snmp(transport);
        PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);
        Address targetAddress = new UdpAddress("127.0.0.1/163");

        pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID,
                new OID(".1.3.6.1.2.1.1.7")));
        pdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.1"),
                new OctetString("Test Trap")));

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);
        target.setAddress(targetAddress);
        target.setTimeout(5 * 1000);
        ResponseEvent responseEvent = snmp.send(pdu, target);
        snmp.close();
    }
}