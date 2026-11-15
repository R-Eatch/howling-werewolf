package com.howlingwerewolf.client;

import com.howlingwerewolf.content.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;

final class HunterCrossbowRender {
    static void registerProperties() {
        for (String name : List.of("pull", "pulling", "charged", "firework")) {
            var key = new ResourceLocation(name);
            var vanilla = ItemProperties.getProperty(Items.CROSSBOW, key);
            if (vanilla != null) {
                ItemProperties.register(ModItems.HUNTER_CROSSBOW.get(), key, vanilla::call);
            }
        }
    }

    private HunterCrossbowRender() {}
}
