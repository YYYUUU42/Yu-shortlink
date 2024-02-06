package com.yu.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.project.dao.entity.ShortLinkDO;
import com.yu.project.dto.req.ShortLinkCreateReqDTO;
import com.yu.project.dto.resp.ShortLinkCreateRespDTO;

/**
 * @author yu
 * @description 短链接接口层
 * @date 2024-02-06
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

	/**
	 * 创建短链接
	 *
	 * @param requestParam 创建短链接请求参数
	 * @return 短链接创建信息
	 */
	ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

}
