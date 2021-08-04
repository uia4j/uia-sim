package uia.road;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class SimInfo {

    private final ConcurrentHashMap<String, Object> info;

    public SimInfo() {
        this.info = new ConcurrentHashMap<>();
    }

    public SimInfo setBoolean(String key, boolean value) {
        this.info.put(key, value);
        return this;
    }

    public SimInfo setInt(String key, int value) {
        this.info.put(key, value);
        return this;
    }

    public SimInfo setString(String key, String value) {
        this.info.put(key, value == null ? "" : value);
        return this;
    }

    public SimInfo setValue(String key, Object value) {
        this.info.put(key, value);
        return this;
    }

    public SimInfo addInt(String key, int value) {
        if (!this.info.containsKey(key)) {
            this.info.put(key, value);
        }
        else {
            this.info.put(key, (int) this.info.get(key) + value);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public SimInfo addString(String key, String value) {
        Object v = this.info.get(key);
        if (v == null || !(v instanceof List)) {
            v = new ArrayList<String>();
            this.info.put(key, v);
        }
        if (value != null) {
            ((List<String>) v).add(value);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public SimInfo addStrings(String key, Collection<String> values) {
        Object v = this.info.get(key);
        if (v == null || !(v instanceof List)) {
            v = new ArrayList<String>();
            this.info.put(key, v);
        }
        if (values != null) {
            ((List<String>) v).addAll(values);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public SimInfo addValue(String key, Object value) {
        Object v = this.info.get(key);
        if (v == null || !(v instanceof List)) {
            v = new ArrayList<Object>();
            this.info.put(key, v);
        }
        if (value != null) {
            ((List<Object>) v).add(value);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> SimInfo addValues(String key, List<T> values) {
        Object v = this.info.get(key);
        if (v == null || !(v instanceof List)) {
            v = new ArrayList<T>();
            this.info.put(key, v);
        }
        if (values != null) {
            ((List<T>) v).addAll(values);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) this.info.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        T v = (T) this.info.get(key);
        return v == null ? defaultValue : v;
    }

    public SimInfo getInfo(String key) {
        SimInfo info = (SimInfo) this.info.get(key);
        if (info == null) {
            info = new SimInfo();
            this.info.put(key, info);
        }
        return info;
    }

    public Map<String, Object> toMap() {
        final TreeMap<String, Object> result = new TreeMap<>();
        this.info.forEach((k, v) -> {
            if (v instanceof SimInfo) {
                result.put(k, ((SimInfo) v).toMap());
            }
            else {
                result.put(k, v);
            }
        });
        return result;
    }

    @Override
    public String toString() {
        return this.info.toString();
    }
}
