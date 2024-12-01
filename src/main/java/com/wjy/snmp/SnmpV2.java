package com.wjy.snmp;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.io.IOException;
import java.util.List;

/**
 * snmp协议v2版本实现
 *
 * @author weijiayu
 * @date 2024/11/27 10:33
 */
@Slf4j
public class SnmpV2 extends SnmpV1 {

    public SnmpV2() {
        super();
    }

    public SnmpV2(int dispatchPoolSize, String listenAddress) {
        super(dispatchPoolSize, listenAddress);
    }

    @Override
    protected synchronized Snmp newInstanceSnmp() {
        try {
            Snmp snmp = this.newSnmp();
            snmp.listen();
            return snmp;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    protected Snmp newSnmp() throws IOException {
        Snmp snmp = super.newSnmp();
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
        return snmp;
    }

    @Override
    protected Target newTarget(String community) {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    @Override
    protected PDU newPdu() {
        return new PDU();
    }

    public VariableBinding[] getBulk(String ip, int port, String community, String oid, int dataSize) {
        try {
            Target target = super.createTarget(community, ip, port);
            PDU pdu = super.createPdu(PDU.GETBULK, oid);
            pdu.setMaxRepetitions(dataSize);
            ResponseEvent responseEvent = super.send(pdu, target);
            return super.parseResponseEvent(responseEvent);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    // fast more than getNext
    public List<TableEvent> getBulkTable(String ip, int port, String community, String oid, String startOid,
                                         String endOid) {
        return this.getTable(ip, port, community, oid, startOid, endOid, PDU.GETBULK);

    }

    @Deprecated
    public List<TableEvent> getNextTable(String ip, int port, String community, String oid, String startOid,
                                         String endOid) {
        return this.getTable(ip, port, community, oid, startOid, endOid, PDU.GETNEXT);
    }

    protected List<TableEvent> getTable(String ip, int port, String community, String oid, String startOid,
                                        String endOid, int optType) {
        Target target = this.createTarget(community, ip, port);
        TableUtils utils = new TableUtils(this.getSnmp(), new DefaultPDUFactory(
                optType));
        OID[] columnOidArray = new OID[]{new OID(oid)};
        OID lowerBoundIndex = startOid != null ? new OID(startOid) : null;
        OID upperBoundIndex = endOid != null ? new OID(endOid) : null;
        log.debug("snmp get table req optType={} target={}, oid={}, startOid={}, endOid={}", optType, target, oid,
                startOid,
                endOid);
        List<TableEvent> tableEventList = utils.getTable(target, columnOidArray, lowerBoundIndex, upperBoundIndex);
        log.debug("snmp get table rsp {}", tableEventList);
        return tableEventList;
    }
}