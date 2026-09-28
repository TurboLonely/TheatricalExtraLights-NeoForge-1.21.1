package com.github.dumann089.theatricalextralights.compat.neoforge;

import com.github.dumann089.theatricalextralights.compat.FireworkLightUpdateQueue;
import dev.imabad.theatrical.api.DynamicLightProvider;
import net.minecraft.core.BlockPos;

/**
 * NeoForge implementation of {@code FireworkLightCompat}.
 *
 * <p>The original Forge/Fabric versions delegated to Shimmer's coloured point lights. Shimmer does
 * not exist for 1.21.1 / NeoForge, so this is an explicit no-op: fireworks are lit by Theatrical's
 * own dynamic light system instead. The pending-update queue is still drained so it cannot grow
 * without bound.
 */
@SuppressWarnings("unused")
public final class FireworkLightCompatImpl {

    private FireworkLightCompatImpl() {
    }

    public static void sync(DynamicLightProvider provider) {
        // No Shimmer on NeoForge 1.21.1.
    }

    public static void remove(DynamicLightProvider provider) {
        // No Shimmer on NeoForge 1.21.1.
    }

    public static void removeAt(BlockPos pos) {
        // No Shimmer on NeoForge 1.21.1.
    }

    public static void flushPending() {
        FireworkLightUpdateQueue.drain(update -> {
        });
    }
}
