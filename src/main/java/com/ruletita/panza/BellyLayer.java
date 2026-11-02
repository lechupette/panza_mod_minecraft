package com.ruletita.panza;

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

/** Panza que rodea el torso. El nivel (0-13) viene del nombre del equipo "rzbN" del jugador. */
public class BellyLayer<M extends EntityModel<? super AvatarRenderState>> extends RenderLayer<AvatarRenderState, M> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PanzaMod.MODID, "textures/entity/belly.png");
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
        ModelPart body = this.getParentModel().root().getChild("body");
        float t = Math.min(lvl / 13f, 1f);
        final float halfW = (4.4f + 3.0f * t) / 16f;
        final float front = (2.3f + 5.2f * t) / 16f;
        final float back = (2.3f + 1.2f * t) / 16f;
        final float y0 = 4.0f / 16f;
        final float y1 = (10.5f + 1.0f * t) / 16f;
        pose.pushPose();
        body.translateAndRotate(pose);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutoutNoCull(TEXTURE), (p, vc) ->
                box(p, vc, -halfW, y0, -front, halfW, y1, back, light));
        pose.popPose();
    }

    // Caras: el frente es -Z (mitad derecha de la textura, con el ombligo); el resto usa la mitad izquierda.
    private static void box(PoseStack.Pose p, VertexConsumer vc, float x0, float y0, float z0, float x1, float y1, float z1, int light) {
        quad(p, vc, light, 0.5f, 0f, 1f, 1f, 0, 0, -1, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0); // frente
        quad(p, vc, light, 0f, 0f, 0.5f, 1f, 0, 0, 1, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);   // espalda
        quad(p, vc, light, 0f, 0f, 0.5f, 1f, 1, 0, 0, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1);   // lado
        quad(p, vc, light, 0f, 0f, 0.5f, 1f, -1, 0, 0, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);  // lado
        quad(p, vc, light, 0f, 0f, 0.5f, 1f, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);  // arriba
        quad(p, vc, light, 0f, 0f, 0.5f, 1f, 0, 1, 0, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);   // abajo
    }

    private static void quad(PoseStack.Pose p, VertexConsumer vc, int light, float u0, float v0, float u1, float v1,
                             float nx, float ny, float nz,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        v(p, vc, light, ax, ay, az, u0, v1, nx, ny, nz);
        v(p, vc, light, bx, by, bz, u1, v1, nx, ny, nz);
        v(p, vc, light, cx, cy, cz, u1, v0, nx, ny, nz);
        v(p, vc, light, dx, dy, dz, u0, v0, nx, ny, nz);
    }

    private static void v(PoseStack.Pose p, VertexConsumer vc, int light, float x, float y, float z, float u, float vv, float nx, float ny, float nz) {
        vc.addVertex(p, x, y, z).setColor(255, 255, 255, 255).setUv(u, vv)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }
}
