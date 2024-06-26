package org.mtf.shortlink.admin.remote.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
/**
 * 短链接修改参数
 */
@Data
public class ShortlinkUpdateReqDTO {
    private String originUrl;
    private String fullShortUrl;
    /**
     * 原始分组标识
     */
    private String originGid;
    private String gid;
    private Integer validDateType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validDate;
    private String describe;
}
