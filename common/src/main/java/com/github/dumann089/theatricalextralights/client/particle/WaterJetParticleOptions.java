package com.github.dumann089.theatricalextralights.client.particle;

import com.github.dumann089.theatricalextralights.particle.ModParticle;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class WaterJetParticleOptions implements ParticleOptions {

    public final float intensity;
    public final float thickness;
    public final float coneAngle;
    public final JetVariant variant;

    public final boolean hasYaw;
    public final float particleYaw;

    public WaterJetParticleOptions(
            float intensity,
            float thickness,
            float coneAngle,
            JetVariant variant
    ) {
        this.intensity = intensity;
        this.thickness = thickness;
        this.coneAngle = coneAngle;
        this.variant = variant;
        this.hasYaw = false;
        this.particleYaw = 0.0f;
    }

    public WaterJetParticleOptions(
            float intensity,
            float thickness,
            float coneAngle,
            JetVariant variant,
            float particleYaw
    ) {
        this.intensity = intensity;
        this.thickness = thickness;
        this.coneAngle = coneAngle;
        this.variant = variant;
        this.hasYaw = true;
        this.particleYaw = particleYaw;
    }

    public WaterJetParticleOptions(float intensity, float thickness, JetVariant variant) {
        this(intensity, thickness, 45.0f, variant);
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticle.WATERJET_OPTIONS.get();
    }

    public String writeToString() {
        if (hasYaw) {
            return intensity + " " + thickness + " " + coneAngle + " " + variant.name() + " " + particleYaw;
        }
        return intensity + " " + thickness + " " + coneAngle + " " + variant.name();
    }

    public static final MapCodec<WaterJetParticleOptions> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("intensity").forGetter(o -> o.intensity),
                    Codec.FLOAT.fieldOf("thickness").forGetter(o -> o.thickness),
                    Codec.FLOAT.optionalFieldOf("coneAngle", 45.0f).forGetter(o -> o.coneAngle),
                    JetVariant.CODEC.fieldOf("variant").forGetter(o -> o.variant),
                    Codec.BOOL.optionalFieldOf("hasYaw", false).forGetter(o -> o.hasYaw),
                    Codec.FLOAT.optionalFieldOf("particleYaw", 0.0f).forGetter(o -> o.particleYaw)
            ).apply(instance, (i, t, c, v, h, y) ->
                    h
                            ? new WaterJetParticleOptions(i, t, c, v, y)
                            : new WaterJetParticleOptions(i, t, c, v)
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, WaterJetParticleOptions> STREAM_CODEC =
            StreamCodec.of(
                    (buf, value) -> {
                        buf.writeFloat(value.intensity);
                        buf.writeFloat(value.thickness);
                        buf.writeFloat(value.coneAngle);
                        buf.writeEnum(value.variant);
                        buf.writeBoolean(value.hasYaw);
                        if (value.hasYaw) {
                            buf.writeFloat(value.particleYaw);
                        }
                    },
                    buf -> {
                        float intensity = buf.readFloat();
                        float thickness = buf.readFloat();
                        float coneAngle = buf.readFloat();
                        JetVariant variant = buf.readEnum(JetVariant.class);

                        boolean hasYaw = buf.readBoolean();
                        if (hasYaw) {
                            float yaw = buf.readFloat();
                            return new WaterJetParticleOptions(intensity, thickness, coneAngle, variant, yaw);
                        }

                        return new WaterJetParticleOptions(intensity, thickness, coneAngle, variant);
                    }
            );
}
