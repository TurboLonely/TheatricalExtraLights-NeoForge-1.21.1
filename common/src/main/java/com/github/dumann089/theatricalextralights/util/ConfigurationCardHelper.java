package com.github.dumann089.theatricalextralights.util;

import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.items.ConfigurationCardData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Adressage DMX avec bascule automatique vers l'univers suivant quand les canaux ne tiennent pas dans 512.
 */
public final class ConfigurationCardHelper {

    public static final int DMX_CHANNELS_PER_UNIVERSE = 512;

    private ConfigurationCardHelper() {
    }

    public record DmxPatch(int universe, int address) {
    }

    public record ApplyResult(
            int requestedUniverse,
            int requestedAddress,
            int appliedUniverse,
            int appliedAddress,
            int channelCount,
            boolean addressFromCard,
            boolean universeWrappedOnApply,
            boolean autoIncrement,
            int nextUniverse,
            int nextAddress,
            boolean nextAddressWrapped,
            ConfigurationCardData updatedData
    ) {
        public int channelEnd() {
            return appliedAddress + Math.max(channelCount, 1) - 1;
        }
    }

    public static boolean fitsInUniverse(int address, int channelCount) {
        if (channelCount <= 0) {
            return true;
        }
        return address > 0 && address + channelCount - 1 <= DMX_CHANNELS_PER_UNIVERSE;
    }

    public static DmxPatch resolvePatch(int universe, int address, int channelCount) {
        int safeUniverse = Math.max(0, universe);
        int safeAddress = Math.max(1, address);
        if (fitsInUniverse(safeAddress, channelCount)) {
            return new DmxPatch(safeUniverse, safeAddress);
        }
        return new DmxPatch(safeUniverse + 1, 1);
    }

    public static DmxPatch advancePatch(int universe, int address, int channelCount) {
        int nextAddress = address + channelCount;
        int nextUniverse = Math.max(0, universe);
        if (nextAddress > DMX_CHANNELS_PER_UNIVERSE) {
            nextUniverse++;
            nextAddress = 1;
        }
        return new DmxPatch(nextUniverse, nextAddress);
    }

    public static ApplyResult applyToFixture(ConfigurationCardData data, BaseDMXConsumerLightBlockEntity consumer) {
        boolean universeFromCard = data.universeEnabled();
        boolean addressFromCard = data.addressEnabled();
        int channelCount = consumer.getChannelCount();

        int requestedUniverse = universeFromCard ? data.dmxUniverse() : consumer.getUniverse();
        int requestedAddress = addressFromCard ? data.dmxAddress() : consumer.getChannelStart();
        int appliedUniverse = consumer.getUniverse();
        int appliedAddress = consumer.getChannelStart();
        boolean universeWrapped = false;

        ConfigurationCardData updatedData = data;

        if (addressFromCard) {
            DmxPatch patch = resolvePatch(requestedUniverse, requestedAddress, channelCount);
            appliedUniverse = patch.universe();
            appliedAddress = patch.address();
            universeWrapped = appliedUniverse != requestedUniverse || appliedAddress != requestedAddress;
            consumer.setUniverse(appliedUniverse);
            consumer.setChannelStartPoint(appliedAddress);
            updatedData = withPatch(updatedData, appliedUniverse, appliedAddress);
        } else if (universeFromCard) {
            appliedUniverse = requestedUniverse;
            consumer.setUniverse(appliedUniverse);
        }

        boolean autoIncrement = data.autoIncrement();
        int nextUniverse = updatedData.dmxUniverse();
        int nextAddress = updatedData.dmxAddress();
        boolean nextWrapped = false;

        if (autoIncrement) {
            DmxPatch before = new DmxPatch(nextUniverse, nextAddress);
            DmxPatch next = advancePatch(nextUniverse, nextAddress, channelCount);
            nextUniverse = next.universe();
            nextAddress = next.address();
            nextWrapped = next.universe() != before.universe() || next.address() != before.address();
            updatedData = withPatch(updatedData, nextUniverse, nextAddress);
        }

        return new ApplyResult(
                requestedUniverse,
                requestedAddress,
                appliedUniverse,
                appliedAddress,
                channelCount,
                addressFromCard,
                universeWrapped,
                autoIncrement,
                nextUniverse,
                nextAddress,
                nextWrapped,
                updatedData
        );
    }

    private static ConfigurationCardData withPatch(ConfigurationCardData data, int universe, int address) {
        return new ConfigurationCardData(data.network(), universe, address, data.autoIncrement(),
                data.universeEnabled(), data.addressEnabled());
    }

    public static void sendPatchMessages(Player player, Level level, BaseDMXConsumerLightBlockEntity consumer,
                                         ApplyResult result) {
        String networkName = TheatricalNetworkAccess.getNetworkName(level, consumer.getNetworkId());
        String fixtureName = Component.translatable(consumer.getTranslationKey()).getString();

        player.sendSystemMessage(Component.translatable(
                "item.configurationcard.patched",
                fixtureName,
                networkName,
                Integer.toString(result.appliedUniverse()),
                Integer.toString(result.appliedAddress()),
                Integer.toString(result.appliedAddress()),
                Integer.toString(result.channelEnd()),
                Integer.toString(result.channelCount())
        ));

        if (result.universeWrappedOnApply() && result.addressFromCard()) {
            player.sendSystemMessage(Component.translatable(
                    "item.configurationcard.universe_wrap",
                    Integer.toString(result.requestedUniverse()),
                    Integer.toString(result.requestedAddress()),
                    Integer.toString(result.appliedUniverse()),
                    Integer.toString(result.appliedAddress())
            ));
        }

        if (result.autoIncrement()) {
            player.sendSystemMessage(Component.translatable(
                    "item.configurationcard.next",
                    Integer.toString(result.nextUniverse()),
                    Integer.toString(result.nextAddress())
            ));
            if (result.nextAddressWrapped() && !result.universeWrappedOnApply()) {
                player.sendSystemMessage(Component.translatable(
                        "item.configurationcard.next_universe_wrap",
                        Integer.toString(result.appliedUniverse()),
                        Integer.toString(result.nextUniverse())
                ));
            }
        }
    }
}
