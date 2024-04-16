package org.mtf.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Optional;

import static org.mtf.shortlink.project.common.constant.ShortlinkConstant.PERMANENT_DEFAULT_CACHE_VALID_DATE;

/**
 * 短连接工具类
 */
public class LinkUtil {
    /**
     * 返回短链接应设置的缓存有效时间毫秒数
     * @param validDate 短链接有效期
     * @return 缓存应设置毫秒数
     */
    public static long getShortlinkCacheValidTime(Date validDate) {
        return Optional.ofNullable(validDate)
                .map(date -> DateUtil.between(new Date(), date, DateUnit.MS))
                .orElse(PERMANENT_DEFAULT_CACHE_VALID_DATE);
    }
    public static String getOs(HttpServletRequest request){
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("windows")) {
            return "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            return "Mac OS";
        } else if (userAgent.toLowerCase().contains("linux")) {
            return "Linux";
        } else if (userAgent.toLowerCase().contains("android")) {
            return "Android";
        } else if (userAgent.toLowerCase().contains("iphone") || userAgent.toLowerCase().contains("ipad")) {
            return "iOS";
        } else {
            return "unknown";
        }
    }
}
