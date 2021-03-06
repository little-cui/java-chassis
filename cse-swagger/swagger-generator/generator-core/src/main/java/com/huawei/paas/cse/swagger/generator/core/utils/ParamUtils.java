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

package com.huawei.paas.cse.swagger.generator.core.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;

import com.fasterxml.jackson.databind.ser.std.MapProperty;
import com.huawei.paas.cse.swagger.extend.parameter.PendingBodyParameter;
import com.huawei.paas.cse.swagger.generator.core.OperationGenerator;

import io.swagger.converter.ModelConverters;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.PropertyBuilder;
import io.swagger.models.properties.RefProperty;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author
 * @version  [版本号, 2017年3月17日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public final class ParamUtils {
    private static DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private ParamUtils() {

    }

    // 如果existName为empty，则通过原型查找
    public static String getParameterName(String existName, Method method, int paramIdx) {
        if (StringUtils.isEmpty(existName)) {
            existName = getParameterName(method, paramIdx);
        }

        return existName;
    }

    public static String getParameterName(Method method, int paramIdx) {
        MethodParameter methodParameter = new MethodParameter(method, paramIdx);
        methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);

        String paramName = methodParameter.getParameterName();
        if (paramName == null) {
            // 小于jdk8的场景中，即使有debug参数，也无法对着interface获取参数名，此时直接使用arg + paramIndex来表示
            paramName = "arg" + paramIdx;
        }
        return paramName;
    }

    public static Type getGenericParameterType(Method method, int paramIdx) {
        return method.getGenericParameterTypes()[paramIdx];
    }

    public static PendingBodyParameter createPendingBodyParameter(OperationGenerator operationGenerator,
            int paramIdx) {
        Method method = operationGenerator.getProviderMethod();
        String paramName = getParameterName(method, paramIdx);
        Type paramType = getGenericParameterType(method, paramIdx);
        return createPendingBodyParameter(operationGenerator, paramName, paramType);
    }

    public static PendingBodyParameter createPendingBodyParameter(OperationGenerator operationGenerator,
            String paramName, Type paramType) {
        addDefinitions(operationGenerator.getSwagger(), paramType);
        Property property = ModelConverters.getInstance().readAsProperty(paramType);

        PendingBodyParameter pendingBodyParameter = new PendingBodyParameter();
        pendingBodyParameter.setName(paramName);
        pendingBodyParameter.setProperty(property);
        pendingBodyParameter.setType(paramType);
        pendingBodyParameter.setOperationGenerator(operationGenerator);

        return pendingBodyParameter;
    }

    public static String generateBodyParameterName(Method method) {
        return method.getName() + "Body";
    }

    public static BodyParameter createBodyParameter(Swagger swagger, String paramName, Type paramType) {
        addDefinitions(swagger, paramType);

        Property property = ModelConverters.getInstance().readAsProperty(paramType);
        Model model = PropertyBuilder.toModel(property);

        BodyParameter bodyParameter = new BodyParameter();
        bodyParameter.setName(paramName);
        bodyParameter.setSchema(model);

        return bodyParameter;
    }

    public static void addDefinitions(Swagger swagger, Type paramType) {
        Map<String, Model> models = ModelConverters.getInstance().readAll(paramType);
        for (Map.Entry<String, Model> entry : models.entrySet()) {
            swagger.addDefinition(entry.getKey(), entry.getValue());
        }
    }

    public static void setParameterType(Swagger swagger, Method method, int paramIdx,
            AbstractSerializableParameter<?> parameter) {
        Type paramType = ParamUtils.getGenericParameterType(method, paramIdx);

        ParamUtils.addDefinitions(swagger, paramType);

        Property property = ModelConverters.getInstance().readAsProperty(paramType);

        if (isComplexProperty(property)) {
            // 简单参数不可以是复杂类型
            String msg = String.format("not allow complex type for %s parameter, method=%s:%s, paramIdx=%d, type=%s",
                    parameter.getIn(),
                    method.getDeclaringClass().getName(),
                    method.getName(),
                    paramIdx,
                    paramType.getTypeName());
            throw new Error(msg);
        }
        parameter.setProperty(property);
    }

    public static boolean isComplexProperty(Property property) {
        if (RefProperty.class.isInstance(property) || ObjectProperty.class.isInstance(property)
                || MapProperty.class.isInstance(property)) {
            return true;
        }

        if (ArrayProperty.class.isInstance(property)) {
            return isComplexProperty(((ArrayProperty) property).getItems());
        }

        return false;
    }

    public static int findParameterByName(String name, List<Parameter> parameterList) {
        for (int idx = 0; idx < parameterList.size(); idx++) {
            Parameter parameter = parameterList.get(idx);
            if (name.equals(parameter.getName())) {
                return idx;
            }
        }

        return -1;
    }

    public static boolean isRealBodyParameter(Parameter parameter) {
        return BodyParameter.class.getName().equals(parameter.getClass().getName());
    }
}
