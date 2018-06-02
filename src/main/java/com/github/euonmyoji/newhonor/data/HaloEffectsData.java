package com.github.euonmyoji.newhonor.data;

import com.flowpowered.math.vector.Vector3d;
import com.github.euonmyoji.newhonor.NewHonor;
import com.github.euonmyoji.newhonor.util.Util;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class HaloEffectsData {
    private final EffectsDelayData delayData;
    private final List<PotionEffect> potionEffects;
    private final double chance;
    private final double radius;
    private final boolean include;
    public LocalDateTime lastRunTime = LocalDateTime.MIN;
    public int lastDelay = 0;

    public HaloEffectsData(CommentedConfigurationNode cfg) throws ObjectMappingException {
        delayData = new EffectsDelayData(cfg.getNode("delay").getString("15"));
        radius = cfg.getNode("radius").getDouble(5);
        potionEffects = Util.getPotionEffects(cfg, Util.getPotionEffectsDurationTick(cfg));
        chance = cfg.getNode("chance").getDouble(1);
        include = cfg.getNode("include-me").getBoolean(false);
    }

    public void execute(List<UUID> list) {
        lastRunTime = LocalDateTime.now();
        lastDelay = delayData.getDelay();
        if (Math.random() < chance) {
            Task.builder().execute(() -> Util.getStream(list)
                    .map(Sponge.getServer()::getPlayer)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(this::offer)).submit(NewHonor.plugin);
        }
    }

    private void offer(Player p) {
        final Vector3d o = p.getLocation().getPosition();
        p.getWorld().getPlayers().forEach(player -> {
            double distanceSquared = player.getLocation().getPosition().distanceSquared(o);
            boolean playerPass = include || !(p.getUniqueId().equals(player.getUniqueId()));
            if (playerPass && distanceSquared < (radius * radius)) {
                Util.offerEffects(player, potionEffects);
            }
        });
    }
}