package com.howlingwerewolf.mixin;

import com.howlingwerewolf.world.VillageWatchtower;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LocateCommand.class)
abstract class LocateWatchtowerMixin {
    @ModifyVariable(method = "locateStructure", at = @At("STORE"), ordinal = 0)
    private static Pair<BlockPos, Holder<Structure>> howling$showTowerPosition(Pair<BlockPos, Holder<Structure>> found,
                                                                             CommandSourceStack source) {
        if (found == null || !found.getSecond().is(VillageWatchtower.KEY)) return found;
        var origin = found.getFirst();
        var chunk = source.getLevel().getChunk(origin.getX() >> 4, origin.getZ() >> 4, ChunkStatus.STRUCTURE_STARTS);
        var start = chunk.getAllStarts().get(found.getSecond().value());
        if (start == null || !start.isValid()) return found;
        return Pair.of(start.getPieces().getFirst().getBoundingBox().getCenter(), found.getSecond());
    }
}
