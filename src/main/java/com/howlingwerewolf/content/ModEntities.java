package com.howlingwerewolf.content;

import com.howlingwerewolf.HowlingWerewolf;
import com.howlingwerewolf.entity.HunterEntity;
import com.howlingwerewolf.entity.AlphaWerewolfEntity;
import com.howlingwerewolf.entity.AlphaMinionEntity;
import com.howlingwerewolf.entity.WerewolfEntity;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import com.howlingwerewolf.entity.TribeWerewolfEntity;
import com.howlingwerewolf.entity.SettlementLeaderEntity;
import com.howlingwerewolf.entity.SquadHealingPotion;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HowlingWerewolf.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<HunterEntity>> HUNTER = ENTITIES.register("hunter",
            () -> EntityType.Builder.of(HunterEntity::new, MobCategory.MONSTER)
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

    public static final DeferredHolder<EntityType<?>, EntityType<WerewolfCompanionEntity>> WEREWOLF_COMPANION =
            ENTITIES.register("werewolf_companion", () -> EntityType.Builder.of(WerewolfCompanionEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 2.0F).clientTrackingRange(10)
                    .build("werewolf_companion"));

    public static final DeferredHolder<EntityType<?>, EntityType<SettlementLeaderEntity>> HUNTER_CAPTAIN =
            ENTITIES.register("hunter_captain", () -> EntityType.Builder.of(SettlementLeaderEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build("hunter_captain"));
    public static final DeferredHolder<EntityType<?>, EntityType<TribeWerewolfEntity>> TRIBE_WEREWOLF =
            ENTITIES.register("tribe_werewolf", () -> EntityType.Builder.of(TribeWerewolfEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 2.0F).clientTrackingRange(10).build("tribe_werewolf"));
    public static final DeferredHolder<EntityType<?>, EntityType<SettlementLeaderEntity>> TRIBE_CHIEF =
            ENTITIES.register("tribe_chief", () -> EntityType.Builder.of(SettlementLeaderEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 2.0F).clientTrackingRange(10).build("tribe_chief"));
    public static final DeferredHolder<EntityType<?>, EntityType<SquadHealingPotion>> SQUAD_HEALING_POTION =
            ENTITIES.register("squad_healing_potion", () -> EntityType.Builder.<SquadHealingPotion>of(SquadHealingPotion::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build("squad_healing_potion"));
    private ModEntities() {}
}
