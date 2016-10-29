package com.hualala.app.example;

import com.hualala.core.rpc.GrpcProtoGenerator;
import org.junit.Test;

/**
 * Created by xiangbin on 2016/10/26.
 */
public class TestExample {

    @Test
    public void generatorProto() {
        GrpcProtoGenerator.generate(Example.class, "example");
    }
}
