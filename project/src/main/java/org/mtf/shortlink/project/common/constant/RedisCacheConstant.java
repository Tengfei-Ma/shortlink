package org.mtf.shortlink.project.common.constant;

/**
 * Redis Key 常量类
 */
public class RedisCacheConstant {
    /**
     * 短链接跳转前缀 Key
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link:goto:%s";
    /**
     * 短链接空值跳转前缀 Key，布隆过滤器误判不存在为存在，redis缓存一个空值，避免缓存穿透
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link:is_null:goto:%s";
    /**
     * 短链接跳转锁前缀 Key
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link:lock:goto:%s";
}
