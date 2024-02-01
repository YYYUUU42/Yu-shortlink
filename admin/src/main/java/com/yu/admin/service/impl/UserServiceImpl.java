package com.yu.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.admin.common.convention.exception.ClientException;
import com.yu.admin.common.convention.exception.ServiceException;
import com.yu.admin.common.enums.UserErrorCodeEnum;
import com.yu.admin.dao.entity.UserDO;
import com.yu.admin.dao.mapper.UserMapper;
import com.yu.admin.dto.req.UserLoginReqDTO;
import com.yu.admin.dto.req.UserRegisterReqDTO;
import com.yu.admin.dto.req.UserUpdateReqDTO;
import com.yu.admin.dto.resp.UserLoginRespDTO;
import com.yu.admin.dto.resp.UserRespDTO;
import com.yu.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.yu.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.yu.admin.common.enums.UserErrorCodeEnum.USER_SAVE_ERROR;


@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

	private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

	private final RedissonClient redissonClient;

	/**
	 * 根据用户名查询用户信息
	 *
	 * @param username 用户名
	 * @return 用户返回实体
	 */
	@Override
	public UserRespDTO getUserByUsername(String username) {
		UserDO userDO = this.getOne(new LambdaQueryWrapper<UserDO>().eq(UserDO::getUsername, username));
		if (userDO == null) {
			throw new ServiceException(UserErrorCodeEnum.USER_NULL);
		}

		UserRespDTO result = new UserRespDTO();
		BeanUtils.copyProperties(userDO, result);
		return result;
	}

	/**
	 * 查询用户名是否存在
	 *
	 * @param username 用户名
	 * @return 用户名存在返回 True，不存在返回 False
	 */
	@Override
	public Boolean hasUsername(String username) {
		return !userRegisterCachePenetrationBloomFilter.contains(username);
	}

	/**
	 * 注册用户
	 *
	 * @param requestParam 注册用户请求参数
	 */
	@Override
	public void register(UserRegisterReqDTO requestParam) {
		if (hasUsername(requestParam.getUsername())) {
			throw new ServiceException(UserErrorCodeEnum.USER_NAME_EXIST);
		}

		RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
		try {
			if (lock.tryLock()) {
				UserDO userDO = BeanUtil.toBean(requestParam, UserDO.class);
				boolean save = this.save(userDO);
				if (!save) {
					throw new ClientException(USER_SAVE_ERROR);
				}

				userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 根据用户名修改用户
	 *
	 * @param requestParam 修改用户请求参数
	 */
	@Override
	public void update(UserUpdateReqDTO requestParam) {
		//从 request 中得到请求头中的token，判断用户信息是否匹配
		LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<UserDO>().eq(UserDO::getUsername, requestParam.getUsername());
		this.update(BeanUtil.toBean(requestParam, UserDO.class), queryWrapper);
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
