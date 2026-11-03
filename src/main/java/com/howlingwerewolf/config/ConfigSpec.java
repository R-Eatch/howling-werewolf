package com.howlingwerewolf.config;
import com.howlingwerewolf.HowlingWerewolf;
import net.fabricmc.loader.api.FabricLoader;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;

/** Validated, server-authoritative configuration, stored as UTF-8 JSON. */
public final class ConfigSpec {
    private final Map<String, ConfigValue<?>> values;
    private ConfigSpec(Map<String, ConfigValue<?>> values) { this.values = Map.copyOf(values); }
    public void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("howlingwerewolf-common.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonObject input = Files.exists(path) ? JsonParser.parseString(Files.readString(path)).getAsJsonObject() : new JsonObject();
            JsonObject output = new JsonObject();
            values.forEach((key,value) -> {
                JsonElement raw = input.get(key);
                if (raw != null) value.read(raw);
                output.add(key, gson.toJsonTree(value.get()));
            });
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(output) + "\n");
        } catch (Exception error) {
            HowlingWerewolf.LOGGER.error("Unable to load {}. Using validated defaults.", path, error);
        }
    }
    public static class ConfigValue<T> {
        private T value;
        private final Predicate<Object> validator;
        ConfigValue(T value, Predicate<Object> validator) { this.value=value; this.validator=validator; }
        public T get() { return value; }
        @SuppressWarnings("unchecked") void read(JsonElement input) {
            try {
                Object parsed = value instanceof Integer ? (Object)input.getAsInt() : value instanceof Double ? (Object)input.getAsDouble()
                    : value instanceof Boolean ? (Object)input.getAsBoolean() : input.getAsString();
                if (validator.test(parsed)) value=(T)parsed;
            } catch (RuntimeException ignored) { /* Keep the validated default for this entry. */ }
        }
    }
    public static final class DoubleValue extends ConfigValue<Double> {
        DoubleValue(double value,double min,double max) { super(value,v -> v instanceof Double n && Double.isFinite(n) && n>=min && n<=max); }
    }
    public static final class IntValue extends ConfigValue<Integer> {
        IntValue(int value,int min,int max) { super(value,v -> v instanceof Integer n && n>=min && n<=max); }
    }
    public static final class BooleanValue extends ConfigValue<Boolean> { BooleanValue(boolean value) { super(value,v -> v instanceof Boolean); } }
    public static final class Builder {
        private final Map<String,ConfigValue<?>> values=new LinkedHashMap<>();
        private String section="";
        public Builder push(String section) { this.section=section+"."; return this; }
        public Builder pop() { section=""; return this; }
        public Builder comment(String... comments) { return this; }
        public Builder worldRestart() { return this; }
        private <T extends ConfigValue<?>> T add(String name,T value) { values.put(section+name,value); return value; }
        public DoubleValue defineInRange(String name,double value,double min,double max) { return add(name,new DoubleValue(value,min,max)); }
        public IntValue defineInRange(String name,int value,int min,int max) { return add(name,new IntValue(value,min,max)); }
        public BooleanValue define(String name,boolean value) { return add(name,new BooleanValue(value)); }
        public ConfigValue<String> define(String name,String value,Predicate<Object> validator) { return add(name,new ConfigValue<>(value,validator)); }
        public ConfigSpec build() { return new ConfigSpec(values); }
    }
}
