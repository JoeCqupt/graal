/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.graalvm.collections;

import java.util.Objects;

/**
 * Utility methods for the {@link EconomicMap}.
 *
 * @since 23.0
 */
public final class EconomicMapUtil {
    /**
     * @since 23.0
     */
    private EconomicMapUtil() {

    }

    /**
     * Compares maps for equality. The maps are equal iff they share the same
     * {@link org.graalvm.collections.Equivalence equivalence strategy}, their keys are equal with
     * respect to the strategy and the values are equal as determined by the
     * {@link Objects#equals(Object, Object) equals} method.
     *
     * @param lhs the first map to be compared
     * @param rhs the second map to be compared
     * @return {@code true} iff the maps are equal
     * @since 23.0
     */
    public static <K, V> boolean equals(UnmodifiableEconomicMap<K, V> lhs, UnmodifiableEconomicMap<K, V> rhs) {
        if (lhs == rhs) {
            return true;
        }
        if (lhs == null || rhs == null || lhs.size() != rhs.size() || !Objects.equals(lhs.getEquivalenceStrategy(), rhs.getEquivalenceStrategy())) {
            return false;
        }
        UnmodifiableMapCursor<K, V> cursor = rhs.getEntries();
        while (cursor.advance()) {
            if (!lhs.containsKey(cursor.getKey()) || !Objects.equals(lhs.get(cursor.getKey()), cursor.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Computes an order-independent hash code for an {@link EconomicMap}.
     *
     * @param map the input map or {@code null}
     * @return the hash code of the map
     * @since 23.0
     */
    public static <K, V> int hashCode(UnmodifiableEconomicMap<K, V> map) {
        if (map == null) {
            return -1;
        }
        int keyHash = 0;
        int valueHash = 0;
        UnmodifiableMapCursor<K, V> cursor = map.getEntries();
        while (cursor.advance()) {
            keyHash ^= cursor.getKey().hashCode();
            if (cursor.getValue() != null) {
                valueHash ^= cursor.getValue().hashCode();
            }
        }
        return keyHash + 31 * valueHash;
    }

    /**
     * Returns an {@link EconomicSet} of the keys contained in a map.
     *
     * @param map the input map
     * @return an {@link EconomicSet} of the keys contained in a map
     * @since 23.0
     */
    public static <K, V> EconomicSet<K> keySet(EconomicMap<K, V> map) {
        EconomicSet<K> set = EconomicSet.create(map.size());
        for (K key : map.getKeys()) {
            set.add(key);
        }
        return set;
    }
}
