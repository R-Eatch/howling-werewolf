/* SPDX-License-Identifier: MPL-2.0 */
package com.howlingwerewolf.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.Set;

/** A cube whose polygons are edited after construction to apply imported or clipped UVs. */
final class EditedModelCube extends ModelPart.Cube {
    EditedModelCube(float x, float y, float z, float width, float height, float depth,
                    float inflate, boolean mirror, float textureWidth, float textureHeight,
                    Set<Direction> faces) {
        super(0, 0, x, y, z, width, height, depth, inflate, inflate, inflate,
                mirror, textureWidth, textureHeight, faces);
    }

    @Override
    public void compile(PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        // Sodium's Cube implementation caches constructor UVs. Read the actual polygons here.
        var position = new Vector3f();
        var normal = new Vector3f();
        for (ModelPart.Polygon polygon : polygons) {
            pose.transformNormal(polygon.normal, normal);
            for (ModelPart.Vertex vertex : polygon.vertices) {
                pose.pose().transformPosition(vertex.pos.x / 16, vertex.pos.y / 16, vertex.pos.z / 16, position);
                buffer.addVertex(position.x, position.y, position.z, color, vertex.u, vertex.v,
                        overlay, light, normal.x, normal.y, normal.z);
            }
        }
    }
}
