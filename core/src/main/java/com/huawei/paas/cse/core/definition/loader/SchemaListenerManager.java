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

package com.huawei.paas.cse.core.definition.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.huawei.paas.cse.core.definition.MicroserviceMeta;
import com.huawei.paas.cse.core.definition.MicroserviceMetaManager;
import com.huawei.paas.cse.core.definition.SchemaMeta;

/**
 * key为microserviceName
 * @author  
 * @version [版本号, 2016年11月30日]
 * @see  [相关类/方法]
 * @since [产品/模块版本]
 */
@Component
public class SchemaListenerManager {
    @Inject
    private List<SchemaListener> schemaListenerList;

    @Inject
    private MicroserviceMetaManager microserviceMetaManager;

    public void setSchemaListenerList(List<SchemaListener> schemaListenerList) {
        this.schemaListenerList = schemaListenerList;
    }

    public void setMicroserviceMetaManager(MicroserviceMetaManager microserviceMetaManager) {
        this.microserviceMetaManager = microserviceMetaManager;
    }

    public void notifySchemaListener(MicroserviceMeta... microserviceMetas) {
        List<SchemaMeta> schemaMetaList = new ArrayList<>();
        for (MicroserviceMeta microserviceMeta : microserviceMetas) {
            schemaMetaList.addAll(microserviceMeta.getSchemaMetas());
        }

        notifySchemaListener(schemaMetaList.toArray(new SchemaMeta[schemaMetaList.size()]));
    }

    public void notifySchemaListener() {
        Collection<MicroserviceMeta> microserviceMetas = microserviceMetaManager.values();
        notifySchemaListener(microserviceMetas.toArray(new MicroserviceMeta[microserviceMetas.size()]));
    }

    public void notifySchemaListener(SchemaMeta... schemaMetas) {
        for (SchemaListener listener : schemaListenerList) {
            listener.onSchemaLoaded(schemaMetas);
        }
    }

    public void notifySchemaListener(List<SchemaMeta> schemaMetaList) {
        SchemaMeta[] schemaMetas = schemaMetaList.toArray(new SchemaMeta[schemaMetaList.size()]);
        notifySchemaListener(schemaMetas);
    }

    public SchemaMeta ensureFindSchemaMeta(String microserviceName, String schemaId) {
        MicroserviceMeta microserviceMeta = microserviceMetaManager.ensureFindValue(microserviceName);
        return microserviceMeta.ensureFindSchemaMeta(schemaId);
    }

    public Collection<SchemaMeta> getAllSchemaMeta(String microserviceName) {
        MicroserviceMeta microserviceMeta = microserviceMetaManager.ensureFindValue(microserviceName);
        return microserviceMeta.getSchemaMetas();
    }
}
