package com.howlingwerewolf.capability;
import com.howlingwerewolf.content.ModRegistry;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
public final class ModAttachments {
    public static final AttachmentType<WerewolfData> WEREWOLF = AttachmentRegistry.create(ModRegistry.id("werewolf"), builder -> builder
        .initializer(WerewolfData::new)
        .persistent(CompoundTag.CODEC.xmap(tag -> { var data=new WerewolfData(); data.deserializeNBT(tag); return data; }, WerewolfData::serializeNBT))
        .copyOnDeath());
    public static final AttachmentType<CompoundTag> LEGACY = AttachmentRegistry.create(ModRegistry.id("legacy_player_data"), builder -> builder
        .initializer(CompoundTag::new).persistent(CompoundTag.CODEC).copyOnDeath());
    public static void initialize() { }
    private ModAttachments() { }
}
