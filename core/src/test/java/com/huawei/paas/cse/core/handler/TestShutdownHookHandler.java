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

package com.huawei.paas.cse.core.handler;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.ws.Holder;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import com.huawei.paas.cse.core.AsyncResponse;
import com.huawei.paas.cse.core.Invocation;
import com.huawei.paas.cse.core.Response;
import com.huawei.paas.cse.core.exception.CommonExceptionData;
import com.huawei.paas.cse.core.exception.InvocationException;
import com.huawei.paas.cse.core.invocation.InvocationType;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestShutdownHookHandler {
    static Field requestCountField = ReflectionUtils.findField(ShutdownHookHandler.class, "requestCounter");

    static {
        requestCountField.setAccessible(true);
    }

    static AtomicLong requestCounter =
        (AtomicLong) ReflectionUtils.getField(requestCountField, ShutdownHookHandler.INSTANCE);

    @Test
    public void testShutdownHookHandlerCount(@Mocked Response response) throws Exception {
        Deencapsulation.setField(ShutdownHookHandler.INSTANCE, "shuttingDown", false);

        ShutdownHookHandler handler = ShutdownHookHandler.INSTANCE;
        Assert.assertEquals(0, handler.getActiveCount());

        // no reply
        Invocation invocation = new MockUp<Invocation>() {
            @Mock
            public void next(AsyncResponse asyncResp) throws Exception {
            }
        }.getMockInstance();
        handler.handle(invocation, asyncResp -> {
        });
        Assert.assertEquals(1, requestCounter.get());
        Assert.assertEquals(1, handler.getActiveCount());

        // normal
        invocation = new MockUp<Invocation>() {
            @Mock
            public void next(AsyncResponse asyncResp) throws Exception {
                asyncResp.handle(response);
            }
        }.getMockInstance();
        handler.handle(invocation, asyncResp -> {
        });
        Assert.assertEquals(2, requestCounter.get());
        Assert.assertEquals(1, handler.getActiveCount());

        // next exception
        invocation = new MockUp<Invocation>() {
            @Mock
            public void next(AsyncResponse asyncResp) throws Exception {
                throw new Error();
            }
        }.getMockInstance();
        try {
            handler.handle(invocation, asyncResp -> {
            });
            Assert.assertFalse(true);
        } catch (Throwable e) {
            Assert.assertEquals(3, requestCounter.get());
            Assert.assertEquals(1, handler.getActiveCount());

        }

        // reply exception
        // TODO: should be fixed
        //        try {
        //            handler.handle(invocation, asyncResp -> {
        //                throw new Error();
        //            });
        //
        //            Assert.assertFalse(true);
        //        } catch (Throwable e) {
        //            Assert.assertEquals(3, requestCounter.get());
        //            Assert.assertEquals(1, handler.getActiveCount());
        //        }
    }

    @Test
    public void testShutdownHookHandlerReject() throws Exception {
        Deencapsulation.setField(ShutdownHookHandler.INSTANCE, "shuttingDown", true);
        Holder<InvocationType> typeHolder = new Holder<>(InvocationType.PRODUCER);
        Invocation invocation = new MockUp<Invocation>() {
            @Mock
            public InvocationType getInvocationType() {
                return typeHolder.value;
            }
        }.getMockInstance();

        ShutdownHookHandler handler = ShutdownHookHandler.INSTANCE;
        handler.handle(invocation, asyncResp -> {
            InvocationException e = asyncResp.getResult();
            Assert.assertEquals(((CommonExceptionData) e.getErrorData()).getMessage(),
                    "shutting down in progress");
            Assert.assertEquals(e.getStatusCode(), 590);
        });

        typeHolder.value = InvocationType.CONSUMER;
        handler.handle(invocation, asyncResp -> {
            InvocationException e = asyncResp.getResult();
            Assert.assertEquals(((CommonExceptionData) e.getErrorData()).getMessage(),
                    "shutting down in progress");
            Assert.assertEquals(e.getStatusCode(), 490);
        });
    }
}
