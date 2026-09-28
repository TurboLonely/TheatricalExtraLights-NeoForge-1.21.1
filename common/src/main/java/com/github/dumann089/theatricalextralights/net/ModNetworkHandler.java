package com.github.dumann089.theatricalextralights.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.GameInstance;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModNetworkHandler {

    public static final Channel CHANNEL = new Channel(
            ResourceLocation.fromNamespaceAndPath("theatricalextralights", "main")
    );

    public static void register() {
        // Client -> server control packets.
        CHANNEL.registerC2S(SetJetHeightPacket.class, SetJetHeightPacket::encode, SetJetHeightPacket::decode, SetJetHeightPacket::handle);
        CHANNEL.registerC2S(SetJetThicknessPacket.class, SetJetThicknessPacket::encode, SetJetThicknessPacket::decode, SetJetThicknessPacket::handle);
        CHANNEL.registerC2S(SetJetConeAnglePacket.class, SetJetConeAnglePacket::encode, SetJetConeAnglePacket::decode, SetJetConeAnglePacket::handle);
        CHANNEL.registerC2S(SetFixturePositionPacket.class, SetFixturePositionPacket::encode, SetFixturePositionPacket::decode, SetFixturePositionPacket::handle);
        CHANNEL.registerC2S(SetPersonalityPacket.class, SetPersonalityPacket::encode, SetPersonalityPacket::decode, SetPersonalityPacket::handle);
        CHANNEL.registerC2S(SetLaserSafetyPacket.class, SetLaserSafetyPacket::encode, SetLaserSafetyPacket::decode, SetLaserSafetyPacket::handle);
        CHANNEL.registerC2S(SetFixtureArmedPacket.class, SetFixtureArmedPacket::encode, SetFixtureArmedPacket::decode, SetFixtureArmedPacket::handle);
        CHANNEL.registerC2S(FollowspotPresetPacket.class, FollowspotPresetPacket::encode, FollowspotPresetPacket::decode, FollowspotPresetPacket::handle);
        CHANNEL.registerC2S(FollowspotConsolePatchPacket.class, FollowspotConsolePatchPacket::encode, FollowspotConsolePatchPacket::decode, FollowspotConsolePatchPacket::handle);
        CHANNEL.registerC2S(FollowspotConsoleControlPacket.class, FollowspotConsoleControlPacket::encode, FollowspotConsoleControlPacket::decode, FollowspotConsoleControlPacket::handle);
        CHANNEL.registerC2S(FollowspotEnterControlPacket.class, FollowspotEnterControlPacket::encode, FollowspotEnterControlPacket::decode, FollowspotEnterControlPacket::handle);
        CHANNEL.registerC2S(FollowspotExitControlPacket.class, FollowspotExitControlPacket::encode, FollowspotExitControlPacket::decode, FollowspotExitControlPacket::handle);
        CHANNEL.registerC2S(SetMountTransformPacket.class, SetMountTransformPacket::encode, SetMountTransformPacket::decode, SetMountTransformPacket::handle);
        CHANNEL.registerC2S(SetLedFacadeConfigPacket.class, SetLedFacadeConfigPacket::encode, SetLedFacadeConfigPacket::decode, SetLedFacadeConfigPacket::handle);
        CHANNEL.registerC2S(SetLedFacadePixelsPacket.class, SetLedFacadePixelsPacket::encode, SetLedFacadePixelsPacket::decode, SetLedFacadePixelsPacket::handle);

        // Server -> client effect packets.
        CHANNEL.registerS2C(ConfettiBurstPacket.class, ConfettiBurstPacket::encode, ConfettiBurstPacket::decode, ConfettiBurstPacket::handle);
    }

    /**
     * Minimal replacement for architectury's {@code NetworkChannel}.
     *
     * <p>{@code NetworkChannel#register} sends both a {@code playToServer} and a {@code playToClient}
     * registration for the very same payload id. NeoForge's {@code NetworkRegistry} keys payloads by
     * {@code type.id()} only and ignores the packet flow, so the second registration always fails with
     * "Cannot register payload ... as it is already registered." and the game refuses to start.
     * Registering every packet for exactly one direction avoids the collision while keeping the same
     * sending helpers.
     */
    public static final class Channel {

        private final ResourceLocation namespace;
        private final Map<Class<?>, MessageInfo<?>> messages = new HashMap<>();

        private Channel(ResourceLocation namespace) {
            this.namespace = namespace;
        }

        public <T> void registerC2S(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder,
                                    Function<FriendlyByteBuf, T> decoder,
                                    BiConsumer<T, Supplier<NetworkManager.PacketContext>> handler) {
            MessageInfo<T> info = register(type, encoder, decoder, handler);
            NetworkManager.registerReceiver(NetworkManager.Side.C2S, info.packetId, info::receive);
        }

        public <T> void registerS2C(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder,
                                    Function<FriendlyByteBuf, T> decoder,
                                    BiConsumer<T, Supplier<NetworkManager.PacketContext>> handler) {
            MessageInfo<T> info = register(type, encoder, decoder, handler);
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, info.packetId, info::receive);
        }

        public <T> void sendToServer(T message) {
            var client = GameInstance.getClient();
            if (client == null || client.getConnection() == null) {
                throw new IllegalStateException("Unable to send packet to the server while not in game!");
            }

            MessageInfo<T> info = infoFor(message);
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(
                    Unpooled.buffer(), client.getConnection().registryAccess());
            info.encoder.accept(message, buf);
            NetworkManager.sendToServer(info.packetId, buf);
        }

        public <T> void sendToPlayer(ServerPlayer player, T message) {
            MessageInfo<T> info = infoFor(message);
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
            info.encoder.accept(message, buf);
            NetworkManager.sendToPlayer(player, info.packetId, buf);
        }

        private <T> MessageInfo<T> register(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder,
                                            Function<FriendlyByteBuf, T> decoder,
                                            BiConsumer<T, Supplier<NetworkManager.PacketContext>> handler) {
            MessageInfo<T> info = new MessageInfo<>(packetId(type), encoder, decoder, handler);
            messages.put(type, info);
            return info;
        }

        @SuppressWarnings("unchecked")
        private <T> MessageInfo<T> infoFor(T message) {
            MessageInfo<T> info = (MessageInfo<T>) messages.get(message.getClass());
            if (info == null) {
                throw new IllegalArgumentException("Unregistered message type " + message.getClass().getName());
            }
            return info;
        }

        /**
         * Keeps the exact same payload ids that architectury's {@code NetworkChannel} produced, so the
         * wire protocol stays unchanged.
         */
        private ResourceLocation packetId(Class<?> type) {
            String hash = UUID.nameUUIDFromBytes(type.getName().getBytes(StandardCharsets.UTF_8))
                    .toString().replace("-", "");
            return ResourceLocation.parse(namespace + "/" + hash);
        }
    }

    private static final class MessageInfo<T> {

        private final ResourceLocation packetId;
        private final BiConsumer<T, FriendlyByteBuf> encoder;
        private final Function<FriendlyByteBuf, T> decoder;
        private final BiConsumer<T, Supplier<NetworkManager.PacketContext>> handler;

        private MessageInfo(ResourceLocation packetId, BiConsumer<T, FriendlyByteBuf> encoder,
                            Function<FriendlyByteBuf, T> decoder,
                            BiConsumer<T, Supplier<NetworkManager.PacketContext>> handler) {
            this.packetId = packetId;
            this.encoder = encoder;
            this.decoder = decoder;
            this.handler = handler;
        }

        private void receive(RegistryFriendlyByteBuf buf, NetworkManager.PacketContext context) {
            handler.accept(decoder.apply(buf), () -> context);
        }
    }
}
