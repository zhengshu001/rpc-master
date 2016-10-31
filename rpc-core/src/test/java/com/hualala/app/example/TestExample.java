package com.hualala.app.example;

import com.hualala.core.rpc.RpcProtoGenerator;
import org.junit.Test;

/**
 * Created by xiangbin on 2016/10/26.
 */
public class TestExample {

    @Test
    public void generatorProto() {
        RpcProtoGenerator.generate(Example.class, "example");
    }
}
