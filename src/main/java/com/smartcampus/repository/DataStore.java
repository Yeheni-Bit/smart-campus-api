package com.smartcampus.repository;

import com.smartcampus.model.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();

    private DataStore() {
    }
}