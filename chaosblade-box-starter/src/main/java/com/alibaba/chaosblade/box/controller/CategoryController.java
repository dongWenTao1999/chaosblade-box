package com.alibaba.chaosblade.box.controller;

import com.alibaba.chaosblade.box.annotation.LoginUser;
import com.alibaba.chaosblade.box.common.common.constant.ChaosFunctionConstant;
import com.alibaba.chaosblade.box.common.common.domain.ChaosError;
import com.alibaba.chaosblade.box.common.common.domain.user.ChaosUser;
import com.alibaba.chaosblade.box.common.common.enums.CommonErrorCode;
import com.alibaba.chaosblade.box.common.infrastructure.domain.scene.SceneFunctionCategoryQueryRequest;
import com.alibaba.chaosblade.box.common.infrastructure.domain.scene.SceneFunctionCategoryUpdateRequest;
import com.alibaba.chaosblade.box.common.infrastructure.exception.ChaosException;
import com.alibaba.chaosblade.box.common.infrastructure.exception.PermissionDeniedException;
import com.alibaba.chaosblade.box.common.infrastructure.scene.CategoryFilterCondition;
import com.alibaba.chaosblade.box.common.infrastructure.util.CollectionUtil;
import com.alibaba.chaosblade.box.dao.model.SceneFunctionCategoryDO;
import com.alibaba.chaosblade.box.model.RestResponseUtil;
import com.alibaba.chaosblade.box.service.SceneFunctionCategoryService;
import com.alibaba.chaosblade.box.service.model.RestResponse;
import com.alibaba.chaosblade.box.service.model.scene.SceneFunctionCategoryVO;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Api(tags = "演练场景管理模块")
public class CategoryController extends BaseController {

    @Resource
    private SceneFunctionCategoryService sceneFunctionCategoryService;

    @PostMapping("AddCategory")
    @ApiOperation(value = "添加演练场景", notes = "项目中用不到")
    public RestResponse<SceneFunctionCategoryVO> addCategory(@LoginUser ChaosUser user, @RequestBody SceneFunctionCategoryUpdateRequest request) {
        if (null == user) {
            throw new PermissionDeniedException(CommonErrorCode.P_USER_PERMISSION_DENIED, "Admin Only.");
        }

        SceneFunctionCategoryDO category = sceneFunctionCategoryService.addCategory(request);
        if (null != category) {
            return RestResponseUtil.okWithData(SceneFunctionCategoryVO.from(category));
        }

        return RestResponseUtil.failed(ChaosError.withCode(CommonErrorCode.B_ERROR_CREATE_MINIAPP_CATEGORY));
    }

    @PostMapping("UpdateCategory")
    @ApiOperation(value = "修改演练场景", notes = "项目中用不到")
    public RestResponse<Void> updateCategory(@LoginUser ChaosUser user, @RequestBody SceneFunctionCategoryUpdateRequest request) {
        if (null == user) {
            throw new PermissionDeniedException(CommonErrorCode.P_USER_PERMISSION_DENIED, "Admin Only.");
        }

        String categoryId = request.getCategoryId();
        if (Strings.isNullOrEmpty(categoryId)) {
            throw new ChaosException(CommonErrorCode.P_ARGUMENT_ILLEGAL, "CategoryId cannot be null or empty.");
        }

        sceneFunctionCategoryService.updateCategory(request);

        return RestResponseUtil.ok();
    }

    @PostMapping("DeleteCategory")
    @ApiOperation(value = "删除演练场景", notes = "项目中用不到")
    public RestResponse<Void> deleteCategory(@LoginUser ChaosUser user, @RequestBody Map<String, String> params) {
        if (null == user) {
            throw new PermissionDeniedException(CommonErrorCode.P_USER_PERMISSION_DENIED, "Admin Only.");
        }

        String categoryId = params.get("categoryId");
        if (Strings.isNullOrEmpty(categoryId)) {
            throw new ChaosException(CommonErrorCode.P_ARGUMENT_ILLEGAL, "CategoryId cannot be null or empty.");
        }

        sceneFunctionCategoryService.deleteCategory(categoryId);

        return RestResponseUtil.ok();
    }

    @PostMapping("QuerySceneFunctionCategories")
    @ApiOperation(value = "查询演练场景目录", notes = "获取演练场景中的一级目录、二级目录、三级目录")
    public RestResponse<List<SceneFunctionCategoryVO>> getSceneFunctionCategories(@LoginUser ChaosUser user, @RequestBody SceneFunctionCategoryQueryRequest request) {
        Integer phase = request.getPhase();
        if (null == phase && (null == user)) {
            throw new PermissionDeniedException(CommonErrorCode.P_ARGUMENT_ILLEGAL, "Category phase cannot be null.");
        }
        CategoryFilterCondition condition = new CategoryFilterCondition();
        condition.setCloudServiceType(request.getCloudServiceType());
        condition.setFilterNoChild(request.getFilterNoChild());
        List<SceneFunctionCategoryDO> categories = sceneFunctionCategoryService.getCategoriesByPhase(phase, request.getScopeType(), request.getOsType(), request.getLang(), condition);

        if (CollectionUtil.isNullOrEmpty(categories)) {
            return RestResponseUtil.okWithData(Lists.newArrayList());
        }
        return RestResponseUtil.okWithData(
                categories.stream()
                        .map(SceneFunctionCategoryVO::from)
                        .collect(Collectors.toList())
        );
    }

    @ApiOperation(value = "查询监控监控策略目录")
    @PostMapping("QueryGlobalMonitorSceneFunctionCategories")
    public RestResponse<List<SceneFunctionCategoryVO>> getGlobalMonitorSceneFunctionCategories() {
        List<SceneFunctionCategoryDO> categories = sceneFunctionCategoryService.getCategoriesByPhaseAndType(
                ChaosFunctionConstant.PHASE_FLAG_GLOBAL,
                SceneFunctionCategoryDO.GLOBAL_MONITOR_CATEGORY_TYPE,
                null,
                null,
                null
        );

        if (CollectionUtil.isNullOrEmpty(categories)) {
            return RestResponseUtil.okWithData(Lists.newArrayList());
        }

        return RestResponseUtil.okWithData(
                categories.stream()
                        .map(SceneFunctionCategoryVO::from)
                        .collect(Collectors.toList())
        );
    }

    @ApiOperation(value = "查询恢复策略目录")
    @PostMapping("QueryGlobalGuardSceneFunctionCategories")
    public RestResponse<List<SceneFunctionCategoryVO>> getGlobalGuardSceneFunctionCategories() {
        List<SceneFunctionCategoryDO> categories = sceneFunctionCategoryService.getCategoriesByPhaseAndType(
                ChaosFunctionConstant.PHASE_FLAG_GLOBAL,
                SceneFunctionCategoryDO.GLOBAL_GUARD_ROOT_CATEGORY_TYPE,
                null,
                null,
                null
        );

        if (CollectionUtil.isNullOrEmpty(categories)) {
            return RestResponseUtil.okWithData(Lists.newArrayList());
        }

        return RestResponseUtil.okWithData(
                categories.stream()
                        .map(SceneFunctionCategoryVO::from)
                        .collect(Collectors.toList())
        );
    }

}
