package com.wjy.snmp.simulator;

import com.wjy.snmp.SnmpV2;
import com.wjy.snmp.receive.SnmpReceiver;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

/**
 * snmp模拟服务端
 *
 * @author weijiayu
 * @date 2024/12/12 17:41
 */
@Slf4j
@CommandLine.Command(name = "snmpSimulatorServer", mixinStandardHelpOptions = true)
public class SnmpSimulatorServer implements Runnable {

    // 自定义oid数据文件后缀
    private static final String snmpOidDataCsvSuffix = "_snmp.csv";
    // 自定义oid处理器包前缀
    private static final String ctmHdlClazzPackagePrefix = "com.wjy.snmp.simulator.ctm.";
    @CommandLine.Option(names = { "-conf", "--confFilePath" }, description = "simulator-config.properties file path.")
    String configFilePath = System.getProperty("user.dir") + "/simulator-config.properties";
    @CommandLine.Option(names = { "-ip", "--ip" }, description = "server listen ip.")
    private String listenIp = "0.0.0.0";
    @CommandLine.Option(names = { "-p", "--port" }, description = "server listen port.")
    private int listenPort = 161;
    @CommandLine.Option(names = { "-pool", "--poolSize" }, description = "snmp pool size with device num.")
    private int poolSize = 200;
    @CommandLine.Option(names = { "-dir", "--dataDir" }, description = "oid data csv dir.")
    private String oidDataDir = System.getProperty("user.dir") + "/data/";

    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new SnmpSimulatorServer()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        try {
            System.out.println("device num: " + poolSize);
            System.out.println("config file path: " + configFilePath);
            System.out.println("oid data csv dir: " + oidDataDir);
            start();
            while (true) {
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start() throws Exception {
        String listenAddr = "udp:" + listenIp + "/" + listenPort;
        SnmpV2 snmpV2 = new SnmpV2(poolSize, listenAddr);
        // 添加消息分发处理器
        DispatchSnmpReceiveHandler dispatchHandler = new DispatchSnmpReceiveHandler();
        SnmpReceiver snmpReceiver = new SnmpReceiver(dispatchHandler);
        snmpV2.addCommandResponder(snmpReceiver);
        // 加载自定义处理器并注册
        HashMap<String, AbsSimulatorReceiveHandler> ctmOidHdlMap = this.loadCtmReceiveHandler(this.configFilePath,
                this.oidDataDir, snmpV2);
        dispatchHandler.addCtmReceiveHandler(ctmOidHdlMap);
        System.out.println("snmp simulator server running and listenAddr: " + listenAddr);
        System.out.println("snmp simulator support oid: " + ctmOidHdlMap.keySet());
    }

    private HashMap<String, AbsSimulatorReceiveHandler> loadCtmReceiveHandler(String configFilePath,
            String oidDataDir,
            SnmpV2 snmpV2) throws Exception {
        HashMap<String, AbsSimulatorReceiveHandler> oidHdlMap = new HashMap<>();
        try (InputStream input = Files.newInputStream(Paths.get(configFilePath))) {
            Properties prop = new Properties();
            prop.load(input);
            for (Object propKey : prop.keySet()) {
                try {
                    String rootOid = propKey.toString();
                    String oidConfig = prop.getProperty(rootOid);

                    // 解析参数配置
                    // TODO 配置优化。暂时使用逗号分割
                    String[] paramArray = oidConfig.split(",", -1);
                    String clazzFullName = ctmHdlClazzPackagePrefix + paramArray[0];
                    String oidDataFilePath = oidDataDir + rootOid + snmpOidDataCsvSuffix;
                    Integer randomNetWorkDelayMsSeed = null;
                    if (paramArray.length > 1 && !paramArray[1].trim().isEmpty()) {
                        try {
                            randomNetWorkDelayMsSeed = Integer.parseInt(paramArray[1]);
                        } catch (Exception ignored) {
                        }
                    }
                    Integer randomErrorSeed = null;
                    if (paramArray.length > 2 && !paramArray[2].trim().isEmpty()) {
                        try {
                            randomErrorSeed = Integer.parseInt(paramArray[2]);
                        } catch (Exception ignored) {
                        }
                    }

                    // 实例化对象
                    Class<?> clazz = SnmpSimulatorServer.class.getClassLoader().loadClass(clazzFullName);
                    Constructor<?> constructor = clazz.getConstructor(String.class, Integer.class, Integer.class);
                    AbsSimulatorReceiveHandler ctmHdl = (AbsSimulatorReceiveHandler) constructor.newInstance(
                            oidDataFilePath,
                            randomNetWorkDelayMsSeed, randomErrorSeed);
                    ctmHdl.setSnmpV2(snmpV2);
                    oidHdlMap.put(rootOid, ctmHdl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return oidHdlMap;
    }
}
