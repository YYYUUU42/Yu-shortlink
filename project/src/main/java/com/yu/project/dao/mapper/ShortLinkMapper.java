package com.yu.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yu.project.dao.entity.ShortLinkDO;
import com.yu.project.dto.req.ShortLinkPageReqDTO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yu
 * @description 短链接持久层
 * @date 2024-02-06
 */
@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

	/**
	 * 分页统计短链接
	 *
	 * @param requestParam
	 * @return
	 */
	IPage<ShortLinkDO> pageLink(ShortLinkPageReqDTO requestParam);
}
