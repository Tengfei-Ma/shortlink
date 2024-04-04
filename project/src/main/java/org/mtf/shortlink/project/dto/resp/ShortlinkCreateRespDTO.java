package org.mtf.shortlink.project.dto.resp;

import lombok.Data;

@Data
public class ShortlinkCreateRespDTO {
    private String fullShortUrl;
    private String originUrl;
    private String gid;
}
