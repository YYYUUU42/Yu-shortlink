package com.yu.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.admin.common.biz.user.UserContext;
import com.yu.admin.common.convention.exception.ClientException;
import com.yu.admin.common.database.BaseDO;
import com.yu.admin.dao.entity.GroupDO;
import com.yu.admin.dao.mapper.GroupMapper;
import com.yu.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.yu.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.yu.admin.dto.resp.ShortLinkGroupRespDTO;
import com.yu.admin.service.GroupService;
import com.yu.admin.toolkit.RandomGenerator;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.yu.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;

/**
 * @author yu
 * @description 短链接分组接口实现层
 * @date 2024-02-06
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

	private final RedissonClient redissonClient;

	@Value("${short-link.group.max-num}")
	private Integer groupMaxNum;

	/**
	 * 新增短链接分组
	 *
	 * @param groupName 短链接分组名
	 */
	@Override
	public void saveGroup(String groupName) {
		saveGroup(UserContext.getUsername(), groupName);
	}

	/**
	 * 新增短链接分组
	 *
	 * @param username  用户名
	 * @param groupName 短链接分组名
	 */
	@Override
	public void saveGroup(String username, String groupName) {
		RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username));
		lock.lock();
		try {
			List<GroupDO> groupDOList = baseMapper.selectList(new LambdaQueryWrapper<GroupDO>()
					.eq(GroupDO::getUsername, username));

			if (CollUtil.isNotEmpty(groupDOList) && groupDOList.size() == groupMaxNum) {
				throw new ClientException(String.format("已超出最大分组数：%d", groupMaxNum));
			}

			String gid;
			//不断得到 gid ，直到不存在
			//todo 优化 gid 的判断
			do {
				gid = RandomGenerator.generateRandom();
			} while (!hasGid(username, gid));

			GroupDO groupDO = GroupDO.builder()
					.gid(gid)
					.sortOrder(0)
					.username(username)
					.name(groupName)
					.build();
			baseMapper.insert(groupDO);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 查询用户短链接分组集合
	 *
	 * @return 用户短链接分组集合
	 */
	@Override
	public List<ShortLinkGroupRespDTO> listGroup() {
		List<GroupDO> groupDOList = baseMapper.selectList(new LambdaQueryWrapper<GroupDO>()
				.eq(GroupDO::getUsername, UserContext.getUsername())
				.orderByDesc(GroupDO::getSortOrder, BaseDO::getUpdateTime));

		List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOList = BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);


		return shortLinkGroupRespDTOList;
	}

	/**
	 * 修改短链接分组
	 *
	 * @param requestParam 修改链接分组参数
	 */
	@Override
	public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
		LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
				.eq(GroupDO::getUsername, UserContext.getUsername())
				.eq(GroupDO::getGid, requestParam.getGid());

		GroupDO groupDO = new GroupDO();
		groupDO.setName(requestParam.getName());
		baseMapper.update(groupDO, updateWrapper);
	}

	/**
	 * 删除短链接分组
	 *
	 * @param gid 短链接分组标识
	 */
	@Override
	public void deleteGroup(String gid) {
		LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
				.eq(GroupDO::getUsername, UserContext.getUsername())
				.eq(GroupDO::getGid, gid)
				.eq(GroupDO::getDelFlag, 0);
		GroupDO groupDO = new GroupDO();
		//可以直接使用 remove，逻辑删除
		groupDO.setDelFlag(1);
		baseMapper.update(groupDO, updateWrapper);
	}

	/**
	 * 短链接分组排序
	 *
	 * @param requestParam 短链接分组排序参数
	 */
	@Override
	public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
		requestParam.forEach(item -> {
			GroupDO groupDO = GroupDO.builder()
					.sortOrder(item.getSortOrder())
					.build();
			LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
					.eq(GroupDO::getUsername, UserContext.getUsername())
					.eq(GroupDO::getGid, item.getGid())
					.eq(GroupDO::getDelFlag, 0);
			baseMapper.update(groupDO, updateWrapper);
		});
	}

	/**
	 * 判断当前 gid 是否已经存在了
	 * @param username
	 * @param gid
	 * @return
	 */
	private boolean hasGid(String username, String gid) {
		LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
				.eq(GroupDO::getGid, gid)
				.eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()));
		GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
		return hasGroupFlag == null;
	}
}
