package com.wjy.snmp.mib;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * mib对象节点
 *
 * @author weijiayu
 * @date 2024/11/29 14:27
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MibTreeObjNode {

    private String oid;
    private String name;
    private List<MibTreeObjTypeNode> typeNodeList = new ArrayList<>();
}
