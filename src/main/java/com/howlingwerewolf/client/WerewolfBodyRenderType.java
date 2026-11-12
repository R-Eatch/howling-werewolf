package com.howlingwerewolf.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Vanilla's body render type, chosen in the werewolf player renderers themselves. Player-model mods such as MCA
 * blank LivingEntityRenderer.getRenderType for players who use their model, which would hide the transformed body.
 */
final class WerewolfBodyRenderType {
    static @Nullable RenderType of(EntityModel<?> model, Identifier texture, boolean visible, boolean translucent, boolean glowing) {
        if (translucent) return RenderTypes.entityTranslucentCullItemTarget(texture);
        if (visible) return model.renderType(texture);
        return glowing ? RenderTypes.outline(texture) : null;
    }

    private WerewolfBodyRenderType() {}
}
