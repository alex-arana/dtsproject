package org.dataminx.dts.common.batch.util;

import static org.dataminx.dts.common.DtsConstants.FILE_ROOT_PROTOCOL;
import static org.dataminx.dts.common.DtsConstants.TMP_ROOT_PROTOCOL;
import static org.mockito.Matchers.anyString;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "unit-test" })
public class RootFileObjectComparatorTest {

    public void testCompare() {
        final RootFileObjectComparator comparator = new RootFileObjectComparator();
        Assert.assertEquals(comparator.compare(TMP_ROOT_PROTOCOL, FILE_ROOT_PROTOCOL), 0);
        Assert.assertEquals(comparator.compare(FILE_ROOT_PROTOCOL, TMP_ROOT_PROTOCOL), 0);
        Assert.assertEquals(comparator.compare(TMP_ROOT_PROTOCOL, TMP_ROOT_PROTOCOL), 0);
        Assert.assertEquals(comparator.compare(FILE_ROOT_PROTOCOL, FILE_ROOT_PROTOCOL), 0);
        Assert.assertTrue(comparator.compare(FILE_ROOT_PROTOCOL, anyString()) != 0);
    }
}
