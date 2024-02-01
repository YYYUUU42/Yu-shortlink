package com.yu.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.admin.dao.entity.UserDO;
import com.yu.admin.dao.mapper.UserMapper;
import com.yu.admin.dto.req.UserLoginReqDTO;
import com.yu.admin.dto.req.UserRegisterReqDTO;
import com.yu.admin.dto.req.UserUpdateReqDTO;
import com.yu.admin.dto.resp.UserLoginRespDTO;
import com.yu.admin.dto.resp.UserRespDTO;
import com.yu.admin.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
	/**
	 * 根据用户名查询用户信息
	 *
	 * @param username 用户名
	 * @return 用户返回实体
	 */
	@Override
	public UserRespDTO getUserByUsername(String username) {
		return null;
	}

	/**
	 * 查询用户名是否存在
	 *
	 * @param username 用户名
	 * @return 用户名存在返回 True，不存在返回 False
	 */
	@Override
	public Boolean hasUsername(String username) {
		return null;
	}

	/**
	 * 注册用户
	 *
	 * @param requestParam 注册用户请求参数
	 */
	@Override
	public void register(UserRegisterReqDTO requestParam) {

	}

	/**
	 * 根据用户名修改用户
	 *
	 * @param requestParam 修改用户请求参数
	 */
	@Override
	public void update(UserUpdateReqDTO requestParam) {

	}

	/**
	 * 用户登录
	 *
	 * @param requestParam 用户登录请求参数
	 * @return 用户登录返回参数 Token
	 */
	@Override
	public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
		return null;
	}

	/**
	 * 检查用户是否登录
	 *
	 * @param username 用户名
	 * @param token    用户登录 Token
	 * @return 用户是否登录标识
	 */
	@Override
	public Boolean checkLogin(String username, String token) {
		return null;
	}

	/**
	 * 退出登录
	 *
	 * @param username 用户名
	 * @param token    用户登录 Token
	 */
	@Override
	public void logout(String username, String token) {

	}
}
