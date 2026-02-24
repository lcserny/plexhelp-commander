package net.cserny.support;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

public class LRUCache<K, V> {

    private final int capacity;
    private final Deque<K> deque;
    private final HashMap<K, V> map;

    private int cacheMisses = 0;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.deque = new LinkedList<>();
        this.map = new HashMap<>();
    }

    public V get(K key) {
        if (!map.containsKey(key)) {
            this.cacheMisses += 1;
            return null;
        }

        // Move the accessed key to the front of the deque
        deque.remove(key);
        deque.addFirst(key);

        return map.get(key);
    }

    public void put(K key, V value) {
        if (map.containsKey(key)) {
            // If key already exists, move it to the front
            deque.remove(key);
        } else if (map.size() >= capacity) {
            // If at capacity, remove the least recently used item
            K leastUsed = deque.removeLast();
            map.remove(leastUsed);
        }

        deque.addFirst(key);
        map.put(key, value);
    }

    public int size() {
        return map.size();
    }

    public int cacheMisses() {
        return this.cacheMisses;
    }
}
