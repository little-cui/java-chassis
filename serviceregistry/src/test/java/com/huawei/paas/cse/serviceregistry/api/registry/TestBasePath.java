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

package com.huawei.paas.cse.serviceregistry.api.registry;

import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by   on 2017/3/29.
 */
public class TestBasePath {
    private BasePath oBasePath;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        oBasePath = new BasePath();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        oBasePath = null;
    }

    /**
     * Test un-initialized Values
     */
    @Test
    public void testDefaultValues() {
        Assert.assertNull(oBasePath.getPath());
        Assert.assertNull(oBasePath.getProperty());
    }

    /**
     * Test Getter and Setter
     * Its insane but need to do it
     */
    @Test
    public void testIntializedValues() {
        initBasePath(); //Initialize the Values
        Assert.assertEquals("a", oBasePath.getPath());
        Assert.assertNotNull(oBasePath.getProperty());
    }

    /**
     * Initialize the Values
     */
    private void initBasePath() {
        oBasePath.setPath("a");
        oBasePath.setProperty(new HashMap<>());
    }
}
