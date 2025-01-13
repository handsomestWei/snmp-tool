package com.wjy.snmp.simulator;

import com.wjy.snmp.receive.BizSnmpReceiveHandler;
import com.wjy.snmp.receive.PduReqData;
import com.wjy.snmp.receive.PduRspData;
import com.wjy.snmp.simulator.ctm.CtmOidInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.PDU;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * snmp模拟请求响应基类
 *
 * @author weijiayu
 * @date 2024/12/12 17:50
 */
@Slf4j
public abstract class AbsSimulatorReceiveHandler<T> implements BizSnmpReceiveHandler {

    // TODO 数据转换为行列表格
    @Getter
    private LinkedHashMap<String, BaseOidInfo<T>> oidDataMap = new LinkedHashMap<>();
    // 随机网络延迟种子
    private Integer randomNetWorkDelayMsSeed;
    // 随机异常种子
    private Integer randomErrorSeed;

    public AbsSimulatorReceiveHandler(String oidDataFilePath, Integer randomNetWorkDelayMsSeed,
                                      Integer randomErrorSeed) {
        this.randomNetWorkDelayMsSeed = randomNetWorkDelayMsSeed;
        this.randomErrorSeed = randomErrorSeed;
        this.oidDataMap = loadOidCsvData(oidDataFilePath);
    }

    public abstract BaseOidInfo<T> parseOidInfo(String line);

    public abstract String handleGetOptValue(BaseOidInfo<T> baseOidInfo);

    @Override
    public PduRspData handlePdu(PduReqData pduReqData) throws Exception {
        doDisturbance();
        PduRspData pduRspData = new PduRspData();
        LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
        pduRspData.setDataMap(dataMap);
        if (pduReqData.getDataMap() == null) {
            return pduRspData;
        }
        switch (pduReqData.getOptType()) {
            case PDU.GET:
                handleGetOpt(pduReqData, pduRspData);
                return pduRspData;
            case PDU.GETBULK:
                handleGetBulkOpt(pduReqData, pduRspData);
                return pduRspData;
            case PDU.SET:
                handleSetOpt(pduReqData, pduRspData);
                return pduRspData;
            default:
                throw new RuntimeException("snmp optType not support");
        }
    }

    public BaseOidInfo defaultParseOidInfo(String line) {
        String[] oidArray = line.split(",", -1);
        BaseOidInfo<CtmOidInfo> baseOidInfo = new BaseOidInfo<>();
        baseOidInfo.setOid(oidArray[0]);
        baseOidInfo.setVal(oidArray[1]);
        return baseOidInfo;
    }

    private void handleGetOpt(PduReqData pduReqData, PduRspData pduRspData) {
        for (String oid : pduReqData.getDataMap().keySet()) {
            BaseOidInfo<T> baseOidInfo = oidDataMap.get(oid);
            if (baseOidInfo == null) {
                continue;
            }
            String oidVal = handleGetOptValue(baseOidInfo);
            pduRspData.getDataMap().put(oid, oidVal);
        }
    }

    private void handleGetBulkOpt(PduReqData pduReqData, PduRspData pduRspData) {
        int dataSize = pduReqData.getDataSize();
        // TODO 使用list辅助查询
        List<String> oidList = new ArrayList<>(oidDataMap.keySet());
        // TODO 支持多个
        String reqOid = pduReqData.getDataMap().keySet().iterator().next();
        // 找到起始索引
        int startIndex = 0;
        if (reqOid.endsWith(".0")) {
            for (; startIndex < oidList.size(); startIndex++) {
                if (reqOid.equals(oidList.get(startIndex))) {
                    // 起始位置+1
                    startIndex++;
                    break;
                }
            }
        } else {
            // oid非.0结尾，视为父节点，默认从0开始
        }
        // 访问区间内数据
        int i = 0;
        for (; i < dataSize; i++) {
            if (startIndex + i >= oidList.size()) {
                break;
            } else {
                String oid = oidList.get(startIndex + i);
                BaseOidInfo<T> baseOidInfo = oidDataMap.get(oid);
                pduRspData.getDataMap().put(oid, handleGetOptValue(baseOidInfo));
            }
        }
        // 桶没有装满，填充末尾最后一个
        for (; i < dataSize; i++) {
            String oid = oidList.get(oidList.size() - 1);
            BaseOidInfo<T> baseOidInfo = oidDataMap.get(oid);
            pduRspData.getDataMap().put(oid, handleGetOptValue(baseOidInfo));
        }
    }

    private void handleSetOpt(PduReqData pduReqData, PduRspData pduRspData) {
        for (Map.Entry<String, String> entry : pduReqData.getDataMap().entrySet()) {
            String oid = entry.getKey();
            BaseOidInfo baseOidInfo = oidDataMap.get(oid);
            if (baseOidInfo == null) {
                continue;
            }
            baseOidInfo.setVal(entry.getValue());
            pduRspData.getDataMap().put(oid, entry.getValue());
        }
    }

    // 随机扰动
    private void doDisturbance() throws Exception {
        if (randomNetWorkDelayMsSeed != null && randomNetWorkDelayMsSeed > 0) {
            // 模拟网络环境，随机延迟x毫秒
            int randomDelayMillis = ThreadLocalRandom.current().nextInt(0, randomNetWorkDelayMsSeed + 1);
            Thread.sleep(randomDelayMillis);
        }
        if (randomErrorSeed != null && randomErrorSeed > 0) {
            // 模拟异常，随机失败
            if (1 == ThreadLocalRandom.current().nextInt(0, randomErrorSeed)) {
                throw new RuntimeException("random error");
            }
        }
    }

    private LinkedHashMap<String, BaseOidInfo<T>> loadOidCsvData(String oidDataFilePath) {
        LinkedHashMap<String, BaseOidInfo<T>> oidDataMap = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(oidDataFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                BaseOidInfo<T> baseOidInfo = parseOidInfo(line);
                oidDataMap.put(baseOidInfo.getOid(), baseOidInfo);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return oidDataMap;
    }
}
