package org.mtf.shortlink.project.dto.req;

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
    private Date validDate;
    private String describe;
}
