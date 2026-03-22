package ki.rage.feature.module.impl.combat;

import ki.rage.client.Client;
import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.PacketEvent;
import ki.rage.client.event.impl.TickEvent;
import ki.rage.client.util.math.TimeUtil;
import ki.rage.feature.module.api.Category;
import ki.rage.feature.module.api.Module;
import ki.rage.feature.module.api.setting.BooleanSetting;
import ki.rage.feature.module.api.setting.ModeSetting;
import ki.rage.feature.module.api.setting.SliderSetting;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Aura extends Module {

    // ============ RANGE SETTINGS ============
    private final SliderSetting attackRange = register(new SliderSetting("Range", 3.0, 1.0, 6.0, 0.1));
    private final SliderSetting wallRange = register(new SliderSetting("WallRange", 3.0, 0.0, 6.0, 0.1));
    private final SliderSetting fov = register(new SliderSetting("FOV", 180.0, 1.0, 180.0, 1.0));

    // ============ ROTATION SETTINGS ============
    private final ModeSetting rotationMode = register(new ModeSetting("Rotation", "Smooth",
            List.of("Smooth", "Instant", "None")));
    private final SliderSetting rotationSpeed = register(new SliderSetting("RotationSpeed", 80.0, 10.0, 180.0, 1.0));

    // ============ ATTACK SETTINGS ============
    private final ModeSetting switchMode = register(new ModeSetting("AutoWeapon", "None",
            List.of("None", "Normal", "Silent")));
    private final BooleanSetting onlyWeapon = register(new BooleanSetting("OnlyWeapon", false));
    private final SliderSetting attackCooldown = register(new SliderSetting("Cooldown", 0.9, 0.5, 1.0, 0.01));

    // ============ CRIT SETTINGS ============
    private final BooleanSetting smartCrit = register(new BooleanSetting("SmartCrit", true));
    private final BooleanSetting autoJump = register(new BooleanSetting("AutoJump", false));
    private final SliderSetting critFallDistance = register(new SliderSetting("CritFallDist", 0.1, 0.0, 1.0, 0.01));

    // ============ MISC SETTINGS ============
    private final BooleanSetting pauseWhileEating = register(new BooleanSetting("PauseEating", false));
    private final BooleanSetting dropSprint = register(new BooleanSetting("DropSprint", true));
    private final BooleanSetting shieldBreaker = register(new BooleanSetting("ShieldBreaker", true));
    private final BooleanSetting rayTrace = register(new BooleanSetting("RayTrace", true));

    // ============ SAFETY SETTINGS ============
    private final BooleanSetting disableOnDeath = register(new BooleanSetting("DisableOnDeath", true));
    private final BooleanSetting disableOnTP = register(new BooleanSetting("DisableOnTP", false));

    // ============ TARGET SETTINGS ============
    private final BooleanSetting targetPlayers = register(new BooleanSetting("Players", true));
    private final BooleanSetting targetHostiles = register(new BooleanSetting("Hostiles", true));
    private final BooleanSetting targetAnimals = register(new BooleanSetting("Animals", false));
    private final BooleanSetting targetMobs = register(new BooleanSetting("Mobs", false));
    private final BooleanSetting targetVillagers = register(new BooleanSetting("Villagers", false));
    private final BooleanSetting targetSlimes = register(new BooleanSetting("Slimes", false));
    private final BooleanSetting targetProjectiles = register(new BooleanSetting("Projectiles", false));

    // ============ FILTER SETTINGS ============
    private final BooleanSetting ignoreInvisible = register(new BooleanSetting("IgnoreInvisible", false));
    private final BooleanSetting ignoreNaked = register(new BooleanSetting("IgnoreNaked", false));
    private final BooleanSetting ignoreTeam = register(new BooleanSetting("IgnoreTeam", false));

    // ============ SORT SETTINGS ============
    private final ModeSetting sortMode = register(new ModeSetting("Sort", "Distance",
            List.of("Distance", "Health", "FOV", "Armor")));
    private final BooleanSetting lockTarget = register(new BooleanSetting("LockTarget", true));

    // ============ VARIABLES ============
    public static Entity target;

    private float rotationYaw;
    private float rotationPitch;

    private Vec3d rotationPoint = Vec3d.ZERO;
    private Vec3d rotationMotion = Vec3d.ZERO;

    private int hitTicks;
    private boolean lookingAtHitbox;

    private final TimeUtil pauseTimer = new TimeUtil();

    // ============ CONSTRUCTOR ============
    public Aura() {
        super("Aura", Category.Combat);
    }

    // ============ LIFECYCLE ============
    @Override
    protected void onEnable() {
        target = null;
        lookingAtHitbox = false;
        rotationPoint = Vec3d.ZERO;
        rotationMotion = Vec3d.ZERO;
        if (mc.player != null) {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
        }
    }

    @Override
    protected void onDisable() {
        target = null;
    }

    // ============ EVENTS ============
    @EventTarget
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (!pauseTimer.hasTimeElapsed(100)) return;
        if (mc.player.isUsingItem() && pauseWhileEating.enabled()) return;

        auraLogic();
        hitTicks--;
    }

    @EventTarget
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof EntityStatusS2CPacket status) {
            if (status.getStatus() == 30 && target != null) {
                Entity entity = status.getEntity(mc.world);
                if (entity != null && entity == target) {
                    sendMessage("Shield broken: " + target.getName().getString());
                }
            }
            if (status.getStatus() == 3 && status.getEntity(mc.world) == mc.player && disableOnDeath.enabled()) {
                setEnabled(false);
                sendMessage("Disabled due to death!");
            }
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && disableOnTP.enabled()) {
            setEnabled(false);
            sendMessage("Disabled due to teleport!");
        }
    }

    // ============ MAIN LOGIC ============
    private void auraLogic() {
        if (!haveWeapon()) {
            target = null;
            return;
        }

        updateTarget();

        if (target == null) {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
            return;
        }

        if (!mc.options.jumpKey.isPressed() && mc.player.isOnGround() && autoJump.enabled()) {
            mc.player.jump();
        }

        calcRotations();

        if (!rotationMode.value().equals("None")) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        }

        boolean readyForAttack = canAttack() && (lookingAtHitbox || !rayTrace.enabled() || rotationMode.value().equals("None"));

        if (readyForAttack) {
            if (shieldBreaker(target)) {
                return;
            }
            attack();
        }
    }

    // ============ WEAPON CHECK ============
    private boolean haveWeapon() {
        Item handItem = mc.player.getMainHandStack().getItem();
        if (onlyWeapon.enabled()) {
            if (switchMode.value().equals("None")) {
                return isWeapon(handItem);
            } else {
                return findWeaponSlot() != -1;
            }
        }
        return true;
    }

    private boolean isWeapon(Item item) {
        // Проверяем через Items напрямую или instanceof для AxeItem/MaceItem
        if (item instanceof AxeItem || item instanceof MaceItem) {
            return true;
        }
        // Проверка на меч через Items
        return item == Items.WOODEN_SWORD
                || item == Items.STONE_SWORD
                || item == Items.IRON_SWORD
                || item == Items.GOLDEN_SWORD
                || item == Items.DIAMOND_SWORD
                || item == Items.NETHERITE_SWORD;
    }

    private int findWeaponSlot() {
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (isWeapon(item)) {
                return i;
            }
        }
        return -1;
    }

    private int findAxeSlot() {
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (item instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    // ============ INVENTORY HELPERS ============
    private int getSelectedSlot() {
        // В 1.21.9 selectedSlot доступен напрямую или через getter
        return mc.player.getInventory().selectedSlot;
    }

    private void setSelectedSlot(int slot) {
        // Используем packet для смены слота (более надёжно для сервера)
        mc.player.getInventory().selectedSlot = slot;
        // Если нужно синхронизировать с сервером:
        // mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    // ============ POSITION HELPERS ============
    private Vec3d getEntityPos(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
    }

    private Vec3d getPlayerEyePos() {
        return new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
    }

    // ============ ATTACK ============
    private void attack() {
        int prevSlot = switchToWeapon();

        boolean wasSprinting = mc.player.isSprinting();
        if (wasSprinting && dropSprint.enabled()) {
            mc.player.setSprinting(false);
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (wasSprinting && dropSprint.enabled()) {
            mc.player.setSprinting(true);
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }

        hitTicks = 10;

        if (prevSlot != -1) {
            setSelectedSlot(prevSlot);
        }
    }

    private int switchToWeapon() {
        if (switchMode.value().equals("None")) return -1;

        int weaponSlot = findWeaponSlot();
        if (weaponSlot == -1) return -1;

        int prevSlot = -1;
        if (switchMode.value().equals("Silent")) {
            prevSlot = getSelectedSlot();
        }

        setSelectedSlot(weaponSlot);
        return prevSlot;
    }

    // ============ SHIELD BREAKER ============
    private boolean shieldBreaker(Entity target) {
        if (!shieldBreaker.enabled()) return false;
        if (!(target instanceof PlayerEntity player)) return false;
        if (!player.isBlocking()) return false;

        int axeSlot = findAxeSlot();
        if (axeSlot == -1) return false;

        int prevSlot = getSelectedSlot();
        setSelectedSlot(axeSlot);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        setSelectedSlot(prevSlot);

        hitTicks = 10;
        return true;
    }

    // ============ ATTACK COOLDOWN ============
    private boolean canAttack() {
        if (hitTicks > 0) return false;

        float cooldown = mc.player.getAttackCooldownProgress(0.5f);

        if (!smartCrit.enabled()) {
            return cooldown >= attackCooldown.get();
        }

        if (mc.player.getAbilities().flying || isPlayerGliding()) {
            return cooldown >= attackCooldown.get();
        }

        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            return cooldown >= attackCooldown.get();
        }

        if (mc.player.isInLava() || mc.player.isSubmergedInWater() || isAboveWater()) {
            return cooldown >= attackCooldown.get();
        }

        if (!mc.player.isOnGround() && mc.player.fallDistance > critFallDistance.get()) {
            return cooldown >= attackCooldown.get();
        }

        if (!autoJump.enabled() && !mc.options.jumpKey.isPressed()) {
            return cooldown >= attackCooldown.get();
        }

        return false;
    }

    /**
     * Проверка полёта на элитрах для 1.21.9
     * Используем проверку через экипировку и состояние игрока
     */
    private boolean isPlayerGliding() {
        // Проверяем наличие элитры в слоте нагрудника
        ItemStack chestArmor = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chestArmor.isOf(Items.ELYTRA)) {
            return false;
        }
        // Проверяем что игрок не на земле и падает (летит на элитрах)
        return !mc.player.isOnGround() && !mc.player.isClimbing() && mc.player.getVelocity().y < 0.5;
    }

    private boolean isAboveWater() {
        BlockPos below = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.4, mc.player.getZ());
        return mc.world.getBlockState(below).getBlock() == Blocks.WATER;
    }

    // ============ TARGET SELECTION ============
    private void updateTarget() {
        Entity newTarget = findTarget();

        if (target == null || !isValidTarget(target)) {
            target = newTarget;
        } else if (sortMode.value().equals("FOV") || !lockTarget.enabled()) {
            target = newTarget;
        }

        if (newTarget instanceof ShulkerBulletEntity || newTarget instanceof FireballEntity) {
            target = newTarget;
        }
    }

    private Entity findTarget() {
        List<LivingEntity> targets = new CopyOnWriteArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if ((entity instanceof ShulkerBulletEntity || entity instanceof FireballEntity)
                    && entity.isAlive() && isInRange(entity) && targetProjectiles.enabled()) {
                return entity;
            }

            if (!isValidTarget(entity)) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            targets.add(living);
        }

        if (targets.isEmpty()) return null;

        return switch (sortMode.value()) {
            case "Distance" -> targets.stream()
                    .min(Comparator.comparing(e -> mc.player.squaredDistanceTo(e)))
                    .orElse(null);
            case "Health" -> targets.stream()
                    .min(Comparator.comparing(e -> e.getHealth() + e.getAbsorptionAmount()))
                    .orElse(null);
            case "FOV" -> targets.stream()
                    .min(Comparator.comparing(this::getFovAngle))
                    .orElse(null);
            case "Armor" -> targets.stream()
                    .min(Comparator.comparing(this::getArmorValue))
                    .orElse(null);
            default -> targets.stream()
                    .min(Comparator.comparing(e -> mc.player.squaredDistanceTo(e)))
                    .orElse(null);
        };
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == null || !entity.isAlive()) return false;
        if (entity == mc.player) return false;
        if (!(entity instanceof LivingEntity living)) return false;
        if (living.isDead()) return false;
        if (entity instanceof ArmorStandEntity) return false;
        if (entity instanceof CatEntity) return false;

        if (!isInRange(entity)) return false;
        if (!isInFov(entity)) return false;

        if (entity instanceof PlayerEntity player) {
            if (!targetPlayers.enabled()) return false;
            if (player.isCreative()) return false;
            if (player.isInvisible() && ignoreInvisible.enabled()) return false;
            if (player.getArmor() == 0 && ignoreNaked.enabled()) return false;
            if (ignoreTeam.enabled() && isOnSameTeam(player)) return false;
            if (isFriend(player.getName().getString())) return false;
        } else if (entity instanceof HostileEntity) {
            if (!targetHostiles.enabled()) return false;
        } else if (entity instanceof SlimeEntity) {
            if (!targetSlimes.enabled()) return false;
        } else if (entity instanceof AnimalEntity) {
            if (!targetAnimals.enabled()) return false;
        } else if (entity instanceof VillagerEntity) {
            if (!targetVillagers.enabled()) return false;
        } else if (entity instanceof MobEntity) {
            if (!targetMobs.enabled()) return false;
        }

        return true;
    }

    private boolean isInRange(Entity entity) {
        double distance = mc.player.distanceTo(entity);
        boolean canSee = mc.player.canSee(entity);
        double maxRange = canSee ? attackRange.get() : wallRange.get();
        return distance <= maxRange;
    }

    private boolean isInFov(Entity entity) {
        if (fov.get() >= 180) return true;

        Vec3d playerLook = mc.player.getRotationVec(1.0f);
        Vec3d toEntity = getEntityPos(entity).subtract(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ())).normalize();

        double dot = playerLook.x * toEntity.x + playerLook.y * toEntity.y + playerLook.z * toEntity.z;
        double angle = Math.toDegrees(Math.acos(MathHelper.clamp(dot, -1.0, 1.0)));

        return angle <= fov.get();
    }

    private boolean isOnSameTeam(PlayerEntity player) {
        return mc.player.getTeamColorValue() == player.getTeamColorValue()
                && mc.player.getTeamColorValue() != 16777215;
    }

    private boolean isFriend(String name) {
        return Client.getInstance().getFriendManager().isFriend(name);
    }

    private float getFovAngle(LivingEntity entity) {
        double dx = entity.getX() - mc.player.getX();
        double dz = entity.getZ() - mc.player.getZ();
        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        return Math.abs(yaw - MathHelper.wrapDegrees(mc.player.getYaw()));
    }

    private float getArmorValue(LivingEntity entity) {
        float value = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack armor = entity.getEquippedStack(slot);
                if (armor != null && !armor.isEmpty()) {
                    int maxDamage = armor.getMaxDamage();
                    if (maxDamage > 0) {
                        value += (float) (maxDamage - armor.getDamage()) / maxDamage;
                    }
                }
            }
        }
        return value;
    }

    // ============ ROTATIONS ============
    private void calcRotations() {
        if (target == null) return;

        Vec3d targetPos = getLegitLookPos(target);
        Vec3d eyePos = getPlayerEyePos();

        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;

        double dist = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        if (rotationMode.value().equals("Instant")) {
            rotationYaw = targetYaw;
            rotationPitch = MathHelper.clamp(targetPitch, -90f, 90f);
        } else {
            float yawDiff = MathHelper.wrapDegrees(targetYaw - rotationYaw);
            float pitchDiff = targetPitch - rotationPitch;

            float maxStep = (float) rotationSpeed.get();

            yawDiff = MathHelper.clamp(yawDiff, -maxStep, maxStep);
            pitchDiff = MathHelper.clamp(pitchDiff, -maxStep / 2f, maxStep / 2f);

            rotationYaw += yawDiff;
            rotationPitch = MathHelper.clamp(rotationPitch + pitchDiff, -90f, 90f);
        }

        // GCD fix для обхода античита
        double gcdFix = Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0) * 1.2;
        rotationYaw = (float) (rotationYaw - (rotationYaw - mc.player.getYaw()) % gcdFix);
        rotationPitch = (float) (rotationPitch - (rotationPitch - mc.player.getPitch()) % gcdFix);

        lookingAtHitbox = isLookingAtTarget();
    }

    /**
     * "DVD Logo" система - точка прицеливания плавно двигается внутри хитбокса
     * Делает ротации более легитными
     */
    private Vec3d getLegitLookPos(Entity target) {
        float minMotion = 0.003f;
        float maxMotion = 0.03f;

        double lengthX = target.getBoundingBox().getLengthX();
        double lengthY = target.getBoundingBox().getLengthY();
        double lengthZ = target.getBoundingBox().getLengthZ();

        if (rotationMotion.equals(Vec3d.ZERO)) {
            rotationMotion = new Vec3d(
                    random(-0.05f, 0.05f),
                    random(-0.05f, 0.05f),
                    random(-0.05f, 0.05f)
            );
        }

        rotationPoint = rotationPoint.add(rotationMotion);

        // Отскок от стенок хитбокса
        if (rotationPoint.x >= (lengthX - 0.05) / 2f)
            rotationMotion = new Vec3d(-random(minMotion, maxMotion), rotationMotion.getY(), rotationMotion.getZ());
        if (rotationPoint.x <= -(lengthX - 0.05) / 2f)
            rotationMotion = new Vec3d(random(minMotion, maxMotion), rotationMotion.getY(), rotationMotion.getZ());

        if (rotationPoint.y >= lengthY)
            rotationMotion = new Vec3d(rotationMotion.getX(), -random(minMotion, maxMotion), rotationMotion.getZ());
        if (rotationPoint.y <= 0.05)
            rotationMotion = new Vec3d(rotationMotion.getX(), random(minMotion, maxMotion), rotationMotion.getZ());

        if (rotationPoint.z >= (lengthZ - 0.05) / 2f)
            rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), -random(minMotion, maxMotion));
        if (rotationPoint.z <= -(lengthZ - 0.05) / 2f)
            rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), random(minMotion, maxMotion));

        // Если не смотрим на цель - целимся в центр
        if (!lookingAtHitbox) {
            rotationPoint = new Vec3d(0, lengthY / 2.0, 0);
        }

        return getEntityPos(target).add(rotationPoint);
    }

    private boolean isLookingAtTarget() {
        if (target == null) return false;

        Vec3d start = getPlayerEyePos();
        Vec3d direction = getRotationVector(rotationPitch, rotationYaw);
        Vec3d end = start.add(direction.multiply(attackRange.get() + 1));

        Box box = target.getBoundingBox().expand(0.1);
        return box.raycast(start, end).isPresent();
    }

    private Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    // ============ UTILITY ============
    public void pause() {
        pauseTimer.reset();
    }

    private static float random(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, max);
    }

    private void sendMessage(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.of("§7[§cAura§7] §f" + message), false);
        }
    }

    // ============ PUBLIC GETTERS ============
    public static Entity getTarget() {
        return target;
    }

    public float getRotationYaw() {
        return rotationYaw;
    }

    public float getRotationPitch() {
        return rotationPitch;
    }
}