package com.seleris.selarium.client.sigil;

import com.seleris.selarium.Selarium;
import com.seleris.selarium.blockentity.ArcaneSigilBlockEntity;
import com.seleris.selarium.dust.DustUtil;
import com.seleris.selarium.network.PreviewSigilPacket;
import com.seleris.selarium.network.SelariumNetwork;
import com.seleris.selarium.ward.WardDefinition;
import com.seleris.selarium.ward.WardDefinitions;
import com.seleris.selarium.ward.WardType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Selarium.MOD_ID, value = Dist.CLIENT)
public final class ClientSigilSelection {
    private static Level previewLevel;
    private static BlockPos previewPos;
    private static WardType previewType = WardType.NONE;
    private static long expiresAt;

    private ClientSigilSelection() { }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null || minecraft.level == null
                || !minecraft.player.isShiftKeyDown() || DustUtil.getDefinition(minecraft.player.getMainHandItem()).isPresent()
                || !(minecraft.hitResult instanceof BlockHitResult hit)) return;
        if (!(minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof ArcaneSigilBlockEntity sigil)) return;
        if (!minecraft.player.getUUID().equals(sigil.getOwner()) || sigil.isActive()) return;

        event.setCanceled(true);
        List<WardDefinition> candidates = WardDefinitions.all().stream()
                .filter(definition -> definition.requirements().stream().allMatch(requirement -> requirement.matches(sigil)))
                .toList();
        if (candidates.isEmpty()) {
            minecraft.player.displayClientMessage(Component.translatable("message.selarium.sigil.preview_none"), true);
            return;
        }

        WardType current = previewLevel == minecraft.level && hit.getBlockPos().equals(previewPos)
                && minecraft.level.getGameTime() <= expiresAt ? previewType : sigil.getWardType();
        int index = -1;
        for (int i = 0; i < candidates.size(); i++) {
            if (candidates.get(i).type() == current) { index = i; break; }
        }
        int direction = event.getScrollDelta() < 0 ? 1 : -1;
        int steps = Math.max(1, (int) Math.round(Math.abs(event.getScrollDelta())));
        index = index < 0 ? (direction > 0 ? 0 : candidates.size() - 1)
                : Math.floorMod(index + direction * steps, candidates.size());

        previewLevel = minecraft.level;
        previewPos = hit.getBlockPos().immutable();
        previewType = candidates.get(index).type();
        expiresAt = minecraft.level.getGameTime() + 600;
        SelariumNetwork.CHANNEL.sendToServer(new PreviewSigilPacket(previewPos, previewType));
        minecraft.player.displayClientMessage(Component.translatable("message.selarium.sigil.preview",
                Component.translatable(previewType.getTranslationKey()), index + 1, candidates.size()), true);
    }

    public static WardType previewFor(ArcaneSigilBlockEntity sigil) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != previewLevel || minecraft.player == null || sigil.isActive()
                || previewPos == null || !previewPos.equals(sigil.getBlockPos())
                || minecraft.level.getGameTime() > expiresAt
                || !(minecraft.hitResult instanceof BlockHitResult hit) || !hit.getBlockPos().equals(previewPos)
                || !minecraft.player.getUUID().equals(sigil.getOwner())) return WardType.NONE;
        WardDefinition definition = WardDefinitions.get(previewType).orElse(null);
        return definition != null && definition.requirements().stream().allMatch(requirement -> requirement.matches(sigil))
                ? previewType : WardType.NONE;
    }
}
