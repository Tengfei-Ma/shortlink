package org.mtf.shortlink.admin.remote.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 短链接请求参数实体
 */
@Data
public class ShortlinkCreateReqDTO {
    private String domain;
    private String originUrl;
    private String gid;
    private Integer createType;
    private Integer validDateType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date validDate;
    private String describe;
}
