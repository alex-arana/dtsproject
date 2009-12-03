/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.batch;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A container class that can be used to keep track of files to be copied during a DTS file transfer
 * operation.
 *
 * @author Alex Arana
 */
public final class DtsFileTransferDetailsPlan implements Map<String, DtsFileTransferDetails> {
    /** Internal map used to keep track of entity statistics. */
    private final Map<String, DtsFileTransferDetails> mInternalMap =
        new LinkedHashMap<String, DtsFileTransferDetails>();

    /**
     * @{inheritDoc}
     */
    @Override
    public void clear() {
        mInternalMap.clear();
    }

    @Override
    public boolean containsKey(final Object key) {
        return mInternalMap.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return mInternalMap.containsValue(value);
    }

    @Override
    public Set<Map.Entry<String, DtsFileTransferDetails>> entrySet() {
        return mInternalMap.entrySet();
    }

    @Override
    public DtsFileTransferDetails get(final Object key) {
        return mInternalMap.get(key);
    }

    @Override
    public boolean isEmpty() {
        return mInternalMap.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return mInternalMap.keySet();
    }

    @Override
    public DtsFileTransferDetails put(final String key, final DtsFileTransferDetails value) {
        return mInternalMap.put(key, value);
    }

    @Override
    public void putAll(final Map<? extends String, ? extends DtsFileTransferDetails> map) {
        mInternalMap.putAll(map);
    }

    @Override
    public DtsFileTransferDetails remove(final Object key) {
        return mInternalMap.remove(key);
    }

    @Override
    public int size() {
        return mInternalMap.size();
    }

    @Override
    public Collection<DtsFileTransferDetails> values() {
        return mInternalMap.values();
    }

    /**
     * Returns an array of {@link DtsFileTransferDetails} elements containing a detailed execution plan for
     * a single DTS Job request.
     *
     * @return an array of <code>DtsFileTransferDetails</code> elements
     */
    public DtsFileTransferDetails[] getFileTransfers() {
        return values().toArray(new DtsFileTransferDetails[] {});
    }

    /**
     * Returns the total number of bytes to be transferred from the source as part of this file transfer plan.
     *
     * @return total number of bytes to be transferred from the source during this operation
     */
    public long getTotalTransferSize() {
        long totalSize = 0L;
        for (final DtsFileTransferDetails fileTransferDetails : getFileTransfers()) {
            totalSize += fileTransferDetails.getTotalBytes();
        }
        return totalSize;
    }
}
