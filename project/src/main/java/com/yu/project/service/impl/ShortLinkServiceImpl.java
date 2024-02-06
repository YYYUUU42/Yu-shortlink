package com.yu.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.project.common.convention.exception.ServiceException;
import com.yu.project.dao.entity.ShortLinkDO;
import com.yu.project.dao.entity.ShortLinkGotoDO;
import com.yu.project.dao.mapper.ShortLinkMapper;
import com.yu.project.dto.req.ShortLinkCreateReqDTO;
import com.yu.project.dto.req.ShortLinkPageReqDTO;
import com.yu.project.dto.resp.ShortLinkCreateRespDTO;
import com.yu.project.dto.resp.ShortLinkPageRespDTO;
import com.yu.project.service.ShortLinkService;
import com.yu.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yu
 * @description 短链接接口实现层
 * @date 2024-02-06
 */
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

	private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

	@Value("${short-link.domain.default}")
	private String createShortLinkDefaultDomain;

	/**
	 * 创建短链接
	 *
	 * @param requestParam 创建短链接请求参数
	 * @return 短链接创建信息
	 */
	@Override
	public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
		//生成完整的短链接
		String shortLinkSuffix = generateSuffix(requestParam);
		String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
				.append("/")
				.append(shortLinkSuffix)
				.toString();

		//短链接实体
		ShortLinkDO shortLinkDO = ShortLinkDO.builder()
				.domain(createShortLinkDefaultDomain)
				.originUrl(requestParam.getOriginUrl())
				.gid(requestParam.getGid())
				.createdType(requestParam.getCreatedType())
				.validDateType(requestParam.getValidDateType())
				.validDate(requestParam.getValidDate())
				.describe(requestParam.getDescribe())
				.shortUri(shortLinkSuffix)
				.enableStatus(0)
				.totalPv(0)
				.totalUv(0)
				.totalUip(0)
				.delTime(0L)
				.fullShortUrl(fullShortUrl)
				.build();

		//mysql 唯一索引兜底
		try {
			baseMapper.insert(shortLinkDO);
		} catch (DuplicateKeyException ex) {
			throw new ServiceException(String.format("短链接：%s 生成重复", fullShortUrl));
		}

		//布隆过滤器添加短链接
		shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);

		return ShortLinkCreateRespDTO.builder()
				.fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
				.originUrl(requestParam.getOriginUrl())
				.gid(requestParam.getGid())
				.build();
	}

	/**
	 * 分页查询短链接
	 *
	 * @param requestParam 分页查询短链接请求参数
	 * @return 短链接分页返回结果
	 */
	@Override
	public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
		IPage<ShortLinkDO> resultPage = baseMapper.pageLink(requestParam);
		return resultPage.convert(each -> {
			ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
			result.setDomain("http://" + result.getDomain());
			return result;
		});
	}


	/**
	 * 生成短链接
	 *
	 * @param requestParam
	 * @return
	 */
	private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
		int customGenerateCount = 0;
		String shorUri;
		while (true) {
			if (customGenerateCount > 10) {
				throw new ServiceException("短链接频繁生成，请稍后再试");
			}
			String originUrl = requestParam.getOriginUrl();
			originUrl += UUID.randomUUID().toString();
			shorUri = HashUtil.hashToBase62(originUrl);

			//在布隆过滤器上判断该短链接是否存在
			if (!shortUriCreateCachePenetrationBloomFilter.contains(createShortLinkDefaultDomain + "/" + shorUri)) {
				break;
			}
			customGenerateCount++;
		}
		return shorUri;
	}
}
