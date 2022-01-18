package dev.gump.worm;

import dev.gump.worm.typeadapter.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WormTypeAdapterRegistry {

    private final Map<Class<?>, WormTypeAdapter<?>> typeAdapterMap = new HashMap<>();

    WormTypeAdapterRegistry() {
        this.registerTypeAdapter(UUID.class, new UUIDTypeAdapter());
        this.registerTypeAdapter(BigDecimal.class, new BigDecimalTypeAdapter());
        this.registerTypeAdapter(Date.class, new DateTypeAdapter());
        this.registerTypeAdapter(Time.class, new TimeTypeAdapter());
        this.registerTypeAdapter(Timestamp.class, new TimestampTypeAdapter());
    }

    public <T> void registerTypeAdapter(Class<T> typeClass, WormTypeAdapter<T> typeAdapter) {
        this.typeAdapterMap.put(typeClass, typeAdapter);
    }

    @SuppressWarnings("unchecked")
    public <T> WormTypeAdapter<T> getTypeAdapter(Class<T> typeClass) {
        return (WormTypeAdapter<T>) this.typeAdapterMap.get(typeClass);
    }

}
