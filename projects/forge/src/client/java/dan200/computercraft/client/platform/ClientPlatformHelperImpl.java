// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client.platform;

import com.google.auto.service.AutoService;
import com.mojang.blaze3d.vertex.PoseStack;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import dan200.computercraft.client.model.FoiledModel;
import dan200.computercraft.client.render.ModelRenderer;
import dan200.computercraft.shared.network.NetworkMessage;
import dan200.computercraft.shared.network.server.ServerNetworkContext;
import dan200.computercraft.shared.platform.NetworkHandler;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@AutoService(dan200.computercraft.impl.client.ClientPlatformHelper.class)
public class ClientPlatformHelperImpl implements ClientPlatformHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ClientPlatformHelperImpl.class);

    private static final RandomSource random = RandomSource.create(0);
    private static final Direction[] directions = Arrays.copyOf(Direction.values(), 7);

    private boolean reportedMissing = false;

    @Override
    public BakedModel getModel(ModelManager manager, ResourceLocation location) {
        var model = manager.getModel(location);
        if (model == null) {
            // This should never happen, but it does anyway! See https://github.com/cc-tweaked/CC-Tweaked/issues/1626
            reportMissingModel(manager, location);
            model = manager.getMissingModel();
        }

        return model;
    }

    private void reportMissingModel(ModelManager manager, ResourceLocation location) {
        if (reportedMissing) return;
        reportedMissing = true;

        LOG.error(
            """
                ModelManager.getModel("{}") returned null. This should never happen, and indicates the model registry is corrupt.

                ModelManager has been transformed by the following transformers: {}.
                The following models are null: {}.""",
            location,
            Optional.ofNullable(Launcher.INSTANCE)
                .flatMap(env -> env.environment().getProperty(IEnvironment.Keys.AUDITTRAIL.get()))
                .map(x -> x.getAuditString(ModelManager.class.getName()))
                .orElse("unknown"),
            manager.bakedRegistry.entrySet().stream()
                .filter(x -> x.getValue() == null)
                .map(x -> x.getKey().toString())
                .collect(Collectors.joining(", "))
        );
    }

    @Override
    public BakedModel createdFoiledModel(BakedModel model) {
        return new FoiledModel(model);
    }

    @Override
    public void sendToServer(NetworkMessage<ServerNetworkContext> message) {
        NetworkHandler.sendToServer(message);
    }

    @Override
    public void renderBakedModel(PoseStack transform, MultiBufferSource buffers, BakedModel model, int lightmapCoord, int overlayLight, @Nullable int[] tints) {
        for (var renderType : model.getRenderTypes(ItemStack.EMPTY, true)) {
            var buffer = buffers.getBuffer(renderType);
            for (var face : directions) {
                random.setSeed(42);
                var quads = model.getQuads(null, face, random, ModelData.EMPTY, renderType);
                ModelRenderer.renderQuads(transform, buffer, quads, lightmapCoord, overlayLight, tints);
            }
        }
    }
}
