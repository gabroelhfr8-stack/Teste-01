package com.seleris.selarium.client.vfx;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Render types used by Selarium's block-entity and world-space effects.
 * The class extends {@link RenderType} only to reach its protected render-state constants.
 */
public final class SelariumRenderTypes extends RenderType {
    private SelariumRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    /**
     * Additive, fullbright, double-sided, no depth writes: for rings, beams and other light-emitting layers.
     * Uses {@code LIGHTNING_TRANSPARENCY} (SRC_ALPHA, ONE): the vanilla {@code ADDITIVE_TRANSPARENCY} is (ONE, ONE),
     * which ignores the texture's alpha and would draw every glow texture as a solid quad.
     */
    private static final Function<ResourceLocation, RenderType> ADDITIVE = Util.memoize(texture -> create(
            "selarium_additive", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_EYES_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)));

    public static RenderType additive(ResourceLocation texture) {
        return ADDITIVE.apply(texture);
    }

    /** Alpha-blended, fullbright, double-sided, no depth writes: for translucent shells and crystals. */
    public static RenderType glow(ResourceLocation texture) {
        return RenderType.entityTranslucentEmissive(texture);
    }

    /** Alpha-blended and lit by the world: for chalk decals lying on the ground. */
    public static RenderType decal(ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
