package com.alibaba.chaosblade.box.controller;

import com.alibaba.chaosblade.box.annotation.LoginUser;
import com.alibaba.chaosblade.box.common.common.domain.PageQueryResponse;
import com.alibaba.chaosblade.box.common.common.domain.experiment.BaseExperimentRequest;
import com.alibaba.chaosblade.box.common.common.domain.user.ChaosUser;
import com.alibaba.chaosblade.box.common.infrastructure.domain.experiment.request.ExperimentExpertiseQueryRequest;
import com.alibaba.chaosblade.box.common.infrastructure.domain.experiment.request.ExperimentFlowInitByExpertiseRequest;
import com.alibaba.chaosblade.box.common.infrastructure.domain.experiment.response.ExperimentFlowInfo;
import com.alibaba.chaosblade.box.common.infrastructure.domain.experiment.response.ExperimentInfo;
import com.alibaba.chaosblade.box.model.RestResponseUtil;
import com.alibaba.chaosblade.box.service.ExpertiseService;
import com.alibaba.chaosblade.box.service.model.RestResponse;
import com.alibaba.chaosblade.box.service.model.expertise.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author haibin
 */
@RestController
@Api(tags = "演练经验管理模块")
public class ExpertiseController extends BaseController {

    @Autowired
    ExpertiseService expertiseService;

    /**
     * 根据演练模板初始化演练流程
     *
     * @param loginUser                            登录用户
     * @param experimentFlowInitByExpertiseRequest
     * @return
     */
    @PostMapping(value = "InitFlowByExpertise")
    @ApiOperation(value = "根据演练模板初始化演练流程")
    public RestResponse<ExperimentFlowInfo> initFlowByTemplate(@LoginUser ChaosUser loginUser,
                                                               @RequestBody ExperimentFlowInitByExpertiseRequest experimentFlowInitByExpertiseRequest) {
        experimentFlowInitByExpertiseRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(
                expertiseService.initFlowByExpertise(experimentFlowInitByExpertiseRequest));
    }

    /**
     * 根据演练模板初始化整个演练
     *
     * @param loginUser
     * @param experimentFlowInitByExpertiseRequest
     * @return
     */
    @PostMapping(value = "InitExperimentByExpertise")
    @ApiOperation(value = "根据演练模板初始化整个演练")
    public RestResponse<ExperimentInfo> initExperimentByExpertise(@LoginUser ChaosUser loginUser,
                                                                  @RequestBody ExperimentFlowInitByExpertiseRequest experimentFlowInitByExpertiseRequest) {
        experimentFlowInitByExpertiseRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(
                expertiseService.initExperimentByExpertise(experimentFlowInitByExpertiseRequest));
    }

    /**
     * 查询演练经验概览
     *
     * @param loginUser
     * @return
     */
    @PostMapping(value = "SearchExpertise")
    @ApiOperation(value = "查询演练经验")
    public RestResponse<PageQueryResponse<ExpertiseView>> queryExpertiseViews(@LoginUser ChaosUser loginUser,
                                                                              @RequestBody ExpertiseSearchRequest expertiseSearchRequest) {
        expertiseSearchRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(expertiseService.searchExpertise(expertiseSearchRequest));
    }

    /**
     * 查询演练经验详情
     *
     * @param loginUser
     * @param experimentExpertiseQueryRequest
     * @return
     */
    @PostMapping(value = "QueryExpertiseDetails")
    @ApiOperation(value = "查询演练经验详情")
    public RestResponse<ExpertiseInfo> queryExpertiseDetails(@LoginUser ChaosUser loginUser,
                                                             @RequestBody ExperimentExpertiseQueryRequest experimentExpertiseQueryRequest) {
        experimentExpertiseQueryRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(
                expertiseService.queryExpertiseDetails(experimentExpertiseQueryRequest));
    }

