package com.ruletita.mod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;

/** Panza y zona posterior: la forma base siempre está presente; la reserva aumenta su volumen. */
public class BellyLayer<M extends EntityModel<? super AvatarRenderState>> extends RenderLayer<AvatarRenderState, M> {
    public BellyLayer(RenderLayerParent<AvatarRenderState, M> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        // La panza base no depende de tener grasa acumulada. Antes se retornaba con grasa 0,
        // por lo que el jugador no veía absolutamente nada al comenzar.
        if (!Debug.belly || state.isInvisible) return;
        float fat = ClientEventos.smoothFat(state.id);
        Identifier skin = state.skin.body().texturePath();
        ModelPart body = this.getParentModel().root().getChild("body");
        // Volumen base visible (0.25) + hasta 0.75 adicional con los 7 niveles de reserva.
        final float t = 0.25f + 0.75f * Math.max(0f, Math.min(fat / 7f, 1f));
        pose.pushPose();
        body.translateAndRotate(pose);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutoutNoCull(skin), (p, vc) -> draw(p, vc, light, t));
        pose.popPose();

        int musc = ClientEventos.muscleOf(state.id);
        if (musc <= 0) return;

        // Tono: la masa de la zona util (grasa 1 a 4) se marca con el musculo; la de la zona pura
        // sigue blanda. Con mas grasa pura, la parte tonificada es una fraccion menor del volumen.
        float tUtil = 0.25f + 0.75f * Math.min(fat, 4f) / 7f;
        float tono = Math.max(0f, Math.min(1f, (musc / 3f) * (tUtil / t)));
        final int alpha = (int) (255f * tono);
        pose.pushPose();
        body.translateAndRotate(pose);
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TONO), (p, vc) -> drawTono(p, vc, light, t, alpha));
        pose.popPose();

        // Brazos: crece el musculo (el brazo no muestra grasa). Bulto en el brazo superior.
        boolean slim = state.skin.model() == PlayerModelType.SLIM;
        float g = 0.10f + 0.50f * (musc / 3f);
        brazo(pose, collector, light, skin, this.getParentModel().root().getChild("right_arm"), 40, 16, slim, false, g);
        brazo(pose, collector, light, skin, this.getParentModel().root().getChild("left_arm"), 32, 48, slim, true, g);
        // Capa de manga (si la skin la tiene): un poco mas grande, para que el bulto no tape mangas ni camperas.
        brazo(pose, collector, light, skin, this.getParentModel().root().getChild("right_arm"), 40, 32, slim, false, g + 0.25f);
        brazo(pose, collector, light, skin, this.getParentModel().root().getChild("left_arm"), 48, 48, slim, true, g + 0.25f);
    }

    private static final Identifier TONO = Identifier.fromNamespaceAndPath(RuletitaMod.MODID, "textures/entity/musculo.png");
    private static int alphaActual = 255;

    private void brazo(PoseStack pose, SubmitNodeCollector collector, int light, Identifier skin, ModelPart arm,
                       int ox, int oy, boolean slim, boolean left, float g) {
        pose.pushPose();
        arm.translateAndRotate(pose);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutoutNoCull(skin),
                (p, vc) -> drawBrazo(p, vc, light, ox, oy, slim, left, g));
        pose.popPose();
    }

    // Brazo superior inflado (coordenadas locales del brazo; cubo vanilla: y -2..10, z -2..2).
    private static void drawBrazo(PoseStack.Pose p, VertexConsumer vc, int light, int ox, int oy, boolean slim, boolean left, float g) {
        alphaActual = 255;
        int w = slim ? 3 : 4, d = 4;
        float bx0 = left ? -1f : (slim ? -2f : -3f);
        float bx1 = bx0 + w;
        float x0 = bx0 - g, x1 = bx1 + g;
        // La parte alta queda al ras del hombro: solo crece hacia los costados, adelante y atras.
        float y0 = -1.85f, y1 = 4.5f;
        float z0 = -2f - g, z1 = 2f + g;
        float va = oy + d, vb = oy + d + 6.5f;
        float uF0 = ox + d, uF1 = ox + d + w;
        float uB0 = ox + 2 * d + w, uB1 = ox + 2 * d + 2 * w;
        float uW0 = ox, uW1 = ox + d;
        float uE0 = ox + d + w, uE1 = ox + 2 * d + w;
        q(p, vc, light, 0, 0, -1, x0, y0, z0, uF0, va, x1, y0, z0, uF1, va, x1, y1, z0, uF1, vb, x0, y1, z0, uF0, vb);
        q(p, vc, light, 0, 0, 1, x1, y0, z1, uB0, va, x0, y0, z1, uB1, va, x0, y1, z1, uB1, vb, x1, y1, z1, uB0, vb);
        q(p, vc, light, -1, 0, 0, x0, y0, z1, uW0, va, x0, y0, z0, uW1, va, x0, y1, z0, uW1, vb, x0, y1, z1, uW0, vb);
        q(p, vc, light, 1, 0, 0, x1, y0, z0, uE0, va, x1, y0, z1, uE1, va, x1, y1, z1, uE1, vb, x1, y1, z0, uE0, vb);
        q(p, vc, light, 0, 1, 0, x0, y1, z0, ox + d, oy + d + 6f, x1, y1, z0, ox + d + w, oy + d + 6f,
                x1, y1, z1, ox + d + w, oy + d + 6f, x0, y1, z1, ox + d, oy + d + 6f);
        q(p, vc, light, 0, -1, 0, x0, y0, z1, ox + d, oy, x1, y0, z1, ox + d + w, oy, x1, y0, z0, ox + d + w, oy + d, x0, y0, z0, ox + d, oy + d);
    }

    // Marcas de pectorales, abdominales y nalgas, translucidas, apenas por fuera de la panza.
    private static void drawTono(PoseStack.Pose p, VertexConsumer vc, int light, float t, int alpha) {
        alphaActual = alpha;
        final float e = 0.7f;
        float x0 = -(4f + e + 2.0f * t), x1 = 4f + e + 2.0f * t;
        float y0 = 3.5f, y1 = 12f + 0.6f * t;
        float z0 = -(2f + e + 5.5f * t) - 0.03f;
        float va = vOf(y0), vb = vOf(y1);
        q(p, vc, light, 0, 0, -1, x0, y0, z0, 20, va, x1, y0, z0, 28, va, x1, y1, z0, 28, vb, x0, y1, z0, 20, vb);
        float by0 = 8.6f, by1 = 13.4f + 0.8f * t;
        float bz1 = 2f + e + 1.2f + 3.8f * t + 0.03f;
        float bx = 4.4f + 1.2f * t;
        float nva = Math.max(26f, vOf(by0)), nvb = Math.min(31.5f, vOf(by1));
        q(p, vc, light, 0, 0, 1, -0.1f, by0, bz1, 33, nva, -bx, by0, bz1, 39, nva, -bx, by1, bz1, 39, nvb, -0.1f, by1, bz1, 33, nvb);
        q(p, vc, light, 0, 0, 1, bx, by0, bz1, 33, nva, 0.1f, by0, bz1, 39, nva, 0.1f, by1, bz1, 39, nvb, bx, by1, bz1, 33, nvb);
        alphaActual = 255;
    }

    // Todo en pixeles del modelo (1 bloque = 16 px). Frente = -Z.
    private static void draw(PoseStack.Pose p, VertexConsumer vc, int light, float t) {
        alphaActual = 255;
        final float e = 0.7f;
        float x0 = -(4f + e + 2.0f * t), x1 = 4f + e + 2.0f * t;
        float y0 = 3.5f, y1 = 12f + 0.6f * t;
        float z0 = -(2f + e + 5.5f * t), z1 = 2f + e;
        torso(p, vc, light, x0, y0, z0, x1, y1, z1);
        float by0 = 8.6f, by1 = 13.4f + 0.8f * t;
        float bz0 = 2f + e - 0.05f, bz1 = 2f + e + 1.2f + 3.8f * t;
        float bx = 4.4f + 1.2f * t;
        nalga(p, vc, light, -bx, -0.1f, by0, by1, bz0, bz1);
        nalga(p, vc, light, 0.1f, bx, by0, by1, bz0, bz1);
    }

    private static float vOf(float y) {
        return 20f + Math.max(0f, Math.min(12f, y));
    }

    private static void torso(PoseStack.Pose p, VertexConsumer vc, int light, float x0, float y0, float z0, float x1, float y1, float z1) {
        float va = vOf(y0), vb = vOf(y1);
        q(p, vc, light, 0, 0, -1, x0, y0, z0, 20, va, x1, y0, z0, 28, va, x1, y1, z0, 28, vb, x0, y1, z0, 20, vb);
        q(p, vc, light, 0, 0, 1, x1, y0, z1, 32, va, x0, y0, z1, 40, va, x0, y1, z1, 40, vb, x1, y1, z1, 32, vb);
        q(p, vc, light, -1, 0, 0, x0, y0, z0, 20, va, x0, y0, z1, 16, va, x0, y1, z1, 16, vb, x0, y1, z0, 20, vb);
        q(p, vc, light, 1, 0, 0, x1, y0, z0, 28, va, x1, y0, z1, 32, va, x1, y1, z1, 32, vb, x1, y1, z0, 28, vb);
        q(p, vc, light, 0, -1, 0, x0, y0, z0, 20, va, x1, y0, z0, 28, va, x1, y0, z1, 28, va, x0, y0, z1, 20, va);
        float vv = Math.min(vb, 31.5f);
        q(p, vc, light, 0, 1, 0, x0, y1, z0, 20, vv, x1, y1, z0, 28, vv, x1, y1, z1, 28, vv, x0, y1, z1, 20, vv);
    }

    private static void nalga(PoseStack.Pose p, VertexConsumer vc, int light, float x0, float x1, float y0, float y1, float z0, float z1) {
        // 31.5 y no 32: v=32 cae justo en el borde de la capa exterior de la skin (transparente),
        // y la cara inferior de las nalgas se descartaba por completo.
        float va = Math.max(26f, vOf(y0)), vb = Math.min(31.5f, vOf(y1));
        float ua = 33f, ub = 39f;
        q(p, vc, light, 0, 0, 1, x1, y0, z1, ua, va, x0, y0, z1, ub, va, x0, y1, z1, ub, vb, x1, y1, z1, ua, vb);
        q(p, vc, light, 0, 0, -1, x0, y0, z0, ua, va, x1, y0, z0, ub, va, x1, y1, z0, ub, vb, x0, y1, z0, ua, vb);
        q(p, vc, light, -1, 0, 0, x0, y0, z0, ua, va, x0, y0, z1, ub, va, x0, y1, z1, ub, vb, x0, y1, z0, ua, vb);
        q(p, vc, light, 1, 0, 0, x1, y0, z0, ua, va, x1, y0, z1, ub, va, x1, y1, z1, ub, vb, x1, y1, z0, ua, vb);
        q(p, vc, light, 0, -1, 0, x0, y0, z0, ua, va, x1, y0, z0, ub, va, x1, y0, z1, ub, va, x0, y0, z1, ua, va);
        q(p, vc, light, 0, 1, 0, x0, y1, z0, ua, vb, x1, y1, z0, ub, vb, x1, y1, z1, ub, vb, x0, y1, z1, ua, vb);
    }

    private static void q(PoseStack.Pose p, VertexConsumer vc, int light, float nx, float ny, float nz,
                          float ax, float ay, float az, float au, float av,
                          float bx, float by, float bz, float bu, float bv,
                          float cx, float cy, float cz, float cu, float cv,
                          float dx, float dy, float dz, float du, float dv) {
        v(p, vc, light, nx, ny, nz, ax, ay, az, au, av);
        v(p, vc, light, nx, ny, nz, bx, by, bz, bu, bv);
        v(p, vc, light, nx, ny, nz, cx, cy, cz, cu, cv);
        v(p, vc, light, nx, ny, nz, dx, dy, dz, du, dv);
    }

    private static void v(PoseStack.Pose p, VertexConsumer vc, int light, float nx, float ny, float nz,
                          float x, float y, float z, float u, float vv) {
        vc.addVertex(p, x / 16f, y / 16f, z / 16f).setColor(255, 255, 255, alphaActual).setUv(u / 64f, vv / 64f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }
}
