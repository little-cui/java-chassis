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

package com.huawei.paas.foundation.vertx.client.tcp;

import org.junit.Assert;
import org.junit.Test;

import com.huawei.paas.foundation.vertx.client.tcp.TcpClientConfig;

public class TestTcpClientConfig {
    @Test
    public void testTcpClientConfig() {
        TcpClientConfig config = new TcpClientConfig();
        Assert.assertEquals(config.getRequestTimeoutMillis(), 30000);
        Assert.assertEquals(config.isSsl(), false);
        config.setRequestTimeoutMillis(500);
        Assert.assertEquals(config.getRequestTimeoutMillis(), 500);
    }
}
