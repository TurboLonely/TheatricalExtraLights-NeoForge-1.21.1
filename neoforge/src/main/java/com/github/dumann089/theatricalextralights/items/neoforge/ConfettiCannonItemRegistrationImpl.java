package com.github.dumann089.theatricalextralights.items.neoforge;

import com.github.dumann089.theatricalextralights.items.ConfettiCannonItem;
import net.minecraft.world.item.Item;

/**
 * NeoForge implementation of {@code ConfettiCannonItemRegistration#create()}.
 *
 * <p>The item's renderer is supplied through {@code RegisterClientExtensionsEvent} instead of a
 * Forge-style item subclass, so the plain item is enough here.
 */
@SuppressWarnings("unused")
public final class ConfettiCannonItemRegistrationImpl {

    private ConfettiCannonItemRegistrationImpl() {
    }

    public static Item create() {
        return ConfettiCannonItem.createDefault();
    }
}
