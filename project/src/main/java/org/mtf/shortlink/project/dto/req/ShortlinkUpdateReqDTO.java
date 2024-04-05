package org.mtf.shortlink.project.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

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
