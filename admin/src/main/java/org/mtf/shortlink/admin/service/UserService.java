package org.mtf.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mtf.shortlink.admin.dao.entity.UserDO;
import org.mtf.shortlink.admin.dto.req.UserLoginReqDTO;
import org.mtf.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.mtf.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.mtf.shortlink.admin.dto.resp.UserLoginRespDTO;
import org.mtf.shortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {
    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否可用
     * @param username 用户名
     * @return 可用返回true，不可用返回false
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     * @param requestParam 用户注册请求参数
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 用户修改信息
     * @param requestParam 用户修改请求参数
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     * @param requestParam 用户登录请求参数
     * @return 用户登录响应参数token
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 检查用户是否登录
     * @param username 用户名
     * @param token 用户名，用户token
     * @return 登录为true，未登录为false
     */
    Boolean checkLogin(String username, String token);
}
