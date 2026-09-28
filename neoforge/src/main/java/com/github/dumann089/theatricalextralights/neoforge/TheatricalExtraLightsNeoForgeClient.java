package com.github.dumann089.theatricalextralights.neoforge;

import com.github.dumann089.theatricalextralights.TheatricalExtraLights;
import com.github.dumann089.theatricalextralights.TheatricalExtraLightsClient;
import com.github.dumann089.theatricalextralights.client.ConfettiCannonClientSetup;
import com.github.dumann089.theatricalextralights.client.ConfettiCannonItemRenderer;
import com.github.dumann089.theatricalextralights.client.ExtraLightsSettingsAccess;
import com.github.dumann089.theatricalextralights.client.LensRenderTypes;
import com.github.dumann089.theatricalextralights.client.ModShaders;
import com.github.dumann089.theatricalextralights.client.entities.FireworkRocketRenderer;
import com.github.dumann089.theatricalextralights.client.firework.DetachedPyroSparks;
import com.github.dumann089.theatricalextralights.client.gui.ExtraLightsSettingsScreen;
import com.github.dumann089.theatricalextralights.client.neoforge.ModParticleClientImpl;
import com.github.dumann089.theatricalextralights.client.render.beam.raymarch.SceneDepthCopy;
import com.github.dumann089.theatricalextralights.config.TheatricalExtraLightsConfig;
import com.github.dumann089.theatricalextralights.entities.ModEntities;
import com.github.dumann089.theatricalextralights.items.Items;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import java.io.IOException;

/**
 * Client glue for NeoForge.
 *
 * <p>This class touches client-only events (for example {@link RegisterParticleProvidersEvent},
 * which references {@code ParticleEngine}), so it must NOT be loaded on a dedicated server:
 * otherwise the runtime dist cleaner would throw
 * "Attempted to load class ... for invalid dist DEDICATED_SERVER" and the mod would fail to
 * construct. It is only referenced from {@code TheatricalExtraLightsNeoForge} inside a dist check.
 */
public final class TheatricalExtraLightsNeoForgeClient {

    private TheatricalExtraLightsNeoForgeClient() {
    }

    public static void init(IEventBus modBus, ModContainer container) {
        modBus.addListener(TheatricalExtraLightsNeoForgeClient::clientSetup);
        modBus.addListener(TheatricalExtraLightsNeoForgeClient::registerShaders);
        modBus.addListener(TheatricalExtraLightsNeoForgeClient::registerClientExtensions);
        modBus.addListener(TheatricalExtraLightsNeoForgeClient::registerEntityRenderers);
        modBus.addListener(TheatricalExtraLightsNeoForgeClient::registerLayerDefinitions);
        modBus.addListener(TheatricalExtraLightsNeoForgeClient::registerParticleProviders);

        // RegisterClientCommandsEvent and RenderLevelStageEvent are posted on the game bus.
        NeoForge.EVENT_BUS.addListener(TheatricalExtraLightsNeoForgeClient::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(TheatricalExtraLightsNeoForgeClient::onRenderLevelStage);

        // "Config" button in the NeoForge mod list.
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> new ExtraLightsSettingsScreen(parent));
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        // General renderers are registered through Architectury / common.
        TheatricalExtraLightsClient.init();
    }

    private static void registerShaders(final RegisterShadersEvent event) {
        try {
            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            ResourceLocation.fromNamespaceAndPath(TheatricalExtraLights.MOD_ID, "gobo_projector"),
                            DefaultVertexFormat.POSITION_TEX_COLOR
                    ),
                    shader -> ModShaders.goboProjectorShader = shader
            );

            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            ResourceLocation.fromNamespaceAndPath(TheatricalExtraLights.MOD_ID, "volumetric_beam"),
                            DefaultVertexFormat.POSITION_TEX_COLOR
                    ),
                    shader -> ModShaders.volumetricBeamShader = shader
            );

            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            ResourceLocation.fromNamespaceAndPath(TheatricalExtraLights.MOD_ID, "beam_raymarch"),
                            DefaultVertexFormat.POSITION_TEX_COLOR
                    ),
                    shader -> ModShaders.beamRaymarchShader = shader
            );
        } catch (IOException e) {
            throw new RuntimeException("Error loading Theatrical Extra Lights shaders", e);
        }
    }

    private static void registerClientExtensions(final RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private ConfettiCannonItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    Minecraft minecraft = Minecraft.getInstance();
                    renderer = new ConfettiCannonItemRenderer(
                            minecraft.getBlockEntityRenderDispatcher(),
                            minecraft.getEntityModels()
                    );
                }
                return renderer;
            }
        }, Items.CONFETTI_CANNON.get());
    }

    private static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        // Registered explicitly here: Architectury's EntityRendererRegistry only records into a
        // static map that is flushed by its own RegisterRenderers listener, which is attached when
        // the class is first loaded (during FMLClientSetupEvent) - too late on NeoForge, so the
        // registration was lost and the firework rocket entity had no renderer (render-thread NPE).
        event.registerEntityRenderer(ModEntities.FIREWORK_ROCKET.get(), FireworkRocketRenderer::new);
    }

    private static void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        ConfettiCannonClientSetup.registerModelLayer(event::registerLayerDefinition);
    }

    private static void registerParticleProviders(final RegisterParticleProvidersEvent event) {
        ModParticleClientImpl.registerNeoForgeProviders(event);
    }

    private static void registerClientCommands(final RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("tel")
                        .then(Commands.literal("config").executes(ctx -> {
                            ExtraLightsSettingsAccess.openDeferred();
                            ctx.getSource().sendSuccess(() -> Component.translatable("tel.command.config.opened"), false);
                            return 1;
                        }))
        );
    }

    private static void onRenderLevelStage(final RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            if (!TheatricalExtraLightsConfig.isRaymarchEngine() || !ModShaders.canUseRaymarch()) {
                return;
            }
            minecraft.renderBuffers().bufferSource().endBatch();
            SceneDepthCopy.capture();
            return;
        }

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        DetachedPyroSparks.render(poseStack, buffers, event.getCamera(),
                event.getPartialTick().getGameTimeDeltaPartialTick(false));
        buffers.endBatch(LensRenderTypes.LENS);
    }
}
