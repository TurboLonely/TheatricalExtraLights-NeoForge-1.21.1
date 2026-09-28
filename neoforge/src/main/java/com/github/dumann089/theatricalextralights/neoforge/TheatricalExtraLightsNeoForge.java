package com.github.dumann089.theatricalextralights.neoforge;

import com.github.dumann089.theatricalextralights.TheatricalExtraLights;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(TheatricalExtraLights.MOD_ID)
public class TheatricalExtraLightsNeoForge {

    public TheatricalExtraLightsNeoForge(IEventBus modBus, ModContainer container) {
        TheatricalExtraLights.init();
        if (FMLEnvironment.dist.isClient()) {
            TheatricalExtraLightsNeoForgeClient.init(modBus, container);
        }
    }
}
