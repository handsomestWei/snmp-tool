package com.wjy.snmp.receive;

import lombok.Data;

/**
 * 转发信息。利用特定规约，从snmp团体名中解析出转发信息
 *
 * @author weijiayu
 * @date 2025/10/16 16:00
 */
@Data
public class RelayInfo {

    private String ip;
    private String port;
    private String communityName;

}
