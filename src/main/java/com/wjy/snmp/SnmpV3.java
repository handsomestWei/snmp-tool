package com.wjy.snmp;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import java.io.IOException;

/**
 * TODO snmp协议v3版本实现
 *
 * @author weijiayu
 * @date 2024/11/27 10:41
 */
@Slf4j
public class SnmpV3 extends SnmpV2 {

    private UsmUser usmUser;

    public SnmpV3(String securityName,
                  OID authenticationProtocol,
                  String authToken,
                  OID privacyProtocol,
                  String privacyToken) {
        super();
    }

    /**
     * snmp v3连接初始化
     *
     * @param dispatchPoolSize
     * @param listenAddress
     * @param securityName
     * @param authenticationProtocol eg org.snmp4j.security.AuthSHA.ID
     * @param authToken
     * @param privacyProtocol        eg org.snmp4j.security.PrivAES128.ID
     * @param privacyToken
     * @date 2024/11/27 15:16
     */
    public SnmpV3(int dispatchPoolSize, String listenAddress, String securityName,
                  OID authenticationProtocol,
                  String authToken,
                  OID privacyProtocol,
                  String privacyToken) {
        super(dispatchPoolSize, listenAddress);
        this.usmUser = new UsmUser(new OctetString(securityName), authenticationProtocol, new OctetString(authToken),
                privacyProtocol, new OctetString(privacyToken));
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
        // 设置v3安全模式
        // @link https://github.com/gaoxingliang/snmp4jdemo/blob/master/src/demo/examples/snmpget/TestSnmpGetV3.java
        OctetString localEngineID = new OctetString(MPv3.createLocalEngineID());
        USM usm = new USM(SecurityProtocols.getInstance().addDefaultProtocols(), localEngineID, 0);
        usm.setEngineDiscoveryEnabled(true);
        usm.addUser(this.usmUser.getSecurityName(), this.usmUser);
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3(usm));
        return snmp;
    }

    @Override
    protected Target newTarget(String community) {
        UserTarget target = new UserTarget();
        target.setSecurityName(this.usmUser.getSecurityName());
        target.setVersion(SnmpConstants.version3);
        return target;
    }

    @Override
    protected PDU newPdu() {
        return new ScopedPDU();
    }

    @Override
    protected PDU newScopePdu() {
        return new ScopedPDU();
    }
}
