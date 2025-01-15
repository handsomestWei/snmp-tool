# snmp-simulator
snmp设备模拟程序。可用于模拟snmp设备，接收服务端指令并响应。

## snmp指令支持

+ get
+ set
+ getbulk （使用限制）目前只支持取第一个oid

## 使用说明

### 运行命令
```sh
java -jar ./snmp-tool-1.0.0.jar
```
#### 命令参数说明
+ -ip 监听ip。默认0.0.0.0
+ -p 监听端口号。snmp协议默认使用161
+ -pool 模拟设备数量。对应消息并发处理线程数，默认200
+ -conf 配置文件`simulator-config.properties`全路径。默认和模拟程序同级
+ -dir oid数据文件目录`/data`。默认和模拟程序同级

### 配置文件说明

使用kv键值对定义

+ key 产品的根oid
+ value 自定义处理器参数配置，参数逗号分割，参数项：
    + 【0】 自定义处理器类名。类的class文件要放置在模拟程序的`com.wjy.snmp.simulator.ctm`包路径下
    + 【1】 随机网络延迟种子。使用随机数模拟网络抖动，可为空
    + 【2】 随机异常种子。使用随机数模拟异常发生概率，可为空

### oid数据文件说明
用于模拟数据返回，放置在`/data`目录下
+ 文件名称 <产品的根oid>_snmp.csv，后缀固定
+ 文件格式 csv，存放oid的kv值，单行逗号分隔。
+ 文件单行格式 前两列固定为oid、oid值。后续列可自定义并支持自定义处理。

## 扩展开发

1、在`com.wjy.snmp.simulator.ctm`包下，实现`AbsSimulatorReceiveHandler`接口      
2、在`simulator-config.properties`配置文件登记kv键值对`<产品的根oid>=<配置参数>`   
3、从真实设备获取全量的有序的oid键值对，录入到`<产品的根oid>_snmp.csv`数据文件，并放置在`/data`目录下   