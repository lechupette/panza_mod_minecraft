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


/**
 * Panza delantera + nalgas. Conserva la geometria base y escala el volumen con 10 niveles
 * de alimentacion y 5 niveles de reserva. El equipo sincroniza el nivel con los clientes.
 */
public class BellyLayer<M extends EntityModel<? super AvatarRenderState>> extends RenderLayer<AvatarRenderState, M> {
    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        float level = ClientEventos.smoothLevel(state.id);
        if (level < 0.05f) return;
        Identifier skin = state.skin.body().texturePath();
        ModelPart body = this.getParentModel().root().getChild("body");
        final float t = Math.min(level / 15f, 1f);
        pose.pushPose();
        body.translateAndRotate(pose);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutoutNoCull(skin), (p, vc) -> draw(p, vc, light, t));
        pose.popPose();
    }

    // Todo en pixeles del modelo (1 bloque = 16 px). Frente = -Z.
    private static void draw(PoseStack.Pose p, VertexConsumer vc, int light, float t) {
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
        float va = Math.max(26f, vOf(y0)), vb = Math.min(32f, vOf(y1));
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
        vc.addVertex(p, x / 16f, y / 16f, z / 16f).setColor(255, 255, 255, 255).setUv(u / 64f, vv / 64f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }
}
