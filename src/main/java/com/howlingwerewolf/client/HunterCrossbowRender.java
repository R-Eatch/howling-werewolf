package com.howlingwerewolf.client;

import com.howlingwerewolf.content.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

final class HunterCrossbowRender {
    static void registerProperties() {
        ItemStack vanillaCrossbow = new ItemStack(Items.CROSSBOW);
        for (String name : List.of("pull", "pulling", "charged", "firework")) {
            var key = ResourceLocation.withDefaultNamespace(name);
            var vanilla = ItemProperties.getProperty(vanillaCrossbow, key);
            if (vanilla != null) {
                ItemProperties.register(ModItems.HUNTER_CROSSBOW.get(), key, vanilla::call);
            }
        }
    }

    private HunterCrossbowRender() {}
}
