// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.shared;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.shared.command.CommandComputerCraft;
import dan200.computercraft.shared.network.client.UpgradesLoadedMessage;
import dan200.computercraft.shared.network.server.ServerNetworking;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Forge-specific dispatch for {@link CommonHooks}.
 */
@Mod.EventBusSubscriber(modid = ComputerCraftAPI.MOD_ID)
public class ForgeCommonHooks {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        switch (event.phase) {
            case START -> CommonHooks.onServerTickStart(event.getServer());
            case END -> CommonHooks.onServerTickEnd();
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        CommonHooks.onServerStarting(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CommonHooks.onServerStopped();
    }

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        CommandComputerCraft.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Sent event) {
        CommonHooks.onChunkWatch(event.getChunk(), event.getPlayer());
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        CommonHooks.onDatapackReload((id, listener) -> event.addListener(listener));
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        var packet = new UpgradesLoadedMessage();
        if (event.getPlayer() == null) {
            ServerNetworking.sendToAllPlayers(packet, event.getPlayerList().getServer());
        } else {
            ServerNetworking.sendToPlayer(packet, event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void lootLoad(LootTableLoadEvent event) {
        // TODO: Remove this when https://github.com/neoforged/NeoForge/issues/474 is resolved.
        if (event.getName() == null) return;

        var pool = CommonHooks.getExtraLootPool(event.getName());
        if (pool != null) event.getTable().addPool(pool.build());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        if (CommonHooks.onEntitySpawn(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDrops(LivingDropsEvent event) {
        event.getDrops().removeIf(itemEntity -> CommonHooks.onLivingDrop(event.getEntity(), itemEntity.getItem()));
    }
}
