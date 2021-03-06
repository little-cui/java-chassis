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

import com.huawei.paas.foundation.common.RegisterManager;

/**
 * 管理所有parameter processor creater
 * @author   
 * @version  [版本号, 2016年12月20日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public final class ParamValueProcessorCreatorManager extends RegisterManager<String, ParamValueProcessorCreator> {
    private static final String NAME = "param value processor mgr";

    public static final ParamValueProcessorCreatorManager INSTANCE = new ParamValueProcessorCreatorManager();

    static {
        new PathProcessorCreator();
        new QueryProcessorCreator();
        new FormProcessorCreator();
        new HeaderProcessorCreator();
        new CookieProcessorCreator();
        new BodyProcessorCreator();
    }

    private ParamValueProcessorCreatorManager() {
        super(NAME);
    }

    /**
     * 直接获取body parameter processor
     * @return
     */
    public ParamValueProcessorCreator getBodyProcessorCreater() {
        return this.ensureFindValue(BodyProcessorCreator.PARAMTYPE);
    }

}
