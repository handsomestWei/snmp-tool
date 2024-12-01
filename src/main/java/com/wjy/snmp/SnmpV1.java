package com.wjy.snmp;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import java.io.IOException;

/**
 * snmp协议v1版本实现
 *
 * @author weijiayu
 * @date 2024/11/27 10:28
 */
@Slf4j
public class SnmpV1 extends AbsSnmpBase {

    public SnmpV1() {
        super();
    }

    public SnmpV1(int dispatchPoolSize, String listenAddress) {
        super(dispatchPoolSize, listenAddress);
    }

    @Override
    protected synchronized Snmp newInstanceSnmp() {
        try {
            Snmp snmp = newSnmp();
            snmp.listen();
            return snmp;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    protected Snmp newSnmp() throws IOException {
        ThreadPool threadPool =
                ThreadPool.create("snmp-" + this.getClass().getSimpleName() + "-" + this.hashCode() +
                                "-pool",
                        super.getDispatchPoolSize());
        MessageDispatcher messageDispatcher = new MultiThreadedMessageDispatcher(threadPool,
                new MessageDispatcherImpl());
        // 定义消息模型，才能在发出请求后接收到udp响应
        messageDispatcher.addMessageProcessingModel(new MPv1());
        // 设置消息接收监听地址
        TransportMapping<?> transportMapping = null;
        String address = super.getListenAddress();
        if (address == null) {
            transportMapping = new DefaultUdpTransportMapping();
        } else {
            Address listenAddress = GenericAddress.parse(address);
            if (listenAddress instanceof TcpAddress) {
                transportMapping = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
            } else {
                transportMapping = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
            }
        }
        return new Snmp(messageDispatcher, transportMapping);
    }

    @Override
    protected Target newTarget(String community) {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setVersion(SnmpConstants.version1);
        return target;
    }

    @Override
    protected PDU newPdu() {
        return new PDUv1();
    }

    @Override
    protected PDU newScopePdu() {
        return new PDU();
    }

    public Variable get(String ip, int port, String community, String oid) {
        return this.getSingle(ip, port, community, oid, PDU.GET);
    }

    public Variable getNext(String ip, int port, String community, String oid) {
        return this.getSingle(ip, port, community, oid, PDU.GETNEXT);
    }

    protected Variable getSingle(String ip, int port, String community, String oid, int optType) {
        try {
            Target target = super.createTarget(community, ip, port);
            PDU pdu = super.createPdu(optType, oid);
            ResponseEvent responseEvent = super.send(pdu, target);
            VariableBinding[] vbs = super.parseResponseEvent(responseEvent);
            if (vbs == null || vbs.length == 0) {
                return null;
            }
            return vbs[0].getVariable();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public Boolean set(String ip, int port, String community, String oid, String value) {
        try {
            Target target = super.createTarget(community, ip, port);
            PDU pdu = super.createSetPdu(oid, value);
            ResponseEvent responseEvent = super.send(pdu, target);
            return super.isSuccess(responseEvent);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}