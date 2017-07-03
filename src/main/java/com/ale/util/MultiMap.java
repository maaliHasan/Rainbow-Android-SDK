package com.ale.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by grobert on 12/05/16.
 */
public class MultiMap<K, V> {

    private final Map<K, List<V>> mInternalMap;

    public MultiMap() {
        mInternalMap = new HashMap<K, List<V>>();
    }

    /**
     * Clears the map.
     */
    public void clear() {
        mInternalMap.clear();
    }

    /**
     * Checks whether the map contains the specified key.
     *
     */
    public boolean containsKey(K key) {
        return mInternalMap.containsKey(key);
    }

    /**
     * Checks whether the map contains the specified value.
     *
     */
    public boolean containsValue(V value) {
        for (List<V> valueList : mInternalMap.values()) {
            if (valueList.contains(value)) {
                return true;
            }
        }
        return false;
    }


    public boolean containsValues(List<V> values){
        for(V value : values){
            if(containsValue(value)){
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the list of values associated with each key.
     */
    public List<V> get(K key) {
        return mInternalMap.get(key);
    }

    /**
     * @see {@link Map#isEmpty()}
     */
    public boolean isEmpty() {
        return mInternalMap.isEmpty();
    }

    /**
     * Check if map is empty.
     */
    public Set<K> keySet() {
        return mInternalMap.keySet();
    }

    /**
     * Adds the value to the list associated with a key if
     * the value is not already stored.
     *
     */
    public V put(K key, V value) {
        List<V> valueList = mInternalMap.get(key);
        if (valueList == null) {
            valueList = new LinkedList<V>();
            mInternalMap.put(key, valueList);
        }
        // add a unique value:
        boolean storedValue = false;
        for(V v : valueList){
            if(v.equals(value)){
                storedValue = true;
                break;
            }
        }

        if(!storedValue){
            valueList.add(value);
        }
        return value;
    }

    /**
     *
     * Adds all entries in given {@link Map} to this {@link MultiMap}.
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Adds all entries in given {@link MultiMap} to this {@link MultiMap}.
     */
    public void putAll(MultiMap<K, ? extends V> m) {
        for (K key : m.keySet()) {
            List<? extends V> values = m.get(key);
            if (values != null) {
                for (V value : values) {
                    put(key, value);
                }
            }
        }
    }

    /**
     * Removes all values associated with the specified key.
     */
    public List<V> remove(K key) {
        return mInternalMap.remove(key);
    }

    /**
     * Returns the number of keys in the map
     */
    public int size() {
        return mInternalMap.size();
    }

    /**
     * Returns list of all values.
     */
    public List<V> values() {
        List<V> allValues = new LinkedList<V>();
        for (List<V> valueList : mInternalMap.values()) {
            allValues.addAll(valueList);
        }
        return allValues;
    }

    /**
     * Construct a new map, that contains a unique String key for each value.
     *
     * Current algorithm will construct unique key by appending a unique position number to key's toString() value
     *
     * @return a {@link Map}
     */
    public Map<String, V> getUniqueMap() {
        Map<String, V> uniqueMap = new HashMap<String, V>();
        for (Map.Entry<K, List<V>> entry : mInternalMap.entrySet()) {
            int count = 1;
            for (V value : entry.getValue()) {
                if (count == 1) {
                    addUniqueEntry(uniqueMap, entry.getKey().toString(), value);
                } else {
                    // append unique number to key for each value
                    addUniqueEntry(uniqueMap, String.format("%s%d", entry.getKey(), count), value);
                }
                count++;
            }
        }
        return uniqueMap;
    }


    /**
     * Recursive method that will append characters to proposedKey until its unique. Used in case there are collisions with generated key values.
     *
     * @param uniqueMap
     * @param proposedKey
     * @param value
     */
    private String addUniqueEntry(Map<String, V> uniqueMap, String proposedKey, V value) {
        // not the most efficient algorithm, but should work
        if (uniqueMap.containsKey(proposedKey)) {
            return addUniqueEntry(uniqueMap, String.format("%s%s", proposedKey, "X"), value);
        } else {
            uniqueMap.put(proposedKey, value);
            return proposedKey;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return mInternalMap.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MultiMap<?, ?> other = (MultiMap<?, ?>) obj;
        return equalObjects(mInternalMap, other.mInternalMap);
    }

    public static boolean equalObjects(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @Override
    public String toString() {
        String str= " ";
        Set<K> keyset = keySet();
        Iterator<K> it = keyset.iterator();
        while(it.hasNext()){
            K key = it.next();
            str += key.toString() + "=";
            for(V value : get(key)){
                str += "'" + value.toString() + "'";
            }
            str += " ";
        }
        return str;
    }
}
