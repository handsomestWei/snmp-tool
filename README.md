# snmp-tool
snmp tool for send, receive, device simulator, mib parse 

## About
+ snmp send and receive be based on [snmp4j](https://snmp4j.org/).
+ the mib parse is custom implementation and avoiding [mibble](https://github.com/cederberg/mibble) `GPL-2.0` risks.

## Useage
### snmp send
in [snmp package](https://github.com/handsomestWei/snmp-tool/tree/main/src/main/java/com/wjy/snmp) and suggested use [SnmpV2](https://github.com/handsomestWei/snmp-tool/blob/main/src/main/java/com/wjy/snmp/SnmpV2.java)

### snmp receive
in [receive package](https://github.com/handsomestWei/snmp-tool/tree/main/src/main/java/com/wjy/snmp/receive), can custom implementation the interface to handle `PDU` data, refer to the [receive demo](https://github.com/handsomestWei/snmp-tool/blob/main/src/test/java/com/wjy/snmp/SnmpV2Test.java)

### snmp simulator
in [simulator package](https://github.com/handsomestWei/snmp-tool/tree/main/src/main/java/com/wjy/snmp/simulator), can simulator snmp device

### mib parse
in [mib package](https://github.com/handsomestWei/snmp-tool/tree/main/src/main/java/com/wjy/snmp/mib), refer to the [parse demo](https://github.com/handsomestWei/snmp-tool/blob/main/src/test/java/com/wjy/snmp/mib/SimpleMibParserTest.java)

### util
the [SnmpHelper](https://github.com/handsomestWei/snmp-tool/blob/main/src/main/java/com/wjy/snmp/SnmpHelper.java) support convert snmp4j data structure to java.