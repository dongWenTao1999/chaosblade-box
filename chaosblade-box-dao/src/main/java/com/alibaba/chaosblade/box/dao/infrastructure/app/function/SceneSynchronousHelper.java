package com.alibaba.chaosblade.box.dao.infrastructure.app.function;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.alibaba.chaosblade.box.common.common.constant.ChaosFunctionConstant;
import com.alibaba.chaosblade.box.common.common.domain.chaosblade.ChaosBladeAction;
import com.alibaba.chaosblade.box.common.common.domain.chaosblade.ChaosBladeActionType;
import com.alibaba.chaosblade.box.common.common.domain.chaosblade.ChaosBladeMetaData;
import com.alibaba.chaosblade.box.common.common.util.MiniAppUtils;
import com.alibaba.chaosblade.box.common.infrastructure.util.CollectionUtil;
import com.alibaba.chaosblade.box.dao.infrastructure.utils.DebugUtils;
import com.alibaba.chaosblade.box.common.infrastructure.util.ResourceUtils;
import com.alibaba.chaosblade.box.dao.model.SceneFunctionDO;
import com.alibaba.chaosblade.box.common.infrastructure.domain.scene.SceneFunctionDependency;
import com.alibaba.chaosblade.box.dao.model.SceneFunctionParameterDO;
import com.alibaba.chaosblade.box.dao.repository.SceneFunctionParameterRepository;
import com.alibaba.chaosblade.box.dao.repository.SceneFunctionRepository;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: sunju
 *
 * Date:   2020/1/9
 */
@Slf4j
public abstract class SceneSynchronousHelper implements InitializingBean {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SceneFunctionRepository sceneFunctionRepository;

    @Autowired
    private SceneFunctionParameterRepository sceneFunctionParameterRepository;

    @Autowired
    private SceneFunctionParameterInterceptors sceneFunctionParameterInterceptors;

    /**
     * ?????????????????????
     */
    protected static Map<String, SceneFunctionDO> functionMap = Maps.newConcurrentMap();

    /**
     * ??????????????????????????????
     */
    protected static List<String> defaultOnlineFunctions = Collections.synchronizedList(new ArrayList<>());

    protected static Map<String, List<SceneFunctionParameterDO>> functionCodeToParams = Maps.newConcurrentMap();

    /**
     * ????????????????????????????????????????????????
     */
    protected static Map<String, LocalSceneFunctionParam> localCodeToParams = Maps.newConcurrentMap();

    /**
     * ?????????????????????
     */
    protected static Map<String, SceneFunctionParameterDO> globalOptionMap = Maps.newConcurrentMap();

    /**
     * @return
     */
    protected static Map<String, Hierarchy> hierarchyMap = Maps.newConcurrentMap();

    public static Set<String> getGlobalHiddenParams() {
        return globalHiddenParams;
    }

    /**
     * ??????????????????,????????????????????????????????????
     */
    protected static Set<String> globalHiddenParams = new HashSet<>();

    protected AtomicBoolean loadedSuccessFlag = new AtomicBoolean(false);

    /**
     * ???????????????????????????
     */
    private static String GlobalParamsPath = "classpath*:config/scene/params/*.json";

    private static String GlobalParamsPathProperties = "chaos.function-param-global-configs";

    private static String GlobalHiddenParamsPathProperties = "chaos.function-hidden-param-global-configs";

    /**
     * ???????????????????????????
     */
    private static String GlobalHiddenParamsPath = "classpath*:config/scene/params/hidden/*.json";

    /**
     * ????????????????????????????????????????????????????????????
     */
    private static String FunctionParamsPath = "config/scene/function_params.json";

    /**
     * ?????????????????????????????????????????????????????????code????????????????????????????????????
     */
    private static String LocalParamsPath = "classpath*:config/scene/local-param/*.json";

    private static String LocalParamsPathProperties = "chaos.function-param-loacl-configs";

    protected void markLoadSuccess() {
        loadedSuccessFlag.set(true);
    }

    public boolean isLoadSuccess() {
        return loadedSuccessFlag.get();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.loadDefaultSceneConfig();
        markLoadSuccess();
    }

