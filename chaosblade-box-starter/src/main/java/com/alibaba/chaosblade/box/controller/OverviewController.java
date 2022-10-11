package com.alibaba.chaosblade.box.controller;

import com.alibaba.chaosblade.box.annotation.LoginUser;
import com.alibaba.chaosblade.box.common.common.domain.BaseRequest;
import com.alibaba.chaosblade.box.common.common.domain.PageQueryResponse;
import com.alibaba.chaosblade.box.common.common.domain.user.ChaosUser;
import com.alibaba.chaosblade.box.model.RestResponseUtil;
import com.alibaba.chaosblade.box.service.model.RestResponse;
import com.alibaba.chaosblade.box.service.model.overview.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author sunpeng
 */
@RestController
@Api(tags = "首页概要信息查询模块")
public class OverviewController extends BaseController {

    @Resource
    private OverviewService overviewService;

    @ApiOperation(value = "演练概要信息查询", notes = "获取概览页面的演练概览数据")
    @PostMapping("UserExperimentOverviewInfo")
    public RestResponse<OverviewExperimentCount> userExperimentOverviewInfo(@LoginUser ChaosUser chaosUser, @RequestBody @ApiParam(value = "传入页数和大小") OverviewRequest overviewRequest) {
        overviewRequest.setUser(chaosUser);
        return RestResponseUtil.initWithServiceResponse(overviewService.getUserExperimentCount(overviewRequest));
    }


    @PostMapping("UserExperimentByDayOverviewInfo")
    @ApiOperation(value = "每日演练概要信息查询", notes = "获取概览页面的每日演练数据")
    public RestResponse<OverviewExperimentTask> userExperimentByDayOverviewInfo(@LoginUser ChaosUser chaosUser) {
        return RestResponseUtil.initWithServiceResponse(overviewService.getUserExperimentDayCount(chaosUser));
    }

    @PostMapping("UserAgentOverviewInfo")
    @ApiOperation(value = "探针概要信息查询", notes = "获取概览页面的探针数据")
    public RestResponse<OverviewAgent> userAgentOverviewInfo(@LoginUser ChaosUser chaosUser) {
        return RestResponseUtil.initWithServiceResponse(overviewService.getUserAgentCount(chaosUser));
    }

    @PostMapping("ProductMessageOverviewInfo")
    public RestResponse<OverviewProduct> productMessageOverviewInfo(@LoginUser ChaosUser chaosUser) {
        return RestResponseUtil.initWithServiceResponse(overviewService.getProductMessage(chaosUser));
    }

    @PostMapping("UserSceneOverview")
    @ApiOperation(value = "常用演练场景查询", notes = "概览页面的常用演练场景")
    public RestResponse<List<OverviewScene>> userSceneOverview(@LoginUser ChaosUser chaosUser, @RequestBody BaseRequest baseRequest) {
        return RestResponseUtil.initWithServiceResponse(overviewService.getUserScene(chaosUser, baseRequest));
    }

    @PostMapping("UserExpertiseOverview")
    @ApiOperation(value = "演练经验概要信息查询", notes = "概览页面的演练经验部分数据")
    public RestResponse<PageQueryResponse<OverviewExpertise>> userExpertiseOverview(@LoginUser ChaosUser chaosUser, @RequestBody OverviewRequest overviewRequest) {
        return RestResponseUtil.initWithServiceResponse(overviewService.getUserExpertise(chaosUser, overviewRequest));
    }
}
