package org.dataminx.dts.common.batch.util;

import static org.dataminx.dts.common.DtsConstants.FILE_ROOT_PROTOCOL;
import static org.dataminx.dts.common.DtsConstants.TMP_ROOT_PROTOCOL;
import static org.mockito.Matchers.anyObject;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "unit-test" })
public class FileObjectMapTest {

    public void testGet() {
        final FileObjectMap<String, Object> fileObjectMap = new FileObjectMap<String, Object>();
        fileObjectMap.put(FILE_ROOT_PROTOCOL, anyObject());
        Assert.assertEquals(fileObjectMap.get(FILE_ROOT_PROTOCOL), anyObject());
        Assert.assertEquals(fileObjectMap.get(TMP_ROOT_PROTOCOL), anyObject());
    }

    public void testPut() {
        final FileObjectMap<String, Object> fileObjectMap = new FileObjectMap<String, Object>();
        fileObjectMap.put(TMP_ROOT_PROTOCOL, anyObject());
        Assert.assertEquals(fileObjectMap.get(FILE_ROOT_PROTOCOL), anyObject());
        Assert.assertEquals(fileObjectMap.get(TMP_ROOT_PROTOCOL), anyObject());
    }

    public void testContainsKey() {
        final FileObjectMap<String, Object> fileObjectMap = new FileObjectMap<String, Object>();
        Assert.assertTrue(!fileObjectMap.containsKey(FILE_ROOT_PROTOCOL));
        fileObjectMap.put(FILE_ROOT_PROTOCOL, anyObject());
        Assert.assertTrue(fileObjectMap.containsKey(FILE_ROOT_PROTOCOL));
        Assert.assertTrue(fileObjectMap.containsKey(TMP_ROOT_PROTOCOL));
    }

    public void testRemove() {
        final FileObjectMap<String, Object> fileObjectMap = new FileObjectMap<String, Object>();
        fileObjectMap.put(FILE_ROOT_PROTOCOL, anyObject());
        Assert.assertEquals(fileObjectMap.remove(TMP_ROOT_PROTOCOL), anyObject());
        Assert.assertTrue(fileObjectMap.isEmpty());
        fileObjectMap.put(TMP_ROOT_PROTOCOL, anyObject());
        Assert.assertEquals(fileObjectMap.remove(FILE_ROOT_PROTOCOL), anyObject());
    }

}
