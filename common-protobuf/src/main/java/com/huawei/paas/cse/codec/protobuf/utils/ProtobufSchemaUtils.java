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

package com.huawei.paas.cse.codec.protobuf.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.huawei.paas.cse.codec.protobuf.utils.schema.WrapSchemaFactory;
import com.huawei.paas.cse.common.javassist.JavassistUtils;
import com.huawei.paas.cse.core.definition.OperationMeta;

import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年2月18日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public final class ProtobufSchemaUtils {
    private static volatile Map<String, WrapSchema> schemaCache = new ConcurrentHashMap<>();

    private interface SchemaCreator {
        WrapSchema create() throws Exception;
    }

    private ProtobufSchemaUtils() {
    }

    private static WrapSchema getOrCreateSchema(String className, SchemaCreator creator) {
        WrapSchema schema = schemaCache.get(className);
        if (schema != null) {
            return schema;
        }

        synchronized (ProtobufSchemaUtils.class) {
            schema = schemaCache.get(className);
            if (schema != null) {
                return schema;
            }

            try {
                schema = creator.create();
            } catch (Exception e) {
                throw new Error(e);
            }
            schemaCache.put(className, schema);
            return schema;
        }
    }

    private static boolean isArgsNeedWrap(Method method) {
        if (method.getParameterCount() != 1) {
            return true;
        }

        // 单参数时，需要根据实际情况判断
        return isNeedWrap(method.getParameterTypes()[0]);
    }

    private static boolean isNeedWrap(Class<?> cls) {
        // protobuf不支持原子类型、enum、string、数组、collection等等作为msg，只有Object类型才可以
        return ClassUtils.isPrimitiveOrWrapper(cls) || cls.isArray() || cls.isEnum()
                || String.class.isAssignableFrom(cls)
                || Collection.class.isAssignableFrom(cls)
                || Map.class.isAssignableFrom(cls)
                || Date.class.isAssignableFrom(cls);
    }

    // 为了支持method args的场景，全部实现ProtobufMessageWrapper接口，有的场景有点浪费，不过无关紧要
    private static WrapSchema createWrapSchema(WrapClassConfig config) throws Exception {
        Class<?> cls = JavassistUtils.createClass(config);
        Schema<?> schema = RuntimeSchema.createFrom(cls);
        return WrapSchemaFactory.createSchema(schema, config.getType());
    }

    // 适用于将单个类型包装的场景
    // 比如return
    public static WrapSchema getOrCreateSchema(Class<?> type) {
        return getOrCreateSchema(type, (String) null);
    }

    public static WrapSchema getOrCreateSchema(JavaType javaType) {
        return getOrCreateSchema(javaType.getRawClass(), javaType.getGenericSignature());
    }

    public static WrapSchema getOrCreateSchema(Class<?> type, Type genericType) {
        return getOrCreateSchema(type,
                TypeFactory.defaultInstance().constructType(genericType).getGenericSignature());
    }

    public static WrapSchema getOrCreateSchema(Class<?> type, String genericSignature) {
        String key = type.getCanonicalName();
        if (genericSignature != null) {
            key = genericSignature;
        }
        return getOrCreateSchema(key, () -> {
            if (!isNeedWrap(type)) {
                // 可以直接使用
                Schema<?> schema = RuntimeSchema.createFrom(type);
                return WrapSchemaFactory.createSchema(schema, WrapType.NOT_WRAP);
            }

            // 需要包装
            WrapClassConfig config = new WrapClassConfig();
            config.setType(WrapType.NORMAL_WRAP);
            // 为了处理泛型包装的场景，必须加上uuid
            // 否则List<List<String>>就会与List<String>冲突
            config.setClassName(
                    "gen." + type.getCanonicalName() + "." + UUID.randomUUID().toString().replaceAll("-", ""));
            if (!Void.TYPE.isAssignableFrom(type)) {
                config.addField("field0", type, genericSignature);
            }

            JavassistUtils.genSingleWrapperInterface(config);

            return createWrapSchema(config);
        });
    }

    public static WrapSchema getOrCreateArgsSchema(OperationMeta operationMeta) {
        Method method = operationMeta.getMethod();
        String type = "gen." + method.getDeclaringClass().getName() + "." + method.getName() + ".Args";

        return getOrCreateSchema(type, () -> {
            if (!isArgsNeedWrap(method)) {
                // 可以直接使用
                Class<?> cls = (Class<?>) method.getParameterTypes()[0];
                Schema<?> schema = RuntimeSchema.createFrom(cls);
                return WrapSchemaFactory.createSchema(schema, WrapType.ARGS_NOT_WRAP);
            }

            // 需要包装
            WrapClassConfig config = new WrapClassConfig();
            config.setType(WrapType.ARGS_WRAP);
            config.setClassName(type);

            Parameter[] params = method.getParameters();
            for (int idx = 0; idx < params.length; idx++) {
                Parameter param = params[idx];
                String paramName = operationMeta.getParamName(idx);

                config.addField(paramName, param.getParameterizedType());
            }

            JavassistUtils.genMultiWrapperInterface(config);

            return createWrapSchema(config);
        });
    }
}
