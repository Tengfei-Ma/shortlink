package org.mtf.shortlink.project.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.mtf.shortlink.project.common.convention.result.Result;
import org.mtf.shortlink.project.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkCreateRespDTO;

/**
 * 自定义流控策略
 */
public class CustomBlockHandler {

    public static Result<ShortlinkCreateRespDTO> createShortlinkBlockHandlerMethod(ShortlinkCreateReqDTO requestParam, BlockException exception) {
        return new Result<ShortlinkCreateRespDTO>().setCode("B100000").setMessage("当前访问网站人数过多，请稍后再试...");
    }
}
