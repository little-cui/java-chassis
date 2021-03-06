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

package com.huawei.paas.cse.core.endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

import com.huawei.paas.cse.core.Transport;
import com.huawei.paas.cse.core.transport.TransportManager;
import com.huawei.paas.cse.serviceregistry.cache.CacheEndpoint;
import com.huawei.paas.cse.serviceregistry.cache.InstanceCache;
import com.huawei.paas.cse.serviceregistry.cache.InstanceCacheManager;

/**
 * registry模块不理解core中的概念
 * 所以要将字符串的各种信息转义一下，方便运行时使用
 * @author   
 * @version  [版本号, 2017年1月26日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class AbstractEndpointsCache<ENDPOINT> {
    static TransportManager transportManager;

    protected List<ENDPOINT> endpoints = new ArrayList<>();

    protected String transportName;

    protected InstanceCache instanceCache = null;

    public static void setTransportManager(TransportManager transportManager) {
        AbstractEndpointsCache.transportManager = transportManager;
    }

    /**
     * <构造函数>
     * @param appId
     * @param microserviceName
     * @param microserviceVersionRule
     * @param transportName 可能为""，表示走任意健康的地址即可
     */
    public AbstractEndpointsCache(String appId, String microserviceName, String microserviceVersionRule,
            String transportName) {
        this.transportName = transportName;
        this.instanceCache = new InstanceCache(appId, microserviceName, microserviceVersionRule, null);
    }

    public List<ENDPOINT> getLatestEndpoints() {
        InstanceCache newCache = InstanceCacheManager.INSTANCE.getOrCreate(instanceCache.getAppId(),
                instanceCache.getMicroserviceName(),
                instanceCache.getMicroserviceVersionRule());
        if (!instanceCache.cacheChanged(newCache)) {
            return endpoints;
        }

        // 走到这里，肯定已经是存在"有效"地址了(可能是个空列表，表示没有存活的实例)
        // 先创建，成功了，再走下面的更新逻辑
        List<ENDPOINT> tmpEndpoints = createEndpints(newCache);

        this.instanceCache = newCache;
        this.endpoints = tmpEndpoints;
        return endpoints;
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param newCache
     * @return
     */
    protected List<ENDPOINT> createEndpints(InstanceCache newCache) {
        Map<String, List<CacheEndpoint>> transportMap = getOrCreateTransportMap(newCache);

        return createEndpoints(transportMap);
    }

    protected List<ENDPOINT> createEndpoints(Map<String, List<CacheEndpoint>> transportMap) {
        List<ENDPOINT> tmpEndpoints = new ArrayList<>();
        for (Entry<String, List<CacheEndpoint>> entry : transportMap.entrySet()) {
            Transport transport = transportManager.findTransport(entry.getKey());
            if (transport == null) {
                continue;
            }

            List<CacheEndpoint> endpointList = entry.getValue();
            if (endpointList == null) {
                continue;
            }

            for (CacheEndpoint cacheEndpont : endpointList) {
                ENDPOINT endpoint = createEndpoint(transport, cacheEndpont);
                tmpEndpoints.add(endpoint);
            }
        }
        return tmpEndpoints;
    }

    private Map<String, List<CacheEndpoint>> getOrCreateTransportMap(InstanceCache newCache) {
        Map<String, List<CacheEndpoint>> allTransportMap = newCache.getOrCreateTransportMap();
        if (StringUtils.isEmpty(transportName)) {
            // 未指定tranport，将所有transport全取出来
            return allTransportMap;
        }

        Map<String, List<CacheEndpoint>> transportMap = new HashMap<>();
        transportMap.put(transportName, allTransportMap.get(transportName));
        return transportMap;
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param transport
     * @param address
     * @return
     */
    protected abstract ENDPOINT createEndpoint(Transport transport, CacheEndpoint cacheEndpoint);
}
