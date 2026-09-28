package com.github.dumann089.theatricalextralights.blocks;

import com.github.dumann089.theatricalextralights.TheatricalExtraLightsScreens;
import com.github.dumann089.theatricalextralights.blockentities.ExtraLightsLightBlockEntity;
import com.github.dumann089.theatricalextralights.net.OpenExtraLightsScreenPacket;
import com.github.dumann089.theatricalextralights.util.ConfigurationCardHelper;
import com.github.dumann089.theatricalextralights.util.TheatricalNetworkAccess;
import com.mojang.serialization.MapCodec;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.blocks.light.BaseLightBlock;
import dev.imabad.theatrical.items.ConfigurationCardData;
import dev.imabad.theatrical.items.DataComponents;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base des fixtures Extra Lights — configuration card avec wrap automatique d'univers DMX.
 */
public abstract class ExtraLightsLightBlock extends BaseLightBlock {

    private static final Map<Class<?>, MapCodec<? extends HorizontalDirectionalBlock>> CODECS =
            new ConcurrentHashMap<>();

    protected ExtraLightsLightBlock(Properties properties) {
        super(properties, null);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODECS.computeIfAbsent(getClass(), ExtraLightsLightBlock::createCodec);
    }

    private static MapCodec<? extends HorizontalDirectionalBlock> createCodec(Class<?> blockClass) {
        return simpleCodec(properties -> {
            try {
                Constructor<?> constructor = blockClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (HorizontalDirectionalBlock) constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to create codec for " + blockClass.getName(), e);
            }
        });
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!level.isClientSide() && be instanceof ExtraLightsLightBlockEntity) {
            if (be instanceof BaseDMXConsumerLightBlockEntity consumerLightBlockEntity) {
                if (!consumerLightBlockEntity.getNetworkId().equals(UUIDUtil.NULL)
                        && !TheatricalNetworkAccess.canPlayerConfigure(player, level, consumerLightBlockEntity.getNetworkId())) {
                    return ItemInteractionResult.FAIL;
                }
            }

            if (player.getItemInHand(hand).getItem()
                    == com.github.dumann089.theatricalextralights.items.Items.FIXTURE_WRENCH.get()) {
                new OpenExtraLightsScreenPacket(pos, TheatricalExtraLightsScreens.MOUNT_WRENCH)
                        .sendTo((ServerPlayer) player);
                return ItemInteractionResult.SUCCESS;
            }

            if (be instanceof BaseDMXConsumerLightBlockEntity consumerLightBlockEntity) {
                if (player.getItemInHand(hand).getItem() == dev.imabad.theatrical.items.Items.CONFIGURATION_CARD.get()) {
                    ItemStack itemInHand = player.getItemInHand(hand);
                    if (itemInHand.has(DataComponents.CONFIGURATION_CARD_DATA.get())) {
                        ConfigurationCardData data = itemInHand.get(DataComponents.CONFIGURATION_CARD_DATA.get());
                        consumerLightBlockEntity.setNetworkId(data.network());
                        ConfigurationCardHelper.ApplyResult result =
                                ConfigurationCardHelper.applyToFixture(data, consumerLightBlockEntity);
                        itemInHand.set(DataComponents.CONFIGURATION_CARD_DATA.get(), result.updatedData());
                        ConfigurationCardHelper.sendPatchMessages(player, level, consumerLightBlockEntity, result);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
}
