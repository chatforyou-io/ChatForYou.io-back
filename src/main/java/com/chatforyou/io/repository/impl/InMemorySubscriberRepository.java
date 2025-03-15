package com.chatforyou.io.repository.impl;

import com.chatforyou.io.repository.SubscriberRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySubscriberRepository<K, T> implements SubscriberRepository<K, T> {
    private final Map<K, T> store = new ConcurrentHashMap<>();

    @Override
    public void add(K key, T obj) {
        store.put(key, obj);
    }

    @Override
    public T get(K key) {
        return store.get(key);
    }

    @Override
    public void remove(K key) {
        store.remove(key);
    }

    @Override
    public Map<K, T> getAll() {
        return store;
    }
}
