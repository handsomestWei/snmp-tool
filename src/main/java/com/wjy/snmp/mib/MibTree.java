package com.wjy.snmp.mib;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * mib oid树
 *
 * @author weijiayu
 * @date 2024/11/29 14:27
 */
@Data
public class MibTree {

    private String oid;
    private String name;
    private List<MibTreeObjNode> objNodeList = new ArrayList<>();

}
