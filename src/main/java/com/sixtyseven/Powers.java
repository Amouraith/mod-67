package com.sixtyseven;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Toda la logica de poderes: Modo 67, Armadura 67 y Modo Proyector. */
public final class Powers {
    // ===== Valores que podes cambiar =====
    private static final int MODE67_TICKS = 134;        // 6,7 s
    private static final int ARMOR_TICKS = 1340;        // 67 s
    private static final int PROJECTOR_TICKS = 160;     // 20 de llanto + 140 de rayos
    private static final int CRY_TICKS = 20;            // 1 s llorando
    private static final float YELLOW_BEAM_DAMAGE = 4f;
    private static final float RED_BEAM_DAMAGE = 100f;
    private static final double YELLOW_RANGE = 24;
    private static final double RED_RANGE = 30;
    // =====================================

    private static final ParticleEffect YELLOW = new DustParticleEffect(new Vector3f(1.0f, 0.85f, 0.0f), 1.0f);
    private static final ParticleEffect YELLOW_BIG = new DustParticleEffect(new Vector3f(1.0f, 0.85f, 0.0f), 2.5f);
    private static final ParticleEffect YELLOW_THIN = new DustParticleEffect(new Vector3f(1.0f, 0.85f, 0.0f), 0.6f);
    private static final ParticleEffect RED = new DustParticleEffect(new Vector3f(1.0f, 0.0f, 0.0f), 1.5f);
    private static final ParticleEffect RED_BIG = new DustParticleEffect(new Vector3f(1.0f, 0.0f, 0.0f), 3.0f);

    private static final String[] D6 = {"###", "#..", "###", "#.#", "###"};
    private static final String[] D7 = {"###", "..#", ".#.", ".#.", ".#."};

    private static final Map<UUID, PowerState> STATES = new HashMap<>();

