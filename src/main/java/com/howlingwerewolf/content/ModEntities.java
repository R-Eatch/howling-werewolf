package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.HunterEntity;
import com.howlingwerewolf.entity.AlphaWerewolfEntity;
import com.howlingwerewolf.entity.AlphaMinionEntity;
import com.howlingwerewolf.entity.WerewolfEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HowlingWerewolf.MOD_ID);

    public static final RegistryObject<EntityType<HunterEntity>> HUNTER = ENTITIES.register("hunter",
            () -> EntityType.Builder.of(HunterEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("hunter"));
    public static final RegistryObject<EntityType<WerewolfEntity>> WEREWOLF = ENTITIES.register("werewolf",
            () -> EntityType.Builder.of(WerewolfEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.0F)
                    .clientTrackingRange(10)
                    .build("werewolf"));
    public static final RegistryObject<EntityType<AlphaWerewolfEntity>> ALPHA_WEREWOLF =
            ENTITIES.register("alpha_werewolf",
                    () -> EntityType.Builder.of(AlphaWerewolfEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 3.0F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .build("alpha_werewolf"));
    public static final RegistryObject<EntityType<AlphaMinionEntity>> ALPHA_MINION =
            ENTITIES.register("alpha_minion",
                    () -> EntityType.Builder.of(AlphaMinionEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 0.85F)
                            .clientTrackingRange(10)
                            .build("alpha_minion"));

    private ModEntities() {}
}
