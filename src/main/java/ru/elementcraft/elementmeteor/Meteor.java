package ru.elementcraft.elementmeteor;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;
import java.util.Random;
import org.bukkit.plugin.Plugin;

public class Meteor extends FireAbility implements AddonAbility {
    private static final long COOLDOWN = 2000;
    private static final double SPEED = 1.2;
    private static final int MAX_TICKS = 40;
    private static final double HITBOX = 1.2;
    private static final int BASE_EXPLOSION_RADIUS = 2;
    private static final double DAMAGE = 8.0;
    private static final double KNOCKBACK = 1.5;
    private static final Material[] PARTICLE_MATERIALS = {Material.DIRT, Material.STONE, Material.GRAVEL};
    private Listener listener;
    private Location currentLoc;
    private Vector direction;
    private int ticks = 0;
    private boolean exploded = false;
    private final Random random = new Random();
    private int explosionRadius = BASE_EXPLOSION_RADIUS;

    public Meteor(Player player) {
        super(player);
        if (bPlayer.isOnCooldown(this)) {
            return;
        }
        bPlayer.addCooldown(this);
        currentLoc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.5));
        direction = player.getLocation().getDirection().normalize().multiply(SPEED);
        // Получаем usageRepository из главного плагина
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ElementMeteor");
        MeteorUsageRepository usageRepository = null;
        if (plugin instanceof ElementMeteor em) {
            usageRepository = em.getUsageRepository();
        }
        if (usageRepository != null) {
            usageRepository.incrementUsages(player.getUniqueId()).thenAccept(usages -> {
                explosionRadius = calculateExplosionRadius(usages);
                Bukkit.getScheduler().runTask(ProjectKorra.plugin, this::start); // Запуск строго после получения радиуса
            });
        } else {
            start();
        }
    }

    /**
     * Формула расчёта радиуса взрыва по количеству использований.
     */
    private int calculateExplosionRadius(int usages) {
        if (usages < 5) {
            return usages * BASE_EXPLOSION_RADIUS;
        } else {
            return 5 * BASE_EXPLOSION_RADIUS;
        }
    }

    @Override
    public void progress() {
        if (!player.isOnline() || player.isDead() || exploded) {
            remove();
            return;
        }
        // Проверка столкновения с блоком
        if (currentLoc.getBlock().getType().isSolid()) {
            explode(currentLoc);
            remove();
            return;
        }
        // Проверка столкновения с сущностями
        for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, HITBOX, HITBOX, HITBOX)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                explode(currentLoc);
                remove();
                return;
            }
        }
        // Движение вперёд
        currentLoc.add(direction);
        spawnMeteorParticles();
        // Таймаут способности
        if (ticks++ > MAX_TICKS) {
            remove();
        }
    }

    @Override
    public long getCooldown() {
        return COOLDOWN;
    }

    @Override
    public String getName() {
        return "Meteor";
    }

    @Override
    public Location getLocation() {
        return currentLoc != null ? currentLoc : player.getLocation();
    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public boolean isExplosiveAbility() {
        return true;
    }

    @Override
    public boolean isIgniteAbility() {
        return false;
    }

    @Override
    public String getAuthor() {
        return "PiatraAlatyr";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public void load() {
        listener = new ElementMeteorListener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(this.listener, ProjectKorra.plugin);
        if (Bukkit.getPluginManager().getPermission("bending.ability.meteor") == null) {
            Permission perm = new Permission("bending.ability.meteor");
            perm.setDefault(PermissionDefault.TRUE);
            ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
        }
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(listener);
        listener = null;
    }

    /**
     * Взрыв метеора, частицы, временное уничтожение блоков, урон и подбрасывание.
     */
    private void explode(Location loc) {
        if (exploded) return;
        exploded = true;
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 3);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
        spawnExplosionParticles(loc);
        entityExplosionDamage(loc);
        createTempCrater(loc);
    }

    private void createTempCrater(Location loc) {
        Material[] protectedBlocks = {Material.CHEST, Material.ENDER_CHEST, Material.BARREL, Material.SHULKER_BOX, Material.ANVIL, Material.ENCHANTING_TABLE, Material.BEACON};
        for (int x = -explosionRadius; x <= explosionRadius; x++)
            for (int y = -explosionRadius; y <= explosionRadius; y++)
                for (int z = -explosionRadius; z <= explosionRadius; z++) {
                    Location blockLoc = loc.clone().add(x, y, z);
                    Material type = blockLoc.getBlock().getType();
                    if (blockLoc.distance(loc) <= explosionRadius && type != Material.BEDROCK && !isProtectedBlock(type, protectedBlocks))
                        new TempBlock(blockLoc.getBlock(), Material.AIR.createBlockData(), 1000);
                }
    }

    private boolean isProtectedBlock(Material type, Material[] protectedBlocks) {
        for (Material mat : protectedBlocks) {
            if (type == mat) return true;
        }
        return false;
    }

    private void entityExplosionDamage(Location loc) {
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, explosionRadius, explosionRadius, explosionRadius)) {
            if (entity instanceof LivingEntity livingEntity && !entity.equals(player)) {
                livingEntity.damage(DAMAGE, player);
                entity.setVelocity(new Vector(0, KNOCKBACK, 0));
            }
        }
    }

    private void spawnExplosionParticles(Location loc) {
        int count = 40 + random.nextInt(30); // 40-70 частиц
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double pitch = random.nextDouble() * Math.PI - (Math.PI / 2);
            double speed = random.nextDouble() * explosionRadius + 2;
            double x = Math.cos(angle) * Math.cos(pitch) * speed;
            double y = Math.sin(pitch) * speed;
            double z = Math.sin(angle) * Math.cos(pitch) * speed;
            Material mat = PARTICLE_MATERIALS[random.nextInt(PARTICLE_MATERIALS.length)];
            int particleAmount = 3 + random.nextInt(5); // 3-7 частиц за раз
            loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, particleAmount, x, y, z, 1, mat.createBlockData());
        }
    }

    private void spawnMeteorParticles() {
        currentLoc.getWorld().spawnParticle(Particle.FLAME, currentLoc, 8, 0.2, 0.2, 0.2, 0.01);
        currentLoc.getWorld().spawnParticle(Particle.LAVA, currentLoc, 2, 0.1, 0.1, 0.1, 0.01);
        currentLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, currentLoc, 2, 0.2, 0.2, 0.2, 0.01);
    }
}