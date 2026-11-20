package com.howlingwerewolf.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import com.howlingwerewolf.entity.HunterEntity;
import com.howlingwerewolf.entity.SettlementLeaderEntity;
import com.howlingwerewolf.entity.TribeWerewolfEntity;
import com.howlingwerewolf.entity.SquadHealingPotion;
import com.howlingwerewolf.entity.AlphaWerewolfEntity;
import com.howlingwerewolf.entity.AlphaMinionEntity;
import com.howlingwerewolf.entity.WerewolfEntity;
import com.howlingwerewolf.entity.WerewolfCompanionEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final ModRegistry<EntityType<?>> ENTITIES =
            new ModRegistry<>(BuiltInRegistries.ENTITY_TYPE);

    public static final Supplier<EntityType<HunterEntity>> HUNTER = ENTITIES.register("hunter",
            () -> EntityType.Builder.of(HunterEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build(key("hunter")));
    public static final Supplier<EntityType<WerewolfEntity>> WEREWOLF = ENTITIES.register("werewolf",
            () -> EntityType.Builder.of(WerewolfEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.0F)
                    .clientTrackingRange(10)
                    .build(key("werewolf")));
    public static final Supplier<EntityType<AlphaWerewolfEntity>> ALPHA_WEREWOLF =
            ENTITIES.register("alpha_werewolf",
                    () -> EntityType.Builder.of(AlphaWerewolfEntity::new, MobCategory.MONSTER)
                            .sized(1.0F, 3.0F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .build(key("alpha_werewolf")));
    public static final Supplier<EntityType<AlphaMinionEntity>> ALPHA_MINION =
            ENTITIES.register("alpha_minion",
                    () -> EntityType.Builder.of(AlphaMinionEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 0.85F)
                            .clientTrackingRange(10)
                            .build(key("alpha_minion")));

    public static final Supplier<EntityType<WerewolfCompanionEntity>> WEREWOLF_COMPANION =
            ENTITIES.register("werewolf_companion", () -> EntityType.Builder.of(WerewolfCompanionEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 2.0F).clientTrackingRange(10)
                    .build(key("werewolf_companion")));
    public static final Supplier<EntityType<SettlementLeaderEntity>> HUNTER_CAPTAIN =
            ENTITIES.register("hunter_captain", () -> EntityType.Builder.of(SettlementLeaderEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("hunter_captain")));
    public static final Supplier<EntityType<TribeWerewolfEntity>> TRIBE_WEREWOLF =
            ENTITIES.register("tribe_werewolf", () -> EntityType.Builder.of(TribeWerewolfEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 2.0F).clientTrackingRange(10).build(key("tribe_werewolf")));
    public static final Supplier<EntityType<SettlementLeaderEntity>> TRIBE_CHIEF =
            ENTITIES.register("tribe_chief", () -> EntityType.Builder.of(SettlementLeaderEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 2.0F).clientTrackingRange(10).build(key("tribe_chief")));
    public static final Supplier<EntityType<SquadHealingPotion>> SQUAD_HEALING_POTION =
            ENTITIES.register("squad_healing_potion", () -> EntityType.Builder.<SquadHealingPotion>of(SquadHealingPotion::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build(key("squad_healing_potion")));
    private static ResourceKey<EntityType<?>> key(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE, ModRegistry.id(path));
    }

    private ModEntities() {}
}
