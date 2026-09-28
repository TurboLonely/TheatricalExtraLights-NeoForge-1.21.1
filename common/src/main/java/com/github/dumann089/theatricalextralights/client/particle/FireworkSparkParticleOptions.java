package com.github.dumann089.theatricalextralights.client.particle;

import com.github.dumann089.theatricalextralights.particle.ModParticle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class FireworkSparkParticleOptions implements ParticleOptions {
    public static final MapCodec<FireworkSparkParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            com.mojang.serialization.Codec.FLOAT.fieldOf("red").forGetter(o -> o.red),
            com.mojang.serialization.Codec.FLOAT.fieldOf("green").forGetter(o -> o.green),
            com.mojang.serialization.Codec.FLOAT.fieldOf("blue").forGetter(o -> o.blue),
            com.mojang.serialization.Codec.FLOAT.fieldOf("scale").forGetter(o -> o.scale),
            com.mojang.serialization.Codec.FLOAT.optionalFieldOf("alpha", 0.95f).forGetter(o -> o.alpha),
            com.mojang.serialization.Codec.INT.fieldOf("lifetime").forGetter(o -> o.lifetime),
            com.mojang.serialization.Codec.FLOAT.fieldOf("gravity").forGetter(o -> o.gravity),
            com.mojang.serialization.Codec.BOOL.optionalFieldOf("trail", false).forGetter(o -> o.trail),
            com.mojang.serialization.Codec.BOOL.optionalFieldOf("strobe", false).forGetter(o -> o.strobe)
    ).apply(instance, FireworkSparkParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FireworkSparkParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeFloat(value.red);
                buf.writeFloat(value.green);
                buf.writeFloat(value.blue);
                buf.writeFloat(value.scale);
                buf.writeFloat(value.alpha);
                buf.writeInt(value.lifetime);
                buf.writeFloat(value.gravity);
                buf.writeBoolean(value.trail);
                buf.writeBoolean(value.strobe);
            },
            buf -> new FireworkSparkParticleOptions(
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readInt(),
                    buf.readFloat(),
                    buf.readBoolean(),
                    buf.readBoolean()
            )
    );

    public final float red;
    public final float green;
    public final float blue;
    public final float scale;
    public final float alpha;
    public final int lifetime;
    public final float gravity;
    public final boolean trail;
    public final boolean strobe;

    public FireworkSparkParticleOptions(float red, float green, float blue, float scale, float alpha, int lifetime, float gravity, boolean trail, boolean strobe) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.scale = scale;
        this.alpha = alpha;
        this.lifetime = lifetime;
        this.gravity = gravity;
        this.trail = trail;
        this.strobe = strobe;
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticle.FIREWORK_SPARK.get();
    }

    public String writeToString() {
        return red + " " + green + " " + blue + " " + scale + " " + alpha + " " + lifetime + " " + gravity + " " + trail + " " + strobe;
    }
}
