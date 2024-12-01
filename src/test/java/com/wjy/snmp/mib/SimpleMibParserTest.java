package com.wjy.snmp.mib;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class SimpleMibParserTest {

    @Test
    public void parse() throws Exception {
        // file path: /<projectPath>/test/resources/test.mib
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("test.mib");
        MibTree tree = SimpleMibParser.parse(inputStream);
        System.out.println(tree);
    }
}