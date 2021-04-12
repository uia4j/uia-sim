package uia.road;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SimInfo {

    private Map<String, Object> info;

    public SimInfo() {
        this.info = new TreeMap<>();
    }

    public SimInfo setInt(String key, int value) {
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

    public SimInfo setString(String key, String value) {
        this.info.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public SimInfo addString(String key, String value) {
        Object v = this.info.get(key);
        if (v == null || !(v instanceof List)) {
            v = new ArrayList<String>();
            this.info.put(key, v);
        }
        ((List<String>) v).add(value);
        return this;
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
}
