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

package com.huawei.paas.cse.core.exception;

import javax.ws.rs.core.Response.StatusType;

import com.huawei.paas.cse.core.context.HttpStatus;

/**
 * 业务在provider端，想返回在swagger中定义好的错误
 * 通过抛出本类型的异常来实现
 * 如果不是本类型的异常，则强制认为是PRODUCER_INNER_STATUS_CODE错误
 * @author
 * @version  [版本号, 2017年1月6日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class InvocationException extends RuntimeException {
    private static final long serialVersionUID = 8027482777502649656L;

    /**
     * http header中的statusCode
     * 不直接使用Status类型，是为了支持业务自定义code
     */
    private StatusType status;

    private Object errorData;

    public InvocationException(StatusType status, Object errorData) {
        this.status = status;
        this.errorData = errorData;
    }

    public InvocationException(StatusType status, String msg) {
        this.status = status;
        this.errorData = new CommonExceptionData(msg);
    }

    public InvocationException(int statusCode, String reasonPhrase, Object errorData, Throwable cause) {
        super(cause);
        status = new HttpStatus(statusCode, reasonPhrase);
        this.errorData = errorData;
    }

    public InvocationException(int statusCode, String reasonPhrase, Object errorData) {
        status = new HttpStatus(statusCode, reasonPhrase);
        this.errorData = errorData;
    }

    /**
     * 获取status的值
     * @return 返回 status
     */
    public StatusType getStatus() {
        return status;
    }

    /**
     * 获取statusCode的值
     * @return 返回 statusCode
     */
    public int getStatusCode() {
        return status.getStatusCode();
    }

    /**
     * 获取reasonPhrase的值
     * @return 返回 reasonPhrase
     */
    public String getReasonPhrase() {
        return status.getReasonPhrase();
    }

    /**
     * 获取errorData的值
     * @return 返回 errorData
     */
    public Object getErrorData() {
        return errorData;
    }

    @Override
    public String getMessage() {
        return this.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InvocationException: code=");
        sb.append(getStatusCode());
        sb.append(";msg=");
        sb.append(getErrorData());
        return sb.toString();
    }
}
