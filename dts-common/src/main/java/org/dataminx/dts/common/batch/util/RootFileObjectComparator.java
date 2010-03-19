package org.dataminx.dts.common.batch.util;

import static org.dataminx.dts.common.DtsConstants.FILE_ROOT_PROTOCOL;
import static org.dataminx.dts.common.DtsConstants.TMP_ROOT_PROTOCOL;

import java.util.Comparator;

public class RootFileObjectComparator implements Comparator<Object> {

    public int compare(final Object arg0, final Object arg1) {
        if (arg0.toString().startsWith(TMP_ROOT_PROTOCOL) && arg1.toString().startsWith(FILE_ROOT_PROTOCOL)
                || arg1.toString().startsWith(TMP_ROOT_PROTOCOL) && arg0.toString().startsWith(FILE_ROOT_PROTOCOL)
                || arg0.equals(arg1)) {
            return 0;
        }
        else {
            return -1;
        }
    }
}