    private Powers() {}

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                tickPlayer(p);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                STATES.remove(handler.getPlayer().getUuid()));
    }

    private static PowerState state(PlayerEntity p) {
        return STATES.computeIfAbsent(p.getUuid(), k -> new PowerState());
    }

    // ---------- Activaciones ----------

    public static void activateMode67(ServerPlayerEntity p) {
        ServerWorld w = (ServerWorld) p.getWorld();
        state(p).mode67 = MODE67_TICKS;

        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, MODE67_TICKS, 2));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, MODE67_TICKS, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, MODE67_TICKS, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, MODE67_TICKS, 0));

        w.spawnParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 67, 1.0, 1.0, 1.0, 0.15);
        w.spawnParticles(ParticleTypes.FIREWORK, p.getX(), p.getY() + 1.0, p.getZ(), 67, 0.5, 0.5, 0.5, 0.3);

        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(w);
        if (bolt != null) {
            bolt.refreshPositionAfterTeleport(p.getPos());
            bolt.setCosmetic(true); // solo visual
            w.spawnEntity(bolt);
        }
        w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 0.6f, 1.3f);

        bigText(w, p);
        p.sendMessage(Text.literal("¡MODO 67 ACTIVADO! Agachate (shift) para tirar rayos")
                .formatted(Formatting.GOLD, Formatting.BOLD), true);
    }

    public static void activateProjector(ServerPlayerEntity p) {
        ServerWorld w = (ServerWorld) p.getWorld();
        state(p).projector = PROJECTOR_TICKS;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, CRY_TICKS, 10, false, false));
        w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_WOLF_WHINE, SoundCategory.PLAYERS, 1.0f, 0.8f);
        p.sendMessage(Text.literal("Modo Proyector... :'(").formatted(Formatting.AQUA, Formatting.BOLD), true);
    }

    // ---------- Tick ----------

    private static void tickPlayer(ServerPlayerEntity p) {
        PowerState s = state(p);
        ServerWorld w = (ServerWorld) p.getWorld();

        boolean armorWorn = tickArmor(p, s, w);

        if (s.mode67 > 0) {
            s.mode67--;
            w.spawnParticles(YELLOW, p.getX(), p.getY() + 1.0, p.getZ(), 3, 0.4, 0.8, 0.4, 0);
            if (s.mode67 > 0 && s.mode67 % 20 == 0) {
                bigText(w, p);
            }
            if (s.mode67 == 0) {
                p.sendMessage(Text.literal("Modo 67 terminado").formatted(Formatting.GRAY), true);
            }
        }

        if (s.projector > 0) {
            s.projector--;
            if (s.projector >= PROJECTOR_TICKS - CRY_TICKS) {
                cry(w, p, s);
            } else {
                w.spawnParticles(RED, p.getX(), p.getY() + 1.0, p.getZ(), 3, 0.4, 0.8, 0.4, 0);
                if (s.projector == PROJECTOR_TICKS - CRY_TICKS - 1) {
                    p.sendMessage(Text.literal("¡Agachate (shift) para disparar los rayos!")
                            .formatted(Formatting.RED, Formatting.BOLD), true);
                }
                if (s.projector == 0) {
                    p.sendMessage(Text.literal("Modo Proyector terminado").formatted(Formatting.GRAY), true);
                }
            }
        }

        boolean redPhase = s.projector > 0 && s.projector < PROJECTOR_TICKS - CRY_TICKS;
        if (p.isSneaking()) {
            if (redPhase) {
                beam(w, p, true);
            } else if (s.projector == 0 && (s.mode67 > 0 || armorWorn)) {
                beam(w, p, false);
            }
        }
    }

    private static void cry(ServerWorld w, ServerPlayerEntity p, PowerState s) {
        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVec(1.0f);
        Vec3d right = rightOf(look);
        for (int side = -1; side <= 1; side += 2) {
            Vec3d pos = eye.add(look.multiply(0.3)).add(right.multiply(0.17 * side)).add(0, -0.05, 0);
            w.spawnParticles(ParticleTypes.FALLING_WATER, pos.x, pos.y, pos.z, 2, 0.03, 0.0, 0.03, 0);
        }
        if (s.projector == PROJECTOR_TICKS - 10) {
            w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_WOLF_WHINE, SoundCategory.PLAYERS, 1.0f, 0.8f);
        }
    }

    // ---------- Armadura 67 ----------

    private static boolean isArmor67(ItemStack stack) {
        return stack.getItem() instanceof Armor67Item;
    }

    private static boolean tickArmor(ServerPlayerEntity p, PowerState s, ServerWorld w) {
        boolean worn = isArmor67(p.getEquippedStack(EquipmentSlot.HEAD))
                || isArmor67(p.getEquippedStack(EquipmentSlot.CHEST))
                || isArmor67(p.getEquippedStack(EquipmentSlot.LEGS))
                || isArmor67(p.getEquippedStack(EquipmentSlot.FEET));
        if (!worn) {
            return false;
        }
        s.armorTicks++;
        int left = ARMOR_TICKS - s.armorTicks;

        if (s.armorTicks == 1) {
            p.sendMessage(Text.literal("¡Armadura 67 activada! Dura 67 segundos")
                    .formatted(Formatting.GOLD, Formatting.BOLD), true);
            w.playSound(null, p.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
        } else if (left > 0 && left % 20 == 0) {
            p.sendMessage(Text.literal("Armadura 67: " + (left / 20) + " s").formatted(Formatting.GOLD), true);
        }
        w.spawnParticles(YELLOW, p.getX(), p.getY() + 1.0, p.getZ(), 2, 0.3, 0.7, 0.3, 0);

        if (left <= 0) {
            PlayerInventory inv = p.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (isArmor67(inv.getStack(i))) {
                    inv.setStack(i, ItemStack.EMPTY);
                }
            }
            s.armorTicks = 0;
            p.sendMessage(Text.literal("La Armadura 67 se desintegró").formatted(Formatting.RED, Formatting.BOLD), true);
            w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 0.8f);
            w.spawnParticles(YELLOW_BIG, p.getX(), p.getY() + 1.0, p.getZ(), 60, 0.5, 1.0, 0.5, 0.1);
            return false;
        }
        return true;
    }

    // ---------- Rayos de los ojos ----------

    private static void beam(ServerWorld w, ServerPlayerEntity p, boolean red) {
        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVec(1.0f);
        double range = red ? RED_RANGE : YELLOW_RANGE;

        Vec3d end = eye.add(look.multiply(range));
        BlockHitResult hit = w.raycast(new RaycastContext(eye, end,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
        boolean hitBlock = hit.getType() == HitResult.Type.BLOCK;
        if (hitBlock) {
            end = hit.getPos();
        }

        Vec3d right = rightOf(look);
        Vec3d up = right.crossProduct(look);
        long time = w.getTime();

        if (red) {
            for (int side = -1; side <= 1; side += 2) {
                Vec3d start = eye.add(look.multiply(0.5)).add(right.multiply(0.15 * side)).add(0, -0.05, 0);
                Vec3d diff = end.subtract(start);
                double len = diff.length();
                if (len < 0.5) continue;
                Vec3d dir = diff.normalize();
                for (double d = 0; d <= len; d += 0.5) {
                    Vec3d pt = start.add(dir.multiply(d));
                    w.spawnParticles(RED, pt.x, pt.y, pt.z, 2, 0.08, 0.08, 0.08, 0);
                }
            }
            if (hitBlock) {
                w.spawnParticles(RED_BIG, end.x, end.y, end.z, 20, 0.4, 0.4, 0.4, 0);
            }
        } else if (time % 2 == 0) {
            Vec3d start = eye.add(look.multiply(0.6)).add(0, -0.1, 0);
            Vec3d diff = end.subtract(start);
            double len = diff.length();
            if (len >= 0.5) {
                Vec3d dir = diff.normalize();
                for (double d = 0; d <= len; d += 0.5) {
                    Vec3d pt = start.add(dir.multiply(d));
                    w.spawnParticles(YELLOW_THIN, pt.x, pt.y, pt.z, 1, 0.05, 0.05, 0.05, 0);
                }
                // Un monton de "67" a lo largo del rayo
                for (double d = 2.0; d <= len; d += 2.5) {
                    glyph(w, start.add(dir.multiply(d)), right.negate(), up, 0.15, YELLOW);
                }
            }
            if (hitBlock) {
                w.spawnParticles(ParticleTypes.LAVA, end.x, end.y, end.z, 3, 0.3, 0.3, 0.3, 0);
            }
        }

        // Danio a los mobs que cruza el rayo (no daña jugadores)
        float dmg = red ? RED_BEAM_DAMAGE : YELLOW_BEAM_DAMAGE;
        Box area = new Box(eye, end).expand(1.5);
        Vec3d finalEnd = end;
        for (Entity e : w.getOtherEntities(p, area,
                ent -> ent instanceof LivingEntity && !(ent instanceof PlayerEntity) && ent.isAlive())) {
            Box bb = e.getBoundingBox().expand(0.4);
            Optional<Vec3d> inter = bb.raycast(eye, finalEnd);
            if (inter.isPresent() || bb.contains(eye)) {
                e.damage(w.getDamageSources().indirectMagic(p, p), dmg);
            }
        }

        if (time % 4 == 0) {
            if (red) {
                w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.PLAYERS, 0.5f, 1.5f);
            } else {
                w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 0.5f, 1.8f);
            }
        }
    }

    // ---------- Dibujar "67" con particulas ----------

    private static void bigText(ServerWorld w, ServerPlayerEntity p) {
        Vec3d look = p.getRotationVec(1.0f);
        Vec3d f = new Vec3d(look.x, 0, look.z);
        f = f.lengthSquared() < 1.0E-4 ? new Vec3d(0, 0, 1) : f.normalize();
        Vec3d right = rightOf(f);
        Vec3d center = p.getPos().add(f.multiply(4.0)).add(0, 2.0, 0);
        glyph(w, center, right.negate(), new Vec3d(0, 1, 0), 0.35, YELLOW_BIG);
    }

    private static void glyph(ServerWorld w, Vec3d center, Vec3d left, Vec3d up, double s, ParticleEffect fx) {
        drawDigit(w, center, left, up, s, fx, D6, 0);
        drawDigit(w, center, left, up, s, fx, D7, 4);
    }

    private static void drawDigit(ServerWorld w, Vec3d center, Vec3d left, Vec3d up, double s,
                                  ParticleEffect fx, String[] rows, int offsetX) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 3; c++) {
                if (rows[r].charAt(c) == '#') {
                    int x = offsetX + c;
                    Vec3d pt = center.add(left.multiply((3 - x) * s)).add(up.multiply((2 - r) * s));
                    w.spawnParticles(fx, pt.x, pt.y, pt.z, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    private static Vec3d rightOf(Vec3d forward) {
        Vec3d r = forward.crossProduct(new Vec3d(0, 1, 0));
        return r.lengthSquared() < 1.0E-6 ? new Vec3d(1, 0, 0) : r.normalize();
    }
}
