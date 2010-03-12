package org.dataminx.dts.common.batch.util;

import static org.dataminx.dts.common.DtsConstants.FILE_ROOT_PROTOCOL;
import static org.dataminx.dts.common.DtsConstants.TMP_ROOT_PROTOCOL;

import java.util.HashMap;

/**
 * A customised HashMap implementation that maps a tmp:// and file:// root URLs
 * into a single key.
 * 
 * @author Gerson Galang
 */
public class FileObjectMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = 1L;

    @Override
    public V get(final Object key) {
        if (key.toString().startsWith(TMP_ROOT_PROTOCOL)) {
            return super.get(FILE_ROOT_PROTOCOL);
        }
        return super.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(final K key, final V value) {
        if (key.toString().startsWith(TMP_ROOT_PROTOCOL)) {
            return super.put((K) FILE_ROOT_PROTOCOL, value);
        }
        return super.put(key, value);
    }

    @Override
    public boolean containsKey(final Object key) {
        if (key.toString().startsWith(TMP_ROOT_PROTOCOL)) {
            return super.containsKey(FILE_ROOT_PROTOCOL);
        }
        return super.containsKey(key);
    }

    @Override
    public V remove(final Object key) {
        if (key.toString().startsWith(TMP_ROOT_PROTOCOL)) {
            return super.remove(FILE_ROOT_PROTOCOL);
        }
        return super.remove(key);
    }

}
