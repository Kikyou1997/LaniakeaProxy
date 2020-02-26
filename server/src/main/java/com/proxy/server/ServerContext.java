package com.proxy.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kikyou
 * Created at 2020/2/20
 */
public class ServerContext {

    public static Map<Integer/*用户id*/, Long/*添加时间*/> idTimeMap = new ConcurrentHashMap<>();

    public static Map<Integer/*用户id*/, String/*用户名*/> idNameMap = new ConcurrentHashMap<>();

    public static Map<Integer/*用户id*/, byte[]/*iv*/> idIvMap = new ConcurrentHashMap<>();

}
