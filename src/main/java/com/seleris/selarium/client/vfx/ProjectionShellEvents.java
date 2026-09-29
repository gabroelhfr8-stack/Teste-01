package com.seleris.selarium.client.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.seleris.selarium.Selarium;
import com.seleris.selarium.network.ProjectionSyncPacket;
import com.seleris.selarium.ward.ProjectionDisplayCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Scroll fields have no block entity, so their shells are drawn from the display snapshots the server
 * sends every second. Mobile fields follow the caster's interpolated position instead of the snapshot.
 */
@Mod.EventBusSubscriber(modid = Selarium.MOD_ID, value = Dist.CLIENT)
public final class ProjectionShellEvents {
    private static final float FADE_TICKS = 40.0F;

    private ProjectionShellEvents() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        List<ProjectionSyncPacket.View> views = ProjectionDisplayCache.views(minecraft.level);
        if (views.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        float partialTick = event.getPartialTick();
        float time = minecraft.level.getGameTime() + partialTick;

        for (ProjectionSyncPacket.View view : views) {
            double cx = view.pos().getX() + 0.5D;
            double cy = view.pos().getY() + 0.5D;
            double cz = view.pos().getZ() + 0.5D;
            if (view.mobile()) {
                Player owner = minecraft.level.getPlayerByUUID(view.owner());
                if (owner != null) {
                    Vec3 position = owner.getPosition(partialTick);
                    cx = position.x;
                    cy = position.y + 0.5D;
                    cz = position.z;
                }
            }
            float strength = Mth.clamp(view.remainingTicks() / FADE_TICKS, 0.0F, 1.0F);
            poseStack.pushPose();
            poseStack.translate(cx - 0.5D - camera.x, cy - 0.5D - camera.y, cz - 0.5D - camera.z);
            Vec3 viewer = camera.subtract(cx, cy, cz);
            WardShellRenderer.draw(poseStack, buffers, view.type(), view.range(), strength, time, viewer, view.pos().asLong());
            poseStack.popPose();
        }
        buffers.endBatch();
    }
}
