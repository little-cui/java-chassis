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

package com.huawei.paas.cse.provider.pojo.schema;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.huawei.paas.cse.provider.pojo.IPerson;
import com.huawei.paas.cse.provider.pojo.Person;
import com.huawei.paas.cse.provider.pojo.RpcSchema;

import mockit.Expectations;
import mockit.Injectable;

public class TestPojoProducers {
    @Test
    public void testPojoProducers(@Injectable ApplicationContext applicationContext) {
        Person bean = new Person();
        PojoProducers producer = new PojoProducers();
        producer.processProvider(applicationContext, "test", bean);
        Assert.assertEquals(producer.getProcucers().size(), 1);
    }

    @Test
    public void testPojoProducersSchemaNull(@Injectable ApplicationContext applicationContext,
            @Injectable RpcSchema schema) {
        IPerson bean = new IPerson() {
        };
        PojoProducers producer = new PojoProducers();
        producer.processProvider(applicationContext, "test", bean);
        Assert.assertEquals(producer.getProcucers().size(), 0);
    }

    @RpcSchema
    static class PersonEmptySchema implements IPerson {

    }

    @Test
    public void testPojoProducersSchemaIdNull(@Injectable ApplicationContext applicationContext,
            @Injectable RpcSchema schema) {
        IPerson bean = new PersonEmptySchema();
        new Expectations() {
            {
            }
        };
        PojoProducers producer = new PojoProducers();
        producer.processProvider(applicationContext, "test", bean);
        Assert.assertEquals(producer.getProcucers().size(), 1);
    }
}
