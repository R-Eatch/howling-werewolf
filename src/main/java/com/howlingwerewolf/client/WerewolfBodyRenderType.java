package com.howlingwerewolf.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Vanilla's body render type, chosen in the werewolf player renderers themselves. Player-model mods such as MCA
 * blank LivingEntityRenderer.getRenderType for players who use their model, which would hide the transformed body.
 */
final class WerewolfBodyRenderType {
    static @Nullable RenderType of(EntityModel<?> model, ResourceLocation texture, boolean visible, boolean translucent, boolean glowing) {
        if (translucent) return RenderType.itemEntityTranslucentCull(texture);
        if (visible) return model.renderType(texture);
        return glowing ? RenderType.outline(texture) : null;
    }

    private WerewolfBodyRenderType() {}
}
