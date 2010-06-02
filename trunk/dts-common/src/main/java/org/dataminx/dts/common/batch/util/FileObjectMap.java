/**
 * Copyright (c) 2010, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(final Object key) {
        if (key.toString().startsWith(TMP_ROOT_PROTOCOL)) {
            return super.get(FILE_ROOT_PROTOCOL);
        }
        return super.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public V put(final K key, final V value) {
        if (key.toString().startsWith(TMP_ROOT_PROTOCOL)) {
            return super.put((K) FILE_ROOT_PROTOCOL, value);
        }
        return super.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final Object key) {
        if (key.toString().startsWith(TMP_ROOT_PROTOCOL)) {
            return super.containsKey(FILE_ROOT_PROTOCOL);
        }
        return super.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V remove(final Object key) {
        if (key.toString().startsWith(TMP_ROOT_PROTOCOL)) {
            return super.remove(FILE_ROOT_PROTOCOL);
        }
        return super.remove(key);
    }

}
