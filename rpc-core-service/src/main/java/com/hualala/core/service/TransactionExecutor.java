package com.hualala.core.service;

import com.hualala.core.base.ResultInfo;
import com.hualala.core.config.message.MessageInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Created by xiangbin on 2016/10/9.
 */
@Aspect
@Configuration
public class TransactionExecutor {

    private Logger logger = LoggerFactory.getLogger(TransactionExecutor.class);

    @Autowired
    private PlatformTransactionManager platformTransactionManager;
    @Autowired
    private MessageInfo messageInfo;

    @Pointcut("@annotation (com.hualala.core.base.Transaction)")
    public void transactionPointcut(){
    }

    @Around("transactionPointcut()")
    public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        logger.info("transaction start ....");
        long startTime = System.currentTimeMillis();
        TransactionStatus status = platformTransactionManager.getTransaction(def);
        ResultInfo resultInfo;
        try {
            Object resultObject = joinPoint.proceed();
            if (logger.isDebugEnabled()) {
                logger.debug("result type [" + resultObject.getClass() + "]");
            }
            if (resultObject instanceof ResultInfo) {
                resultInfo = (ResultInfo)resultObject;
                if (!resultInfo.success()) {
                    platformTransactionManager.rollback(status);
                } else {
                    platformTransactionManager.commit(status);
                }
            } else {
                platformTransactionManager.commit(status);
            }
            return resultObject;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new IllegalTransactionStateException("transaction error", e);
//            resultInfo = messageInfo.getResultMessage(ErrorInfo.SERVICE_EXECUTE_ERROR, new Object[]{e.getMessage()});
//            platformTransactionManager.rollback(status);
//            return resultInfo;
        } finally {
            logger.info("transaction end cost [" + (System.currentTimeMillis() - startTime) + "]ms");
            if (!status.isCompleted()) {
                platformTransactionManager.rollback(status);
            }
        }
    }
}