    public List<String> getHierarchyChildren(String parentCode) {
        if (hierarchyMap.containsKey(parentCode)) {
            return hierarchyMap.get(parentCode).children;
        } else {
            return Collections.emptyList();
        }
    }

    public String getHierarchyDefaultCode(String parentCode) {
        if (hierarchyMap.containsKey(parentCode)) {
            return hierarchyMap.get(parentCode).defaultCode;
        } else {
            return null;
        }
    }

    /**
     * ???????????????????????????
     *
     * @throws Exception
     */
    private void loadDefaultSceneConfig() throws Exception {
        functionMap = loadSceneFunctionConfig();
        defaultOnlineFunctions = loadDefaultOnlineApps();
        globalOptionMap = loadGlobalOptionMap();
        globalHiddenParams = loadGlobalHiddenParams();
        functionCodeToParams = loadFunctionCodeToParams();
        localCodeToParams = loadLocalCodeToParams();
        hierarchyMap = loadHierarchy();
        log.info("load scene function config success");
    }

    protected Map<String, List<SceneFunctionParameterDO>> loadFunctionCodeToParams() {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
            FunctionParamsPath)) {
            assert inputStream != null;
            return JSON.parseObject(inputStream,
                new TypeReference<Map<String, List<SceneFunctionParameterDO>>>() {}.getType());
        } catch (IOException e) {
            log.error("load function params failed", e);
            return Maps.newConcurrentMap();
        }
    }

    protected Map<String, LocalSceneFunctionParam> loadLocalCodeToParams() {
        List<String> configs = Lists.newArrayList(LocalParamsPath);
        List<LocalSceneFunctionParam> localSceneFunctionParams = ResourceUtils.getResources(this.applicationContext,
            LocalSceneFunctionParam.class,
            LocalParamsPathProperties, configs);
        return localSceneFunctionParams.stream().collect(
            Collectors.toMap(LocalSceneFunctionParam::getName, e -> e, (a1, a2) -> a1));
    }

    protected Set<String> loadGlobalHiddenParams() {
        List<String> configs = Lists.newArrayList(GlobalHiddenParamsPath);
        List<HiddenParam> sceneFunctionParameterDOS = ResourceUtils.getResources(this.applicationContext,
            HiddenParam.class,
            GlobalHiddenParamsPathProperties, configs);
        return sceneFunctionParameterDOS.stream().flatMap(new Function<HiddenParam, Stream<String>>() {
            @Override
            public Stream<String> apply(HiddenParam hiddenParam) {
                return hiddenParam.getParams().stream();
            }
        }).collect(Collectors.toSet());
    }

    public static boolean isGlobalParams(String alias) {
        return globalOptionMap.containsKey(alias);
    }

    /**
     * ???????????????????????????
     *
     * @return
     */
    protected Map<String, SceneFunctionParameterDO> loadGlobalOptionMap() {
        List<String> configs = Lists.newArrayList(GlobalParamsPath);
        List<SceneFunctionParameterDO> sceneFunctionParameterDOS = ResourceUtils.getResources(this.applicationContext,
            SceneFunctionParameterDO.class,
            GlobalParamsPathProperties, configs);
        return sceneFunctionParameterDOS.stream().collect(
            Collectors.toMap(SceneFunctionParameterDO::getAlias, e -> e, (a1, a2) -> a1));
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    protected abstract Map<String, SceneFunctionDO> loadSceneFunctionConfig();

    protected abstract List<String> loadDefaultOnlineApps();

    @Data
    private static class HiddenParam {
        private List<String> params = new ArrayList<>();
    }

    /**
     * ???????????????????????????????????????
     *
     * @param newFunctionDefinition ???chaosblade?????????????????????????????????????????????????????????
     */
    public FunctionWrapper useDefaultConfig(SceneFunctionDO newFunctionDefinition) {
        SceneFunctionDO existSceneFunctionDO;
        DebugUtils.recordSceneFunctionSync("before override", newFunctionDefinition);
        existSceneFunctionDO = getExistSceneFunctionDO(newFunctionDefinition.getCode()).orElse(null);
        if (existSceneFunctionDO != null) {
            //????????????????????????????????????????????????,??????????????????,????????????????????????
            String[] ignoreCopyParameters = new String[] {"parameters", "dependencies", "phaseFlag", "categories",
                "categoryList", "supportScopeTypes", "supportScopeTypeList"};
            newFunctionDefinition.setFunctionId(existSceneFunctionDO.getFunctionId());
            List<SceneFunctionParameterDO> sceneFunctionParameterDOS = newFunctionDefinition.getParameters();
            List<SceneFunctionDependency> dep = newFunctionDefinition.getDependencyList();
            BeanUtils.copyProperties(existSceneFunctionDO, newFunctionDefinition, ignoreCopyParameters);
            newFunctionDefinition.setDependencyList(dep);
            if (CollectionUtil.isNullOrEmpty(newFunctionDefinition.getCategoryList())) {
                newFunctionDefinition.setCategoryList(existSceneFunctionDO.getCategoryList());
            }
            if (CollectionUtil.isNullOrEmpty(newFunctionDefinition.getSupportScopeTypeList())) {
                newFunctionDefinition.setSupportScopeTypeList(existSceneFunctionDO.getSupportScopeTypeList());
            }
            if (newFunctionDefinition.getPhaseFlag() == null) {
                newFunctionDefinition.setPhaseFlag(existSceneFunctionDO.getPhaseFlag());
            }
            newFunctionDefinition.setParameters(sceneFunctionParameterDOS);
            //????????????????????????
            mergeFunctionParams(newFunctionDefinition, existSceneFunctionDO);
            //????????????????????????????????????
            setDefaultOnlineFunctions(newFunctionDefinition);
        } else {
            //???????????????????????????
            initFunctionWithDefaultConfig(newFunctionDefinition);
            String code = newFunctionDefinition.getCode();
            hierarchyMap.entrySet().stream().filter(hierarchy ->
                    !hierarchy.getKey().equals(code) && hierarchy.getValue().children.contains(code)
            ).findFirst().map(hierarchy -> {
                newFunctionDefinition.setParentCode(hierarchy.getKey());
                return null;
            });
        }
        //??????????????????????????????
        setGlobalFunctionConfig(newFunctionDefinition);

        //??????????????????
        setGlobalFunctionParams(newFunctionDefinition, existSceneFunctionDO);
        DebugUtils.recordSceneFunctionSync("after override", newFunctionDefinition);
        if (newFunctionDefinition.isSupport(ChaosFunctionConstant.PHASE_FLAG_ATTACK)) {
            if (newFunctionDefinition.getDependencies().isEmpty()) {
                throw new IllegalArgumentException(
                    "scene function sync error,code:" + newFunctionDefinition.getCode() +
                        " is attack phase, should have dependencies(such as recovery or "
                        + "prepare)");
            }
        }
        FunctionWrapper functionWrapper = new FunctionWrapper(newFunctionDefinition, existSceneFunctionDO);
        sceneFunctionParameterInterceptors.beforeSync(functionWrapper);
        return functionWrapper;
    }

    private void setGlobalFunctionConfig(SceneFunctionDO functionDefinitionFromChaosBlade) {
        String code = functionDefinitionFromChaosBlade.getCode();
        if (!MiniAppUtils.isFromChaosBlade(code)) { return; }
        ChaosBladeAction attackChaosBladeAction = null;
        boolean recovery = false;
        ChaosBladeAction chaosBladeAction = ChaosBladeMetaData.getInstance().getChaosBladeAction(code);
        if (ChaosBladeActionType.STOP_ATTACK.equals(chaosBladeAction.getActionType())) {
            attackChaosBladeAction = ChaosBladeMetaData.getInstance().getChaosBladeAction(
                MiniAppUtils.getAttackCodeByRecoveryCode(code));
            recovery = true;
        }
        if (ChaosBladeActionType.ATTACK.equals(chaosBladeAction.getActionType())) {
            attackChaosBladeAction = chaosBladeAction;
        }
        if (recovery) {
            functionDefinitionFromChaosBlade.setEnabled(ChaosFunctionConstant.ENABLED_ONLINE);
        }
        if (attackChaosBladeAction == null) { return; }
        fillFunctionInfo(functionDefinitionFromChaosBlade, attackChaosBladeAction, recovery);
    }

    private void fillFunctionInfo(SceneFunctionDO sceneFunctionDO, ChaosBladeAction attack, boolean recovery) {
        String name = ChaosBladeMetaData.getInstance().getFunctionNameByChaosBladeAction(
            sceneFunctionDO.getName(), attack);
        if (recovery) {
            sceneFunctionDO.setName(MiniAppUtils.getRecoverName(name));
        } else {
            sceneFunctionDO.setName(name);
            if (attack.isJvm()) {
                sceneFunctionDO.addCategory(ChaosBladeMetaData.JvmActionToCategory.get(attack.getAction()));
                sceneFunctionDO.setEnabled(ChaosFunctionConstant.ENABLED_ONLINE);
            } else {
                sceneFunctionDO.addCategory(ChaosBladeMetaData.SubTargetToCategory.get(attack.getSubTarget()));
            }
        }
        if (ChaosBladeMetaData.getScopeToScopeType().containsKey(attack.getScope())) {
            sceneFunctionDO.appendSupportScopeType(
                ChaosBladeMetaData.getScopeToScopeType().get(attack.getScope()));
        }
        //??????????????????????????????????????????
        setSupportOsType(sceneFunctionDO);
    }

    private void mergeFunctionParams(SceneFunctionDO functionDefinitionFromChaosBlade,
        SceneFunctionDO existSceneFunctionDO) {
        if (CollectionUtil.isNullOrEmpty(existSceneFunctionDO.getParameters())) { return; }
        Map<String, List<SceneFunctionParameterDO>> existParamAliasToDef = existSceneFunctionDO.getParameters()
            .stream().collect(Collectors.groupingBy(SceneFunctionParameterDO::getAlias));
        List<SceneFunctionParameterDO> leftSceneFunctionParameterDOs = new ArrayList<>();
        for (SceneFunctionParameterDO sceneFunctionParameterDO : functionDefinitionFromChaosBlade.getParameters()) {
            if (existParamAliasToDef.containsKey(sceneFunctionParameterDO.getAlias())) {
                //?????????????????????????????????????????????
                leftSceneFunctionParameterDOs.add(existParamAliasToDef.get(sceneFunctionParameterDO.getAlias()).get(0));
            } else {
                //???????????????,???????????????
                sceneFunctionParameterDO.setVisible(false);
                leftSceneFunctionParameterDOs.add(sceneFunctionParameterDO);
            }
        }
        functionDefinitionFromChaosBlade.setParameters(leftSceneFunctionParameterDOs);
    }

    /**
     * ?????????????????????
     *
     * @param functionDefinitionFromChaosBlade
     * @param existSceneFunctionDO
     */
    protected void setGlobalFunctionParams(SceneFunctionDO functionDefinitionFromChaosBlade,
        SceneFunctionDO existSceneFunctionDO) {
        if (CollectionUtil.isNullOrEmpty(functionDefinitionFromChaosBlade.getParameters())) { return; }
        List<SceneFunctionParameterDO> sceneFunctionParameterDOS = new ArrayList<>();
        functionDefinitionFromChaosBlade.getParameters().forEach(parameterDO -> {
            SceneFunctionParameterDO defParameter = getGlobalFunctionParamConfig(functionDefinitionFromChaosBlade,
                parameterDO,
                globalOptionMap);
            if (defParameter != null) {
                parameterDO.setSequence(defParameter.getSequence());
                parameterDO.setName(defParameter.getName());
                parameterDO.setDescription(defParameter.getDescription());
                parameterDO.setComponent(defParameter.getComponent());
            } else {
                defParameter = handleWhenNoGlobalConfig(existSceneFunctionDO, parameterDO);
            }
            if (globalHiddenParams.contains(parameterDO.getAlias())) {
                defParameter.setIsDelete(true);
            }
            sceneFunctionParameterDOS.add(defParameter);
        });
        functionDefinitionFromChaosBlade.setParameters(sceneFunctionParameterDOS);
    }

    private SceneFunctionParameterDO handleWhenNoGlobalConfig(SceneFunctionDO existSceneFunctionDO,
        SceneFunctionParameterDO parameterDO) {
        SceneFunctionParameterDO defParameter;
        if (existSceneFunctionDO != null && existSceneFunctionDO.getParameters() != null) {
            Map<String, SceneFunctionParameterDO> parameterMaps = existSceneFunctionDO.getParameters()
                .stream()
                .collect(Collectors.toMap(
                    SceneFunctionParameterDO::getAlias,
                    Function.identity(),
                    (k1, k2) -> k2
                ));
            defParameter = parameterMaps.getOrDefault(parameterDO.getAlias(), parameterDO);
        } else {
            defParameter = parameterDO;
        }
        return defParameter;
    }

    protected SceneFunctionParameterDO getGlobalFunctionParamConfig(
        SceneFunctionDO functionDefinitionFromChaosBlade,
        SceneFunctionParameterDO parameter,
        Map<String, SceneFunctionParameterDO> globalOptionMap) {
        SceneFunctionParameterDO sceneFunctionParameterDO = null;
        if (functionCodeToParams.containsKey(functionDefinitionFromChaosBlade.getCode())) {
            sceneFunctionParameterDO = functionCodeToParams.get(functionDefinitionFromChaosBlade.getCode()).stream()
                .filter(
                    parameterDO -> parameterDO.getAlias().equals(parameter.getAlias())).findAny().orElse(null);
        }
        if (sceneFunctionParameterDO == null && localCodeToParams.containsKey(parameter.getAlias())) {
            LocalSceneFunctionParam local = localCodeToParams.get(parameter.getAlias());
            sceneFunctionParameterDO = parseLocalParam(local, functionDefinitionFromChaosBlade.getCode());
        }
        if (sceneFunctionParameterDO != null) {
            sceneFunctionParameterDO.setParameterId(null);
            sceneFunctionParameterDO.setFunctionId(null);
            return sceneFunctionParameterDO;
        }

        if (!globalOptionMap.isEmpty()) {
            sceneFunctionParameterDO = globalOptionMap.get(parameter.getAlias());
            if (sceneFunctionParameterDO != null) {
                sceneFunctionParameterDO.setParameterId(null);
                sceneFunctionParameterDO.setFunctionId(null);
                return sceneFunctionParameterDO;
            }
        }
        return null;
    }

    protected SceneFunctionParameterDO parseLocalParam(LocalSceneFunctionParam localSceneFunctionParam, String code) {
        //????????????
        if (!CollectionUtil.isNullOrEmpty(localSceneFunctionParam.getExcludeScene())) {
            if (localSceneFunctionParam.getExcludeScene().stream().anyMatch(code::contains)) {
                return null;
            }
        }
        //????????????
        if (!CollectionUtil.isNullOrEmpty(localSceneFunctionParam.getExcludeCode())) {
            if (localSceneFunctionParam.getExcludeCode().contains(code)) {
                return null;
            }
        }
        //?????????
        if (!CollectionUtil.isNullOrEmpty(localSceneFunctionParam.getIncludeScene())) {
            if (localSceneFunctionParam.getIncludeScene().stream().anyMatch(code::contains)) {
                return localSceneFunctionParam.getParam();
            }
        }
        //????????????
        if (!CollectionUtil.isNullOrEmpty(localSceneFunctionParam.getIncludeCode())) {
            if (localSceneFunctionParam.getIncludeCode().contains(code)) {
                return localSceneFunctionParam.getParam();
            }
        }
        return null;
    }

    /**
     * ??????????????????????????????
     *
     * @param functionCode
     * @return
     */
    protected Optional<SceneFunctionDO> getExistSceneFunctionDO(String functionCode) {
        Optional<SceneFunctionDO> functionDO = sceneFunctionRepository.findByCode(functionCode);
        functionDO.ifPresent(sceneFunctionDO -> sceneFunctionDO.setParameters(
            sceneFunctionParameterRepository.findByFunctionId(sceneFunctionDO.getFunctionId())));
        return functionDO;
    }

    /**
     * ???????????????????????????,??????????????????????????????????????????????????????,
     * ???????????????????????????????????????
     *
     * ????????????????????????????????????????????????????????????
     *
     * @param functionDefinitionFromChaosBlade
     */
    private void initFunctionWithDefaultConfig(SceneFunctionDO functionDefinitionFromChaosBlade) {
        SceneFunctionDO defaultFunctionMap = getDefaultSceneFunction(functionDefinitionFromChaosBlade);
        if (defaultFunctionMap == null) {
            List<String> defaultOnlineAppCodes = getDefaultOnlineApp();
            if(defaultOnlineAppCodes.contains(functionDefinitionFromChaosBlade.getCode())) {
                functionDefinitionFromChaosBlade.setEnabled(ChaosFunctionConstant.ENABLED_ONLINE);
                return;
            }
            functionDefinitionFromChaosBlade.setEnabled(ChaosFunctionConstant.ENABLED_READY);
            return;
        }
        useDefaultSceneFunction(functionDefinitionFromChaosBlade, defaultFunctionMap);
    }

    private void useDefaultSceneFunction(SceneFunctionDO functionDefinitionFromChaosBlade,
        SceneFunctionDO defaultFunctionMap) {
        if (!Strings.isNullOrEmpty(defaultFunctionMap.getName())) {
            functionDefinitionFromChaosBlade.setName(defaultFunctionMap.getName());
        }
        if (!Strings.isNullOrEmpty(defaultFunctionMap.getDescription())) {
            functionDefinitionFromChaosBlade.setDescription(defaultFunctionMap.getDescription());
        }
        functionDefinitionFromChaosBlade.setPhaseFlag(defaultFunctionMap.getPhaseFlag());
        functionDefinitionFromChaosBlade.setEnabled(defaultFunctionMap.getEnabled());
        functionDefinitionFromChaosBlade.setCategories(defaultFunctionMap.getCategories());
        functionDefinitionFromChaosBlade.setSupportScopeTypes(defaultFunctionMap.getSupportScopeTypes());
    }

    protected SceneFunctionDO getDefaultSceneFunction(SceneFunctionDO function) {
        if (!functionMap.isEmpty()) {
            return functionMap.get(function.getCode());
        }
        return null;
    }

    protected Map<String, Hierarchy> loadHierarchy() {
        InputStream stream = ResourceUtil.getStream("config/scene/hierarchy/hierarchy.json");
        JSONObject jsonObject = JSON.parseObject(IoUtil.read(stream, Charset.defaultCharset()));

        Map<String, Hierarchy> map = new HashMap<>();

        jsonObject.entrySet().forEach(o -> {
            JSONObject value = (JSONObject) o.getValue();
            Hierarchy hierarchy = new Hierarchy(
                    value.getString("defaultCode"),
                    value.getJSONArray("children").stream().map(v -> v.toString()).collect(Collectors.toList())
            );
            map.put(o.getKey(), hierarchy);
        });
        return map;
    }

    public static class FunctionWrapper {

        public SceneFunctionDO getExistSceneFunction() {
            return existSceneFunction;
        }

        private SceneFunctionDO existSceneFunction;

        public SceneFunctionDO getUpdateSceneFunction() {
            return updateSceneFunction;
        }

        private SceneFunctionDO updateSceneFunction;

        public FunctionWrapper(SceneFunctionDO updateSceneFunction, SceneFunctionDO existSceneFunction) {
            this.existSceneFunction = existSceneFunction;
            this.updateSceneFunction = updateSceneFunction;
        }
    }

    private void setSupportOsType(SceneFunctionDO sceneFunctionDO) {
        String appCode = sceneFunctionDO.getCode();
        if(MiniAppUtils.isWin(appCode)) {
            sceneFunctionDO.appendSupportOsTypes(ChaosFunctionConstant.DEVICE_OS_TYPE_WINDOWS);
        } else {
            sceneFunctionDO.appendSupportOsTypes(ChaosFunctionConstant.DEVICE_OS_TYPE_LINUX);
        }

    }

    private List<String> getDefaultOnlineApp() {
        return defaultOnlineFunctions;
    }
    
    private static class Hierarchy {

        private String defaultCode;

        private List<String> children;

        public Hierarchy(String defaultCode, List<String> children) {
            this.defaultCode = defaultCode;
            this.children = children;
        }
    }

    /**
     * ?????????????????????chaos?????????
     * @param newFunctionDefinition
     */
    private void setDefaultOnlineFunctions(SceneFunctionDO newFunctionDefinition) {
        List<String> defaultOnlineAppCodes = getDefaultOnlineApp();
        if(defaultOnlineAppCodes.contains(newFunctionDefinition.getCode())) {
            newFunctionDefinition.setEnabled(ChaosFunctionConstant.ENABLED_ONLINE);
        }
    }

}
