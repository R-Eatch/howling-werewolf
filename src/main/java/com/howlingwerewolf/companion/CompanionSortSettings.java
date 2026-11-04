/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import java.util.List;

/**
 * The anchor where sorting began. Storage is discovered around it each pass;
 * {@code containers} only keeps pre-1.2.7 records loadable and is cleared on the next start.
 */
public record CompanionSortSettings(Identifier dimension, BlockPos center, List<BlockPos> containers, boolean consolidate) {
    public static final int RADIUS = 16, LEGACY_LIMIT = 24;
    public static final CompanionSortSettings EMPTY = new CompanionSortSettings(Identifier.withDefaultNamespace("overworld"), BlockPos.ZERO, List.of(), true);
    public static final Codec<CompanionSortSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(CompanionSortSettings::dimension),
            BlockPos.CODEC.fieldOf("center").forGetter(CompanionSortSettings::center),
            BlockPos.CODEC.listOf().validate(list -> list.size() <= LEGACY_LIMIT
                    ? DataResult.success(list.stream().distinct().toList()) : DataResult.error(() -> "Too many sorting containers"))
                    .fieldOf("containers").forGetter(CompanionSortSettings::containers),
            Codec.BOOL.optionalFieldOf("consolidate", true).forGetter(CompanionSortSettings::consolidate)
    ).apply(instance, CompanionSortSettings::new));
    public CompanionSortSettings { center = center.immutable(); containers = List.copyOf(containers); }
    /** A sphere around the anchor, so the area stays put while the companion walks between containers. */
    public boolean contains(BlockPos pos) {
        long x = (long) pos.getX() - center.getX(), y = (long) pos.getY() - center.getY(), z = (long) pos.getZ() - center.getZ();
        return x * x + y * y + z * z <= (long) RADIUS * RADIUS;
    }
    public CompanionSortSettings anchoredAt(Identifier newDimension, BlockPos newCenter) {
        return new CompanionSortSettings(newDimension, newCenter, List.of(), consolidate);
    }
    public CompanionSortSettings withConsolidate(boolean value) {
        return new CompanionSortSettings(dimension, center, containers, value);
    }
}
