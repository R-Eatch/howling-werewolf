package com.howlingwerewolf.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

/** Preserves the flower offsets' discrete triangular distribution on 1.21.11. */
public final class TrapezoidIntProvider extends IntProvider {
    public static final MapCodec<TrapezoidIntProvider> CODEC = RecordCodecBuilder.<TrapezoidIntProvider>mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("min").forGetter(value -> value.min),
            Codec.INT.fieldOf("max").forGetter(value -> value.max),
            Codec.INT.fieldOf("plateau").forGetter(value -> value.plateau)
        ).apply(instance,TrapezoidIntProvider::new)).validate(value ->
            value.max >= value.min && (long)value.max-value.min < Integer.MAX_VALUE
                && value.plateau >= 0 && value.plateau <= (long)value.max-value.min
                ? DataResult.success(value) : DataResult.error(() -> "Invalid trapezoid bounds or plateau"));
    public static final IntProviderType<TrapezoidIntProvider> TYPE = Registry.register(
            BuiltInRegistries.INT_PROVIDER_TYPE,ModRegistry.id("trapezoid"),() -> CODEC);
    private final int min,max,plateau;
    private TrapezoidIntProvider(int min,int max,int plateau) { this.min=min;this.max=max;this.plateau=plateau; }
    @Override public int sample(RandomSource random) {
        int lower=(max-min-plateau)/2;
        int upper=max-min-lower;
        return min+random.nextInt(upper+1)+random.nextInt(lower+1);
    }
    @Override public int getMinValue() { return min; }
    @Override public int getMaxValue() { return max; }
    @Override public IntProviderType<?> getType() { return TYPE; }
    public static void initialize() { }
}
