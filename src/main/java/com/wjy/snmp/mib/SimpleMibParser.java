package com.wjy.snmp.mib;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手搓简单mib文件解析器。使用cederberg/mibble（基于GPL-2.0开源协议）会对分发有限制
 *
 * @author weijiayu
 * @date 2024/11/29 11:00
 */
public class SimpleMibParser {

    /**
     * 通用oid前缀，例：
     * 1.3.6.1.2.1 表示Internet标准管理信息库
     * 1.3.6.1.4.1 表示ISO标准化组织下的私有企业管理信息库
     * 1.3.6.1.4.1.42578 表示某个特定企业的私有MIB
     */
    private static final String OID_PREFIX = "1.3.6.1.4.1";

    // 正则：ans.1语法赋值表达式。例::= { rs485pdu1 7 }
    private static final String REGEX_ANS_1_SET_VALUE = "::=\\s*\\{\\s*([a-zA-Z0-9]+)\\s+([0-9]+)\\s*\\}";
    // 注释符
    private static final String COMMENT_SYMBOL = "--";
    // 赋值符
    private static final String ANS_1_SET_SYMBOL = "::=";
    private static final String ANS_1_OBJ_IDENTIFIER_SYMBOL = "OBJECT IDENTIFIER";
    private static final String ANS_1_OBJ_TYPE_SYMBOL = "OBJECT-TYPE";
    private static final String ANS_1_OBJ_TYPE_SYNTAX_SYMBOL = "SYNTAX";
    private static final String ANS_1_OBJ_TYPE_ACCESS_SYMBOL = "MAX-ACCESS";
    private static final String ANS_1_OBJ_TYPE_DESCRIPTION_SYMBOL = "DESCRIPTION";

    /**
     * mib定义层级默认三层，root-object-type
     *
     * @param inputStream mib流
     * @return com.wjy.snmp.mib.MibTree oid节点树
     * @date 2024/11/29 11:28
     */
    public static MibTree parse(InputStream inputStream) throws Exception {
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader reader =
                     new BufferedReader(inputStreamReader)) {
            MibTree rootNode = new MibTree();
            // 对象缓存，解析对象类型时用于查找关联的对象
            HashMap<String, MibTreeObjNode> objNodeHashMap = new HashMap<>();

            // 逐行顺序解析
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (isCommentLine(line)) {
                    continue;
                }
                if (line.contains("IMPORTS")) {
                    // 跳过import导入定义
                    skipImport(reader);
                    continue;
                }
                if (line.contains("MODULE-IDENTITY")) {
                    // 解析模块定义，获取树根节点
                    parseModuleIdentity(reader, line, rootNode);
                    continue;
                }
                if (line.contains(ANS_1_OBJ_IDENTIFIER_SYMBOL)) {
                    // 解析OBJECT IDENTIFIER对象定义
                    parseObjIdentifier(line, rootNode, objNodeHashMap);
                    continue;
                }
                if (line.contains(ANS_1_OBJ_TYPE_SYMBOL)) {
                    // 解析OBJECT-TYPE各对象属性定义
                    parseObjType(reader, line, objNodeHashMap);
                }
            }
            return rootNode;
        }
    }

    private static void skipImport(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                // TODO 默认结束
                return;
            }
        }
    }

    private static void parseModuleIdentity(BufferedReader reader,
                                            String line, MibTree rootNode) throws IOException {
        line = line.trim();
        String moduleName = line.split("MODULE-IDENTITY")[0].trim();
        rootNode.setName(moduleName);
        String nodeIndex = "";
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (isCommentLine(line)) {
                continue;
            }
            if (line.contains(ANS_1_OBJ_IDENTIFIER_SYMBOL)) {
                // root oid
                Map.Entry<String, Map.Entry<String, String>> entry = parseObjIdentifierLine(line);
                rootNode.setOid(OID_PREFIX + "." + entry.getValue().getValue() + "." + nodeIndex);
                break;
            }
            if (line.contains(ANS_1_SET_SYMBOL)) {
                Map.Entry<String, String> entry = parseKv(line);
                nodeIndex = entry.getValue();
            }
        }
    }

    // 解析对象定义。例ippdu1 OBJECT IDENTIFIER ::= { sysDetails 1 }
    private static void parseObjIdentifier(String line, MibTree rootNode,
                                           HashMap<String, MibTreeObjNode> objNodeHashMap) {
        String[] arr = line.split(ANS_1_OBJ_IDENTIFIER_SYMBOL);
        String objName = arr[0].trim();
        String objIndex = parseKv(arr[1]).getValue();
        MibTreeObjNode objNode = new MibTreeObjNode(rootNode.getOid() + "." + objIndex, objName,
                new ArrayList<>());
        // 默认mib文件已经按顺序定义
        rootNode.getObjNodeList().add(objNode);
        objNodeHashMap.put(objName, objNode);
    }

    private static Map.Entry<String, Map.Entry<String, String>> parseObjIdentifierLine(String line) {
        line = line.trim();
        HashMap<String, Map.Entry<String, String>> rs = new HashMap<>();
        String[] arr = line.split(ANS_1_OBJ_IDENTIFIER_SYMBOL);
        rs.put(arr[0].trim(), parseKv(arr[1]));
        return rs.entrySet().iterator().next();
    }

    // 解析对象类型定义
    private static void parseObjType(BufferedReader reader, String line, HashMap<String,
            MibTreeObjNode> objNodeHashMap) throws IOException {
        line = line.trim();
        MibTreeObjTypeNode typeNode = new MibTreeObjTypeNode();
        typeNode.setName(line.split(ANS_1_OBJ_TYPE_SYMBOL)[0].trim());
        while ((line = reader.readLine()) != null) {
            if (isCommentLine(line)) {
                continue;
            }
            if (line.contains(ANS_1_OBJ_TYPE_SYNTAX_SYMBOL)) {
                typeNode.setSyntax(line.split(ANS_1_OBJ_TYPE_SYNTAX_SYMBOL)[1].trim());
                continue;
            }
            if (line.contains(ANS_1_OBJ_TYPE_ACCESS_SYMBOL)) {
                typeNode.setAccess(line.split(ANS_1_OBJ_TYPE_ACCESS_SYMBOL)[1].trim());
                continue;
            }
            if (line.contains(ANS_1_OBJ_TYPE_DESCRIPTION_SYMBOL)) {
                typeNode.setDescription(line.split(ANS_1_OBJ_TYPE_DESCRIPTION_SYMBOL)[1].trim());
                continue;
            }
            if (line.contains(ANS_1_SET_SYMBOL)) {
                Map.Entry<String, String> entry = parseKv(line);
                MibTreeObjNode parentObjNode = objNodeHashMap.get(entry.getKey());
                // 默认mib文件已经按顺序定义
                parentObjNode.getTypeNodeList().add(typeNode);
                // 叶子节点oid拼接父级后，填充.0
                typeNode.setOid(parentObjNode.getOid() + "." + entry.getValue() + ".0");
                // TODO 默认结束
                return;
            }
        }
    }

    // 解析赋值语句。例::= { enterprises 42578 }
    private static Map.Entry<String, String> parseKv(String line) {
        line = line.trim();
        HashMap<String, String> kvMap = new HashMap<>();
        Pattern pattern = Pattern.compile(REGEX_ANS_1_SET_VALUE);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            kvMap.put(matcher.group(1).trim(), matcher.group(2).trim());
            break;
        }
        return kvMap.entrySet().iterator().next();
    }

    // 跳过注释
    private static boolean isCommentLine(String line) {
        return line.startsWith(COMMENT_SYMBOL);
    }
}
