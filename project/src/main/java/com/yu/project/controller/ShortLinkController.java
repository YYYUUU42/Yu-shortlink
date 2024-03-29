package com.yu.project.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yu.project.common.convention.result.Result;
import com.yu.project.common.convention.result.Results;
import com.yu.project.dto.req.ShortLinkCreateReqDTO;
import com.yu.project.dto.req.ShortLinkPageReqDTO;
import com.yu.project.dto.resp.ShortLinkCreateRespDTO;
import com.yu.project.dto.resp.ShortLinkPageRespDTO;
import com.yu.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

	private final ShortLinkService shortLinkService;


	/**
	 * 创建短链接
	 */
	@PostMapping("/api/short-link/v1/create")
	public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
		return Results.success(shortLinkService.createShortLink(requestParam));
	}

	/**
	 * 分页查询短链接
	 */
	@GetMapping("/api/short-link/v1/page")
	public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
		return Results.success(shortLinkService.pageShortLink(requestParam));
	}
}
