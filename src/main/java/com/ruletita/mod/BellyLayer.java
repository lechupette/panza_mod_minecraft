package com.ruletita.mod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Team;

import java.util.HashMap;
import java.util.Map;

/**
 * Panza delantera + nalgas. Es el torso con la propia skin del jugador, mas grande, sin tocar
 * brazos, piernas ni cabeza. El nivel (0-13) viene del nombre del equipo "rzbN" del jugador.
 */
public class BellyLayer<M extends EntityModel<? super AvatarRenderState>> extends RenderLayer<AvatarRenderState, M> {
    private static final Map<Integer, double[]> CURRENT = new HashMap<>();

    public BellyLayer(RenderLayerParent<AvatarRenderState, M> parent) {
        super(parent);
    }

    private static float targetLevel(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 0f;
        Entity e = mc.level.getEntity(entityId);
        if (e == null) return 0f;
        Team team = e.getTeam();
        if (team == null) return 0f;
        String n = team.getName();
        if (n.startsWith("rzb")) {
            try { return Integer.parseInt(n.substring(3)); } catch (NumberFormatException ignored) { }
        }
        return 0f;
    }

    private static float smooth(int id) {
        long now = System.nanoTime();
        if (CURRENT.size() > 128) CURRENT.clear();
        float target = targetLevel(id);
        double[] st = CURRENT.computeIfAbsent(id, k -> new double[]{target, now});
        float dt = (float) Math.min(0.1, (now - (long) st[1]) / 1.0e9);
        st[0] += (target - st[0]) * Math.min(1.0, dt * 6.0);
        st[1] = now;
        return (float) st[0];
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        float lvl = smooth(state.id);
        if (lvl < 0.05f) return;
        Identifier skin = state.skin.body().texturePath();
        ModelPart body = this.getParentModel().root().getChild("body");
        final float t = Math.min(lvl / 13f, 1f);
        pose.pushPose();
        body.translateAndRotate(pose);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutoutNoCull(skin), (p, vc) -> draw(p, vc, light, t));
        pose.popPose();
    }

    // Todo en pixeles del modelo (1 bloque = 16 px). Frente = -Z. Origen = parte superior del torso.
    private static void draw(PoseStack.Pose p, VertexConsumer vc, int light, float t) {
        final float e = 0.7f;
        // torso (panza): crece hacia delante y un poco a los lados
        float x0 = -(4f + e + 2.0f * t), x1 = 4f + e + 2.0f * t;
        float y0 = 3.5f, y1 = 12f + 0.6f * t;
        float z0 = -(2f + e + 5.5f * t), z1 = 2f + e;
        torso(p, vc, light, x0, y0, z0, x1, y1, z1);
        // nalgas: dos cubos detras, bajo la cintura
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
        // frente (skin: x 20..28)
        q(p, vc, light, 0, 0, -1, x0, y0, z0, 20, va, x1, y0, z0, 28, va, x1, y1, z0, 28, vb, x0, y1, z0, 20, vb);
        // espalda (skin: x 32..40)
        q(p, vc, light, 0, 0, 1, x1, y0, z1, 32, va, x0, y0, z1, 40, va, x0, y1, z1, 40, vb, x1, y1, z1, 32, vb);
        // lados (skin: 16..20 derecha, 28..32 izquierda)
        q(p, vc, light, -1, 0, 0, x0, y0, z0, 20, va, x0, y0, z1, 16, va, x0, y1, z1, 16, vb, x0, y1, z0, 20, vb);
        q(p, vc, light, 1, 0, 0, x1, y0, z0, 28, va, x1, y0, z1, 32, va, x1, y1, z1, 32, vb, x1, y1, z0, 28, vb);
        // arriba y abajo: muestrea una franja de la skin del frente
        q(p, vc, light, 0, -1, 0, x0, y0, z0, 20, va, x1, y0, z0, 28, va, x1, y0, z1, 28, va, x0, y0, z1, 20, va);
        float vv = Math.min(vb, 31.5f);
        q(p, vc, light, 0, 1, 0, x0, y1, z0, 20, vv, x1, y1, z0, 28, vv, x1, y1, z1, 28, vv, x0, y1, z1, 20, vv);
    }

    private static void nalga(PoseStack.Pose p, VertexConsumer vc, int light, float x0, float x1, float y0, float y1, float z0, float z1) {
        float va = Math.max(26f, vOf(y0)), vb = Math.min(32f, vOf(y1));
        float ua = 33f, ub = 39f;
        // atras (la parte baja de la espalda de la skin)
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
        vc.addVertex(p, x / 16f, y / 16f, z / 16f).setColor(255, 255, 255, 255).setUv(u / 64f, vv / 64f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }
}
