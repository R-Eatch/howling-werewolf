package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.HunterEntity;
import com.howlingwerewolf.entity.AlphaWerewolfEntity;
import com.howlingwerewolf.entity.AlphaMinionEntity;
import com.howlingwerewolf.entity.WerewolfEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HowlingWerewolf.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<HunterEntity>> HUNTER = ENTITIES.register("hunter",
            () -> EntityType.Builder.of(HunterEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("hunter"));
    public static final DeferredHolder<EntityType<?>, EntityType<WerewolfEntity>> WEREWOLF = ENTITIES.register("werewolf",
            () -> EntityType.Builder.of(WerewolfEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.0F)
                    .clientTrackingRange(10)
                    .build("werewolf"));
    public static final DeferredHolder<EntityType<?>, EntityType<AlphaWerewolfEntity>> ALPHA_WEREWOLF =
            ENTITIES.register("alpha_werewolf",
                    () -> EntityType.Builder.of(AlphaWerewolfEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 3.0F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .build("alpha_werewolf"));
    public static final DeferredHolder<EntityType<?>, EntityType<AlphaMinionEntity>> ALPHA_MINION =
            ENTITIES.register("alpha_minion",
                    () -> EntityType.Builder.of(AlphaMinionEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 0.85F)
                            .clientTrackingRange(10)
                            .build("alpha_minion"));

    private ModEntities() {}
}
