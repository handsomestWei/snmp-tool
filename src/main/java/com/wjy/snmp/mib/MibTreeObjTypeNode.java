package com.wjy.snmp.mib;

import lombok.Data;

/**
 * mib 对象的属性节点。叶子
 *
 * @author weijiayu
 * @date 2024/11/29 14:29
 */
@Data
public class MibTreeObjTypeNode {

    private String oid;
    private String name;
    private String syntax;
    private String access;
    private String description;
}
