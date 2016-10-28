package com.hualala.core.service.config.mybatis;

import com.hualala.commons.mybatis.item.BaseItem;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.Properties;

/**
 * Created by xiangbin on 2016/10/28.
 */
@Intercepts({@Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}
)})
public class ActionInterceptor implements Interceptor {

    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement)invocation.getArgs()[0];
        Object param = invocation.getArgs()[1];
        if(ms == null) {
            return invocation.proceed();
        } else {
            long now;
            BaseItem e;
            if(ms.getSqlCommandType().equals(SqlCommandType.INSERT) && param != null && param instanceof BaseItem) {
                now = this.getFormattedTimeStamp(DateTime.now());
                e = (BaseItem)param;
                if(e.getCreateTime() == null) {
                    e.setCreateTime(Long.valueOf(now));
                }

                if(e.getActionTime() == null) {
                    e.setActionTime(Long.valueOf(now));
                }

                if(e.getAction() == null) {
                    e.setAction(BaseItem.Action.INSERT.getValue());
                }
            }

            if(ms.getSqlCommandType().equals(SqlCommandType.UPDATE) && param != null) {
                if (param instanceof BaseItem) {
                    now = this.getFormattedTimeStamp(DateTime.now());
                    e = (BaseItem) param;
                    if (e.getActionTime() == null) {
                        e.setActionTime(Long.valueOf(now));
                    }

                    if (e.getAction() == null) {
                        e.setAction(BaseItem.Action.UPDATE.getValue());
                    }
                } else if (param instanceof Map) {
                    now = this.getFormattedTimeStamp(DateTime.now());
                    Map m = (Map)param;
                    if (!m.containsKey("action")) {
                        m.put("action", BaseItem.Action.UPDATE.getValue());
                    }
                    if (!m.containsKey("actionTime")) {
                        m.put("actionTime", now);
                    }
                }
            }

            return invocation.proceed();
        }
    }

    public Object plugin(Object target) {
        return target instanceof Executor ? Plugin.wrap(target, this):target;
    }

    public void setProperties(Properties properties) {
    }

    private long getFormattedTimeStamp(DateTime time) {
        return (long)time.getYear() * 10000000000L + (long)time.getMonthOfYear() * 100000000L + (long)time.getDayOfMonth() * 1000000L + (long)time.getHourOfDay() * 10000L + (long)time.getMinuteOfHour() * 100L + (long)time.getSecondOfMinute();
    }
}
