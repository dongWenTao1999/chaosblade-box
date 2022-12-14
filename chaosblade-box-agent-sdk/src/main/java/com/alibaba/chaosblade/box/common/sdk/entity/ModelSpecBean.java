/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.chaosblade.box.common.sdk.entity;

import java.util.List;

/**
 * @author Changjun Xiao
 */
public class ModelSpecBean {
    private String target;
    private String shortDesc;
    private String longDesc;
    private String example;
    private List<ActionSpecBean> actions;
    private PrepareSpecBean prepare;
    private String scope;
    private List<String> subTargets;

    public ModelSpecBean() {
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public List<ActionSpecBean> getActions() {
        return actions;
    }

    public void setActions(List<ActionSpecBean> actions) {
        this.actions = actions;
    }

    public PrepareSpecBean getPrepare() {
        return prepare;
    }

    public void setPrepare(PrepareSpecBean prepare) {
        this.prepare = prepare;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getSubTargets() {
        return subTargets;
    }

    public void setSubTargets(List<String> subTargets) {
        this.subTargets = subTargets;
    }
}