    /**
     * 禁用演练经验
     *
     * @param loginUser
     * @param experimentExpertiseQueryRequest
     * @return
     */
    @PostMapping(value = "DisableExpertise")
    @ApiOperation(value = "禁用演练经验")
    public RestResponse disableExpertise(@LoginUser ChaosUser loginUser,
                                         @RequestBody ExperimentExpertiseQueryRequest experimentExpertiseQueryRequest) {
        experimentExpertiseQueryRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(
                expertiseService.disableExpertise(experimentExpertiseQueryRequest));
    }

    /**
     * 创建经验
     *
     * @param loginUser
     * @param experimentExpertiseCreateRequest
     * @return
     */
    @PostMapping(value = "CreateExpertise")
    @ApiOperation(value = "创建经验")
    public RestResponse<String> createExpertise(@LoginUser ChaosUser loginUser,
                                                @RequestBody ExperimentExpertiseCreateRequest experimentExpertiseCreateRequest) {
        experimentExpertiseCreateRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(
                expertiseService.createExpertise(experimentExpertiseCreateRequest));
    }

    /**
     * 更新模板
     *
     * @param loginUser
     * @param experimentExpertiseUpdateRequest
     * @return
     */
    @PostMapping(value = "UpdateExpertise")
    @ApiOperation(value = "更新模板")
    public RestResponse<String> updateExpertise(@LoginUser ChaosUser loginUser,
                                                @RequestBody ExperimentExpertiseUpdateRequest experimentExpertiseUpdateRequest) {
        experimentExpertiseUpdateRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(
                expertiseService.updateExpertise(experimentExpertiseUpdateRequest));
    }

    /**
     * 转换演练到经验库
     *
     * @param loginUser
     * @param experimentRequest
     * @return
     */
    @PostMapping(value = "ConvertExperimentToExpertise")
    @ApiOperation(value = "转换演练到经验库")
    public RestResponse<String> convertExperimentToExpertise(@LoginUser ChaosUser loginUser,
                                                             @RequestBody BaseExperimentRequest experimentRequest) {
        experimentRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(
                expertiseService.convertExperimentToExpertise(experimentRequest));
    }

    /**
     * 删除经验
     *
     * @param loginUser
     * @param experimentExpertiseQueryRequest
     * @return
     */
    @PostMapping(value = "DeleteExpertise")
    @ApiOperation(value = "删除经验")
    public RestResponse deleteExpertise(@LoginUser ChaosUser loginUser,
                                        @RequestBody ExperimentExpertiseQueryRequest experimentExpertiseQueryRequest) {
        experimentExpertiseQueryRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(
                expertiseService.deleteExpertise(experimentExpertiseQueryRequest));
    }

    /**
     * 启用经验
     *
     * @param loginUser
     * @param experimentExpertiseQueryRequest
     * @return
     */
    @PostMapping(value = "EnableExpertise")
    @ApiOperation(value = "启用经验")
    public RestResponse enableExpertise(@LoginUser ChaosUser loginUser,
                                        @RequestBody ExperimentExpertiseQueryRequest experimentExpertiseQueryRequest) {
        experimentExpertiseQueryRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(
                expertiseService.enableExpertise(experimentExpertiseQueryRequest));
    }

    @PostMapping(value = "CloneExpertise")
    @ApiOperation(value = "克隆经验")
    public RestResponse<ExpertiseInfo> cloneExpertise(@LoginUser ChaosUser loginUser,
                                                      @RequestBody ExpertiseCloneRequest expertiseCloneRequest) {
        expertiseCloneRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(
                expertiseService.cloneExpertise(expertiseCloneRequest));
    }

    @PostMapping(value = "PageableQueryExpertise")
    @ApiOperation(value = "分页查询经验")
    public RestResponse<PageQueryResponse<AdminExpertiseView>> pageableQueryExpertise(@LoginUser ChaosUser loginUser,
                                                                                      @RequestBody ExpertiseSearchRequest expertiseCloneRequest) {
        expertiseCloneRequest.setUser(loginUser);
        return RestResponseUtil.initWithServiceResponse(
                expertiseService.listExpertise(expertiseCloneRequest));
    }
}

