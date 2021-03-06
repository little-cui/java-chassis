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

package com.huawei.paas.cse.common.rest.definition;

import java.io.Serializable;
import java.util.Comparator;

import com.huawei.paas.cse.common.rest.definition.path.PathRegExp;

/**
 * 用于RestOperation的排序
 * @author   
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class RestOperationComparator implements Serializable, Comparator<RestOperationMeta> {

    /**
     * findbugs提示需要加上该UID
     */
    private static final long serialVersionUID = -2364909265520813678L;

    @Override
    public int compare(RestOperationMeta r1, RestOperationMeta r2) {
        // 排序规则:
        // 1.静态字符多的优先
        // 2.变量组多的优先
        // 3.带正则表达式的优先
        // 如:
        // /customers/{id}/{name}/address
        // /customers/{id : .+}/address
        // /customers/{id}/address
        // /customers/{id : .+}

        PathRegExp path1 = r1.getAbsolutePathRegExp();
        PathRegExp path2 = r2.getAbsolutePathRegExp();

        int staticCompare = path2.getStaticCharCount() - path1.getStaticCharCount();
        if (staticCompare != 0) {
            return staticCompare;
        }

        int groupCompare = path2.getGroupCount() - path1.getGroupCount();
        if (groupCompare != 0) {
            return groupCompare;
        }

        return path2.getGroupWithRegExpCount() - path1.getGroupWithRegExpCount();
    }

}
