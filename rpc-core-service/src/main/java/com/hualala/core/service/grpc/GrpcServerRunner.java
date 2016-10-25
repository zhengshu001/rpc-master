package com.hualala.core.service.grpc;

import com.hualala.grpc.GRpcServerRunner;
import com.hualala.grpc.autoconfigure.GRpcServerProperties;
import com.hualala.grpc.logging.LoggingServerInterceptor;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Created by xiangbin on 2016/8/12.
 */
public class GrpcServerRunner implements CommandLineRunner, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(GRpcServerRunner.class);
    private static final String bindServiceMethodName = "bindService";
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private GRpcServerProperties gRpcServerProperties;

    private Server server;

    public GrpcServerRunner() {
    }

    public void run(String... strings) throws Exception {
        LOG.info("Starting gRPC Server ...");
        ServerBuilder serverBuilder = ServerBuilder.forPort(this.gRpcServerProperties.getPort());

        Map<String, BindableService> rpcServices = this.applicationContext.getBeansOfType(BindableService.class);

        Iterator<String> iterator = rpcServices.keySet().iterator();
        while (iterator.hasNext()) {
            String service = iterator.next();
            //LOG.info("rpcService [" + service + "] implements [" + rpcServices + "]");
            BindableService rpcService = rpcServices.get(service);
            ServerServiceDefinition serviceDefinition = rpcService.bindService();
            ServerServiceDefinition loggingServiceDefinition = ServerInterceptors.intercept(serviceDefinition, new ServerInterceptor[]{new LoggingServerInterceptor()});
            serverBuilder.addService(loggingServiceDefinition);
            LOG.info("\'{}\' service has been registered.", loggingServiceDefinition.getServiceDescriptor().getName());
        }
        this.server = serverBuilder.build().start();
        LOG.info("gRPC Server started, listening on port {}.", Integer.valueOf(this.gRpcServerProperties.getPort()));
        this.startDaemonAwaitThread();
    }

    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread() {
            public void run() {
                try {
                    GrpcServerRunner.this.server.awaitTermination();
                } catch (InterruptedException var2) {
                    GrpcServerRunner.LOG.error("gRPC server stopped.", var2);
                }

            }
        };
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    public void destroy() throws Exception {
        LOG.info("Shutting down gRPC server ...");
        Optional.ofNullable(this.server).ifPresent(Server::shutdown);
        LOG.info("gRPC server stopped.");
    }
}
