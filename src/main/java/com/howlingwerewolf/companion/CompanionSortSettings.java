/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.companion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import java.util.List;

/** Explicit owner-selected storage; a double chest occupies one entry. */
public record CompanionSortSettings(Identifier dimension, BlockPos center, List<BlockPos> containers, boolean consolidate) {
    public static final int RADIUS = 8, HEIGHT = 4, LIMIT = 24;
    public static final CompanionSortSettings EMPTY = new CompanionSortSettings(Identifier.withDefaultNamespace("overworld"), BlockPos.ZERO, List.of(), true);
    public static final Codec<CompanionSortSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(CompanionSortSettings::dimension),
            BlockPos.CODEC.fieldOf("center").forGetter(CompanionSortSettings::center),
            BlockPos.CODEC.listOf().validate(list -> list.size() <= LIMIT
                    ? DataResult.success(list.stream().distinct().toList()) : DataResult.error(() -> "Too many sorting containers"))
                    .fieldOf("containers").forGetter(CompanionSortSettings::containers),
            Codec.BOOL.optionalFieldOf("consolidate", true).forGetter(CompanionSortSettings::consolidate)
    ).apply(instance, CompanionSortSettings::new));
    public CompanionSortSettings { center = center.immutable(); containers = List.copyOf(containers); }
    public boolean contains(BlockPos pos) {
        return Math.abs((long) pos.getX() - center.getX()) <= RADIUS
                && Math.abs((long) pos.getZ() - center.getZ()) <= RADIUS
                && Math.abs((long) pos.getY() - center.getY()) <= HEIGHT;
    }
}
