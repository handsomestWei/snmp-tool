package com.wjy.snmp;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommandResponder;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.List;

/**
 * snmp协议基类
 *
 * @author weijiayu
 * @date 2024/11/28 10:54
 * @see org.snmp4j.mp.SnmpConstants#version1 注意协议版本号和库内常量定义的对应关系为v1-0，v2-1，v3-3
 */
@Slf4j
public abstract class AbsSnmpBase implements AutoCloseable {

    private volatile Snmp snmp = null;
    private int retries = 2;
    private long timeout = 3 * 1000;
    private int dispatchPoolSize = 5;
    private String listenAddress;

    protected abstract Snmp newInstanceSnmp();

    protected abstract Target newTarget(String community);

    protected abstract PDU newPdu();

    protected abstract PDU newScopePdu();

    protected String getListenAddress() {
        return this.listenAddress;
    }

    public AbsSnmpBase() {
    }

    /**
     * 初始化
     *
     * @param dispatchPoolSize 线程池
     * @param listenAddress    监听地址<protocol>:<ip>/<port>。使用默认请求161、监听162端口时，类库已绑定不用配置。eg. udp:192.168.0.87/163
     * @return
     * @date 2024/11/29 10:00
     */
    public AbsSnmpBase(int dispatchPoolSize, String listenAddress) {
        if (dispatchPoolSize > 0) {
            this.dispatchPoolSize = dispatchPoolSize;
        }
        this.listenAddress = listenAddress;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * 添加消息接收处理器
     *
     * @param listener
     * @return void
     * @throws
     * @date 2024/11/28 19:32
     * @see com.wjy.snmp.receive
     */
    public void addCommandResponder(CommandResponder listener) {
        this.getSnmp().addCommandResponder(listener);
    }

    protected int getDispatchPoolSize() {
        return this.dispatchPoolSize;
    }

    @Override
    public void close() throws Exception {
        if (this.snmp != null) {
            snmp.close();
        }
    }

    protected Snmp getSnmp() {
        if (this.snmp == null) {
            synchronized (this) {
                if (this.snmp == null) {
                    this.snmp = newInstanceSnmp();
                }
            }
        }
        return this.snmp;
    }

    protected Target createTarget(String community, String ipAddress, int port) {
        Target target = this.newTarget(community);
        target.setAddress(GenericAddress.parse("udp:" + ipAddress + "/" + port));
        target.setRetries(this.retries);
        target.setTimeout(this.timeout);
        return target;
    }

    // TODO 安全认证
    protected Target createTarget(String community, String ipAddress, int port, int securityLevel, int securityModel,
                                  String securityName) {
        Target target = this.newTarget(community);
        target.setAddress(GenericAddress.parse("udp:" + ipAddress + "/" + port));
        target.setRetries(this.retries);
        target.setTimeout(this.timeout);
        target.setSecurityLevel(securityLevel);
        target.setSecurityModel(securityModel);
        target.setSecurityName(new OctetString(securityName));
        return target;
    }

    protected PDU createPdu(int optType, String oid) {
        PDU pdu = this.newPdu();
        pdu.setType(optType);
        pdu.add(new VariableBinding(new OID(oid)));
        return pdu;
    }

    protected PDU createPdu(int optType, List<String> oidList) {
        PDU pdu = this.newScopePdu();
        pdu.setType(optType);
        for (String oid : oidList) {
            pdu.add(new VariableBinding(new OID(oid)));
        }
        return pdu;
    }

    protected PDU createSetPdu(String oid, String value) {
        PDU pdu = this.newPdu();
        pdu.setType(PDU.SET);
        pdu.add(new VariableBinding(new OID(oid), new OctetString(value)));
        return pdu;
    }

    protected ResponseEvent send(PDU pdu, Target target) throws IOException {
        log.debug("snmp req pdu={}, target={}", pdu, target);
        return this.getSnmp().send(pdu, target);
    }

    protected VariableBinding[] parseResponseEvent(ResponseEvent responseEvent) throws Exception {
        if (responseEvent == null || responseEvent.getResponse() == null) {
            throw new Exception("snmp rsp timed out");
        }
        PDU response = responseEvent.getResponse();
        log.debug("snmp rsp data {}", response);
        if (response.getErrorStatus() != PDU.noError) {
            throw new Exception("snmp rsp error " + response.getErrorStatusText());
        }
        int size = response.getVariableBindings().size();
        return response.getVariableBindings().toArray(new VariableBinding[size]);
    }

    protected Boolean isSuccess(ResponseEvent responseEvent) {
        if (responseEvent == null || responseEvent.getResponse() == null) {
            return false;
        }
        PDU response = responseEvent.getResponse();
        log.debug("snmp rsp data {}", response);
        return response.getErrorStatus() != PDU.noError;
    }
}
