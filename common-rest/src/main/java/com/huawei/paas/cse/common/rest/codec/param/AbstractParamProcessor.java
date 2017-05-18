/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.cse.common.rest.codec.param;

import com.fasterxml.jackson.databind.JavaType;

/**
 * @author   
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class AbstractParamProcessor implements ParamValueProcessor {
    protected String paramPath;

    protected JavaType targetType;

    /**
     * <构造函数>
     * @param paramPath
     * @param targetType [参数说明]
     */
    public AbstractParamProcessor(String paramPath, JavaType targetType) {
        this.paramPath = paramPath;
        this.targetType = targetType;
    }

    /**
     * {@inheritDoc}
     */
    public String getParameterPath() {
        return paramPath;
    }

}