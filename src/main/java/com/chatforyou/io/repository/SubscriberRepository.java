package com.chatforyou.io.repository;

import java.util.Map;

public interface SubscriberRepository<K, T> {
    void add(K key, T subscriber); // key 를 기준으로 t 를 추가
    T get(K key); // 가져오기
    void remove(K key); // 제거
    Map<K, T> getAll();
}
