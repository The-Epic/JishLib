package me.jishuna.jishlib.config;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigType<T> {

    private Class<T> clazz;
    private List<ConfigType<?>> componentTypes;

    public static ConfigType<?> create(Type type) {
        if (type instanceof Class) {
            return create((Class<?>) type, new ArrayList<>());
        }
        return create(type.getTypeName());
    }

    public static ConfigType<?> get(Field field) {
        return create(field.getGenericType());
    }

    private static ConfigType<?> create(String typeName) {
        try {
            int ind = typeName.indexOf('<');
            if (ind == -1) {
                return new ConfigType<>(Class.forName(typeName));
            }
            Class<?> clazz = Class.forName(typeName.substring(0, ind));
            List<ConfigType<?>> componentTypes = splitOnComma(typeName, ind + 1, typeName.length() - 1).stream()
                    .map(ConfigType::create).collect(Collectors.toList());
            return create(clazz, componentTypes);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("All parameter types for config must be known at compiletime", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ConfigType<T> create(Class<?> clazz, List<ConfigType<?>> componentTypes) {
        return new ConfigType<>((Class<T>) clazz, componentTypes);
    }

    private static List<String> splitOnComma(String str, int start, int end) {
        int depth = 0;
        StringBuilder current = new StringBuilder();
        List<String> split = new ArrayList<>();
        for (int i = start; i < end; i++) {
            char c = str.charAt(i);
            switch (c) {
            case '<':
                depth++;
                break;
            case '>':
                depth--;
                break;
            case ',':
                if (depth != 0) {
                    break;
                }
                split.add(current.toString().trim());
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }
        String last = current.toString().trim();
        if (last.length() != 0) {
            split.add(last);
        }
        return split;
    }

    public ConfigType(Class<T> clazz, List<ConfigType<?>> componentTypes) {
        this.clazz = clazz;
        this.componentTypes = componentTypes;
    }

    public ConfigType(Class<T> clazz) {
        this(clazz, new ArrayList<>());
    }

    public Class<T> getType() {
        return clazz;
    }

    public List<ConfigType<?>> getComponentTypes() {
        return componentTypes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, componentTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConfigType)) {
            return false;
        }
        ConfigType<?> type = (ConfigType<?>) o;
        return type.clazz.equals(clazz) && type.componentTypes.equals(componentTypes);
    }

    @Override
    public String toString() {
        String str = clazz.getName();
        if (componentTypes.size() > 0) {
            str += "<" + componentTypes.stream().map(ConfigType::toString).collect(Collectors.joining(", ")) + ">";
        }
        return str;
    }
}
