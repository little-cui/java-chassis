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

package com.huawei.paas.cse.serviceregistry.client.http;

import com.huawei.paas.foundation.common.net.IpPort;
import com.huawei.paas.foundation.vertx.client.http.HttpClientWithContext;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

/**
 * Created by on 2017/4/28.
 */
public final class WebsocketUtils {
    private WebsocketUtils() {
    }

    public static void open(IpPort ipPort, String url, Handler<Void> onOpen, Handler<Void> onClose,
            Handler<Buffer> onMessage, Handler<Throwable> onException,
            Handler<Throwable> onConnectFailed) {
        HttpClientWithContext vertxHttpClient = WebsocketClientPool.INSTANCE.getClient();
        vertxHttpClient.runOnContext(client -> {
            client.websocket(ipPort.getPort(),
                    ipPort.getHostOrIp(),
                    url,
                    RestUtils.getDefaultHeaders(),
                    ws -> {
                        onOpen.handle(null);

                        ws.exceptionHandler(onException);

                        ws.closeHandler(v -> {
                            onClose.handle(v);
                            try {
                                ws.close();
                            } catch (Exception err) {
                            }
                        });
                        ws.handler(onMessage);
                    },
                    onConnectFailed);
        });
    }
}
