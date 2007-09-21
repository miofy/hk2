package org.jvnet.hk2.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Map from a key to multiple values.
 * Order is significant among values.
 * 
 * @author Kohsuke Kawaguchi
 */
public class MultiMap<K,V> {
    private final Map<K,List<V>> store;

    /**
     * Creates an empty multi-map.
     */
    public MultiMap() {
        store = new HashMap<K, List<V>>();
    }

    /**
     * Creates a multi-map backed by the given store.
     */
    private MultiMap(Map<K,List<V>> store) {
        this.store = store;
    }

    /**
     * Adds one more value.
     */
    public final void add(K k,V v) {
        List<V> l = store.get(k);
        if(l==null) {
            l = new ArrayList<V>();
            store.put(k,l);
        }
        l.add(v);
    }

    /**
     * Replaces all the existing values associated with the key
     * by the given value.
     *
     * @param v
     *      Can be null or empty.
     */
    public void set(K k, List<V> v) {
        store.put(k,new ArrayList<V>(v));
    }

    /**
     * @return
     *      Can be empty but never null. Read-only.
     */
    public final List<V> get(K k) {
        List<V> l = store.get(k);
        if(l==null) return Collections.emptyList();
        return l;
    }

    /**
     * Gets the first value if any, or null.
     */
    public final V getOne(K k) {
        List<V> lst = store.get(k);
        if(lst ==null)  return null;
        if(lst.isEmpty())   return null;
        return lst.get(0);
    }

    /**
     * Lists up all entries.
     */
    public Set<Entry<K,List<V>>> entrySet() {
        return store.entrySet();
    }

    /**
     * Format the map as "key=value1,key=value2,...."
     */
    public String toCommaSeparatedString() {
        StringBuilder buf = new StringBuilder();
        for (Entry<K,List<V>> e : entrySet()) {
            for (V v : e.getValue()) {
                if(buf.length()>0)  buf.append(',');
                buf.append(e.getKey()).append('=').append(v);
            }
        }
        return buf.toString();
    }

    private static final MultiMap EMPTY = new MultiMap(Collections.emptyMap());

    /**
     * Gets the singleton read-only empty multi-map.
     *
     * @see Collections#emptyMap()
     */
    public static <K,V> MultiMap<K,V> emptyMap() {
        return EMPTY;
    }
}
