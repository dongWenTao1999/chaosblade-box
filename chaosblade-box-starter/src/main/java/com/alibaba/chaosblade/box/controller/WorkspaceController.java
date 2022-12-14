package com.alibaba.chaosblade.box.controller;

import com.alibaba.chaosblade.box.annotation.LoginUser;
import com.alibaba.chaosblade.box.annotation.SwaggerDoc;
import com.alibaba.chaosblade.box.common.common.domain.Response;
import com.alibaba.chaosblade.box.common.common.domain.user.ChaosUser;
import com.alibaba.chaosblade.box.common.experiment.clientobject.ExperimentStat;
import com.alibaba.chaosblade.box.common.infrastructure.constant.PermissionTypes;
import com.alibaba.chaosblade.box.common.infrastructure.domain.experiment.request.ExperimentCloneRequest;
import com.alibaba.chaosblade.box.common.infrastructure.domain.workspace.WorkspaceExperiment;
import com.alibaba.chaosblade.box.common.infrastructure.domain.workspace.WorkspaceExperimentPageableQueryRequest;
import com.alibaba.chaosblade.box.common.infrastructure.domain.workspace.WorkspaceExperimentRequest;
import com.alibaba.chaosblade.box.common.infrastructure.domain.workspace.WorkspaceQueryRequest;
import com.alibaba.chaosblade.box.common.infrastructure.exception.ChaosException;
import com.alibaba.chaosblade.box.dao.infrastructure.domain.workspace.WorkspaceBaseInfo;
import com.alibaba.chaosblade.box.dao.infrastructure.domain.workspace.WorkspaceExperimentPageableQueryResponse;
import com.alibaba.chaosblade.box.dao.infrastructure.experiment.request.ExperimentCreateRequest;
import com.alibaba.chaosblade.box.model.RestResponseUtil;
import com.alibaba.chaosblade.box.service.ExperimentWriteService;
import com.alibaba.chaosblade.box.service.WorkspaceService;
import com.alibaba.chaosblade.box.service.auth.perimission.UserPermissionService;
import com.alibaba.chaosblade.box.service.model.RestResponse;
import com.google.common.base.Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;


/**
 * @Author yinyansheng
 * @create 2020/10/15
 */
@RestController
@SwaggerDoc
@Slf4j
@Api(tags = "????????????????????????")
public class WorkspaceController extends BaseController {

    @Resource
    private WorkspaceService workspaceService;

    @Resource
    private UserPermissionService userPermissionService;

    @Autowired
    private ExperimentWriteService experimentWriteService;

    @ApiOperation(value = "???????????????????????????????????????")
    @PostMapping(value = "/GetGeneralWorkSpaceStatInfo")
    public RestResponse<ExperimentStat> getGeneralWorkSpaceStatInfo(
            @LoginUser ChaosUser user, @RequestBody WorkspaceQueryRequest request) {
        Preconditions.checkArgument(request.getWorkspaceId() != null, "workspace id is required");

        return RestResponseUtil.initWithServiceResponse(
            workspaceService.getGeneralWorkSpaceStatInfo(user, request)
        );
    }

    @ApiOperation(value = "????????????????????????????????????")
    @PostMapping(value = "/PageableGeneralExperiments")
    public RestResponse<WorkspaceExperimentPageableQueryResponse> PageableGeneralExperiments(
            @LoginUser ChaosUser user, @RequestBody WorkspaceExperimentPageableQueryRequest request) {
        Preconditions.checkArgument(request.getWorkspaceId() != null, "workspace id is required");
        return RestResponseUtil.okWithData(
            workspaceService.pageableGeneralExperiments(user, request, PermissionTypes.ALL)
        );
    }

    @PostMapping("/ListGeneralWorkspaceExperimentTags")
    @ApiOperation(value = "?????????????????????????????????????????????")
    public RestResponse<List<String>> listExperimentTags(@LoginUser ChaosUser user,
        @RequestBody WorkspaceQueryRequest request) {
        return RestResponseUtil.okWithData(
            workspaceService.listExperimentTagsInWorkspace(request.getWorkspaceId(), request.getNamespace()));
    }

    @ApiOperation(value = "????????????????????????????????????")
    @PostMapping(value = "/workspaceCreateExperiment")
    public RestResponse<String> workspaceCreateExperiment(@LoginUser ChaosUser chaosUser,
        @RequestBody ExperimentCreateRequest experimentCreateRequest) throws ChaosException {
        userPermissionService.checkCreateExperimentPermission(chaosUser, experimentCreateRequest);
        Response<String> experiment = experimentWriteService.createExperiment(experimentCreateRequest);
        return RestResponseUtil.initWithServiceResponse(
            experiment
        );
    }

    @ApiOperation(value = "????????????ID????????????????????????")
    @PostMapping(value = "/GetWorkspaceByExperimentId")
    public RestResponse<List<WorkspaceBaseInfo>> getWorkspaceByExperimentId(@LoginUser ChaosUser chaosUser,
                                                                            @RequestBody WorkspaceQueryRequest request) {
        Preconditions.checkArgument(request.getExperimentId() != null, "experiment id is required");
        return RestResponseUtil.okWithData(
                workspaceService.getWorkspaceByExperimentId(chaosUser, request)
        );
    }

    @ApiOperation(value = "????????????????????????????????????")
    @PostMapping(value = "workspaceCloneExperiment")
    public RestResponse<String> cloneExperiment(@LoginUser ChaosUser chaosUser,
        @RequestBody ExperimentCloneRequest experimentCloneRequest) {
        userPermissionService.checkExperimentPermission(PermissionTypes.W, chaosUser,
            experimentCloneRequest.getExperimentId(), experimentCloneRequest.getNamespace(), null);

        Response<String> response = experimentWriteService.cloneExperiment(experimentCloneRequest);

        if (response.getError() == null) {
            WorkspaceExperiment workspaceExperiment = new WorkspaceExperiment();
            workspaceExperiment.setExperimentId(response.getResult());
            workspaceExperiment.setExperimentName(experimentCloneRequest.getName());

            WorkspaceExperimentRequest workspaceExperimentRequest = WorkspaceExperimentRequest
                .builder()
                .workspaceId(experimentCloneRequest.getWorkspaceId())
                .workspaceExperimentList(Collections.singletonList(workspaceExperiment))
                .build();

            workspaceExperimentRequest.setNamespace(experimentCloneRequest.getNamespace());
            workspaceService.addWorkspaceExperiment(chaosUser, workspaceExperimentRequest);
        }

        return RestResponseUtil.initWithServiceResponse(
            response
        );
    }

}
