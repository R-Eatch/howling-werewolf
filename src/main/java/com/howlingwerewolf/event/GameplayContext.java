package com.howlingwerewolf.event;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

/** Mutable decisions shared by Fabric callbacks and the narrow vanilla hooks. */
public final class GameplayContext {
    public static class EntityContext<T extends Entity> {
        private final T entity;
        private boolean canceled;
        public EntityContext(T entity) { this.entity=entity; }
        public T getEntity() { return entity; }
        public boolean isCanceled() { return canceled; }
        public void setCanceled(boolean value) { canceled=value; }
    }
    public static final class Tick extends EntityContext<Player> { public Tick(Player player) { super(player); } }
    public static final class Damage extends EntityContext<LivingEntity> {
        private final DamageSource source;
        private float amount;
        private float originalDamage;
        public Damage(LivingEntity entity,DamageSource source,float amount) { super(entity);this.source=source;this.amount=amount;this.originalDamage=amount; }
        public DamageSource getSource() { return source; }
        public float getAmount() { return amount; }
        public Damage(LivingEntity entity,DamageSource source,float amount,float original) { this(entity,source,amount);this.originalDamage=original; }
        public float getOriginalDamage() { return originalDamage; }
        public float getHealthDamage() { return amount; }
        public float getNewDamage() { return amount; }
        public void setAmount(float value) { amount=value; }
    }
    public static final class Attack extends EntityContext<Player> { public Attack(Player player) { super(player); } }
    public static final class Food extends EntityContext<LivingEntity> {
        private final ItemStack item;
        public Food(LivingEntity entity,ItemStack item) { super(entity);this.item=item; }
        public ItemStack getItem() { return item; }
    }
    public static final class Interaction extends EntityContext<Player> {
        private final BlockPos pos;
        public Interaction(Player player,BlockPos pos) { super(player);this.pos=pos; }
        public BlockPos getPos() { return pos; }
    }
    public static final class Fall extends EntityContext<LivingEntity> { public Fall(LivingEntity entity) { super(entity); } }
    public static final class Jump extends EntityContext<LivingEntity> { public Jump(LivingEntity entity) { super(entity); } }
    public static final class BreakSpeed extends EntityContext<Player> {
        private final BlockState state;
        private final float original;
        private float speed;
        public BreakSpeed(Player player,BlockState state,float original) { super(player);this.state=state;this.original=original;speed=original; }
        public BlockState getState() { return state; }
        public float getOriginalSpeed() { return original; }
        public float getNewSpeed() { return speed; }
        public void setNewSpeed(float value) { speed=value; }
    }
    public static final class Harvest extends EntityContext<Player> {
        private final BlockState block;
        private boolean harvest;
        public Harvest(Player player,BlockState block,boolean harvest) { super(player);this.block=block;this.harvest=harvest; }
        public BlockState getTargetBlock() { return block; }
        public boolean canHarvest() { return harvest; }
        public void setCanHarvest(boolean value) { harvest=value; }
    }
    public static final class Size extends EntityContext<Entity> {
        private final Pose pose;
        private EntityDimensions size;
        public Size(Entity entity,Pose pose,EntityDimensions size) { super(entity);this.pose=pose;this.size=size; }
        public Pose getPose() { return pose; }
        public EntityDimensions getNewSize() { return size; }
        public void setNewSize(EntityDimensions value) { size=value; }
    }
    public static final class Pickup {
        private final Player player;
        private final ItemEntity item;
        private boolean allowed=true;
        public Pickup(Player player,ItemEntity item) { this.player=player;this.item=item; }
        public Player getPlayer() { return player; }
        public ItemEntity getItemEntity() { return item; }
        public void denyPickup() { allowed=false; }
        public boolean allowed() { return allowed; }
    }
}
