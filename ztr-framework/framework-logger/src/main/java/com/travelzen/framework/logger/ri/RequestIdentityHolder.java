package com.travelzen.framework.logger.ri;

import com.travelzen.framework.logger.core.ri.CallInfo;
import com.travelzen.framework.logger.core.util.IpUtil;
import com.travelzen.framework.logger.core.util.PropertiesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *  use class in framework-logger-core
 *
 */
@Deprecated
public class RequestIdentityHolder {

    private static Logger logger = LoggerFactory.getLogger(RequestIdentityHolder.class);
    private static ThreadLocal<CallInfo> holder = new ThreadLocal<>();
    private static Random RDM = new Random();
    public static String ip;
    public static String product;

    static {
        ip = IpUtil.getIP();
        Map propertyMap = PropertiesUtil.getProperty("properties/logback-variables.properties");
        String appName = (String) propertyMap.get("APP_NAME");
        product = (appName != null ? appName : "NULL");
    }

    public static CallInfo get() {
        return holder.get();
    }

    public static void init() {
        CallInfo callInfo = new CallInfo();
        callInfo.setRpid(genReqId());
        callInfo.setIp(ip);
        callInfo.setProduct(product);
        callInfo.setTime(new Date().getTime());
        set(callInfo);
    }

    private static void set(CallInfo callinfo) {
        holder.set(callinfo);
        org.slf4j.MDC.put("rpid", String.format("[rpid=%s]", callinfo.getRpid()));
    }

    public static void remove() {
        holder.remove();
        org.slf4j.MDC.remove("rpid");
    }

    /**
     * 向内读取数据时，将获取到的CallInfo设置到ThreadLocal
     *
     * @return
     */
    public static void setOnRead(CallInfo callInfo) {
        if (callInfo == null) {
            logger.warn("调用方缺失CallInfo信息");
        } else {
            List<String> ips = callInfo.getIps();
            List<String> products = callInfo.getProducts();
            List<Long> times = callInfo.getTimes();
            int index = 0;
            for (int i = 0; i < ips.size(); i++) {
                String ipTmp = ips.get(i);
                String productTmp = products.get(i);
                if (ip.equals(ipTmp) && product.equals(productTmp)) {
                    index = i;
                }
            }
            if (index > 0) {
                for (int i = index; i < ips.size(); i++) {
                    ips.remove(i);
                    products.remove(i);
                    times.remove(i);
                }
            }
            callInfo.setIp(ip);
            callInfo.setProduct(product);
            callInfo.setTime(new Date().getTime());
            set(callInfo);
        }
    }

    private static String genReqId() {
		return String.format("%08x", RDM.nextInt());
	}
}