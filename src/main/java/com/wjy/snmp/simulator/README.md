# snmp-simulator
snmp设备模拟程序。可用于模拟snmp设备，接收服务端指令并响应。

## 简介
- 海量设备模拟：无需真实设备，通过模拟，您可以在没有真实设备的情况下测试基于SNMP的网络管理应用程序的功能和性能。
    - 支持同时模拟运行海量设备。
    - 内置随机网络抖动和断连功能，模拟更真实的网络环境。
- 一键配置：放置oid文件，在[simulator-config.properties](/simulator-config.properties)配置文件添加设备，即可轻松完成设备模拟。
- 可扩展：提供接口，支持自定义扩展开发，满足更丰富的数据模拟需求。
- 开源内部引擎：基于开源的SNMP4j作为内部引擎，确保稳定性和可靠性。
- 跨平台运行：支持在linux、windows系统部署运行。

## snmp指令支持
+ get
+ set
+ getbulk 支持取第一个oid

## 使用说明
### 运行命令
```sh
java -jar ./snmp-tool-1.0.0.jar
```
注意依赖的jar包放在同级`lib`目录内。

#### 命令参数说明
+ -ip 监听ip。默认0.0.0.0
+ -p 监听端口号。snmp协议默认使用161
+ -pool 模拟设备数量。对应消息并发处理线程数，默认200
+ -conf 配置文件`simulator-config.properties`全路径。默认和模拟程序同级
+ -dir oid数据文件目录`/data`。默认和模拟程序同级

### 配置文件说明
配置文件内容[参考](/simulator-config.properties)

使用kv键值对定义
+ key 产品的根oid。
+ value 自定义处理器参数配置，参数逗号分割，参数项：
    + 【0】 自定义处理器类名。类的class文件要放置在模拟程序的`com.wjy.snmp.simulator.ctm`包路径下
    + 【1】 随机网络延迟种子。使用随机数模拟网络抖动，可为空
    + 【2】 随机异常种子。使用随机数模拟异常发生概率，可为空

使用根oid来区分不同的设备产品类型，不区分单个设备。上层采集多设备时,只需把ip指向到模拟程序。

### oid数据文件说明
用于模拟数据返回，放置在运行同级的`data`目录下
+ 文件名称 <产品的根oid>_snmp.csv，后缀固定
+ 文件格式 csv，存放oid的kv值，单行逗号分隔。
+ 文件单行格式 前两列固定为oid、oid值。后续列可自定义并支持自定义处理。

## 扩展开发
支持扩展开发，满足更丰富的数据模拟需求。开发流程：
+ 在`com.wjy.snmp.simulator.ctm`包下，实现`AbsSimulatorReceiveHandler`接口
+ 在`simulator-config.properties`配置文件登记kv键值对`<产品的根oid>=<配置参数>`
+ 从真实设备获取全量的有序的oid键值对，录入到`<产品的根oid>_snmp.csv`数据文件，并放置在[/data](/data/)目录下
