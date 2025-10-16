# snmp-tool
![Static Badge](https://img.shields.io/badge/build-passing-green)
![Static Badge](https://img.shields.io/badge/stars-please-green)
![Static Badge](https://img.shields.io/badge/issue-thanks-green)

SNMP工具集，支持SNMP请求收发、Trap陷阱消息接收处理、海量SNMP设备模拟、简单MIB解析等功能。

## 功能特性
+ 基于开源的SNMP4j作为内部引擎，确保稳定性和可靠性。
    + 封装出单例模式和连接配置项，方便和上层`springboot`项目整合
    + 封装出更易用的接口，简化调用流程，尤其是Table表格批量操作
    + 支持SNMP v1/v2c协议的数据发送。
    + 支持SNMP请求和响应，Trap陷阱消息接收处理。
+ 设备模拟：提供[SNMP设备模拟器](/src/main/java/com/wjy/snmp/simulator/)java实现，只需提供oid文件和简单配置即可使用。
    + 支持海量设备模拟。
    + 支持消息转发。
    + 支持自定义数据模拟扩展开发。
+ MIB解析：提供简单的MIB解析功能，规避[mibble](https://github.com/cederberg/mibble)`GPL-2.0`许可证风险。
+ 工具类: 提供SNMP数据转换和辅助功能。

## 使用说明

### SNMP发送
位于 [snmp包](https://github.com/handsomestWei/snmp-tool/tree/main/src/main/java/com/wjy/snmp) 中，建议使用 [SnmpV2](https://github.com/handsomestWei/snmp-tool/blob/main/src/main/java/com/wjy/snmp/SnmpV2.java) 类

```java
// 示例：发送SNMP GET请求
SnmpV2 snmp = new SnmpV2();
snmp.setCommunity("public");
snmp.setTargetHost("192.168.1.1");
snmp.setTargetPort(161);
String result = snmp.snmpGet("1.3.6.1.2.1.1.1.0");
```

### SNMP接收
位于 [receive包](https://github.com/handsomestWei/snmp-tool/tree/main/src/main/java/com/wjy/snmp/receive) 中，可以自定义实现接口来处理 `PDU` 数据，参考 [接收示例](https://github.com/handsomestWei/snmp-tool/blob/main/src/test/java/com/wjy/snmp/SnmpV2Test.java)

```java
// 示例：启动SNMP接收器
SnmpReceiver receiver = new SnmpReceiver();
receiver.setCommunity("public");
receiver.setPort(161);
receiver.setHandler(new BizSnmpReceiveHandler());
receiver.start();
```

### SNMP设备模拟器
位于 [simulator包](https://github.com/handsomestWei/snmp-tool/tree/main/src/main/java/com/wjy/snmp/simulator) 中。支持海量设备模拟，只需提供oid文件和简单配置即可使用。
```sh
## 示例：运行SNMP设备模拟器
java -jar ./snmp-tool-1.0.0.jar
```

### MIB解析
位于 [mib包](https://github.com/handsomestWei/snmp-tool/tree/main/src/main/java/com/wjy/snmp/mib) 中，参考 [解析示例](https://github.com/handsomestWei/snmp-tool/blob/main/src/test/java/com/wjy/snmp/mib/SimpleMibParserTest.java)

```java
// 示例：解析MIB文件
SimpleMibParser parser = new SimpleMibParser();
MibTree tree = parser.parse("test.mib");
```

### 工具类
[SnmpHelper](https://github.com/handsomestWei/snmp-tool/blob/main/src/main/java/com/wjy/snmp/SnmpHelper.java) 支持将`snmp4j`数据结构转换为Java对象。
