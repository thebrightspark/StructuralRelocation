package brightspark.structuralrelocation.particle;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

import java.util.Map;
import java.util.Random;

public enum TeleportAnimation {
    FLAIR() {
        @Override
        public void init(Map<String, Object> data, Random rand) {
            data.put("rotX", randRotation(rand));
            data.put("rotY", randRotation(rand));
            data.put("rotZ", randRotation(rand));
        }

        @Override
        public void preRender(Map<String, Object> data, Entity entity, float partialTicks, double posX, double posY, double posZ, int particleAge, int particleMaxAge, boolean inverse) {
            float ageProgressInv = calcAgeProgressInv(particleAge, particleMaxAge, partialTicks, inverse);
            float ageProgress = 1 - ageProgressInv;
            GlStateManager.rotate(ageProgress * 200F, (float) data.get("rotX"), (float) data.get("rotY"), (float) data.get("rotZ"));
            GlStateManager.scale(ageProgressInv, ageProgressInv, ageProgressInv);
        }
    },
    SCALE() {
        @Override
        public void preRender(Map<String, Object> data, Entity entity, float partialTicks, double posX, double posY, double posZ, int particleAge, int particleMaxAge, boolean inverse) {
            float ageProgressInv = calcAgeProgressInv(particleAge, particleMaxAge, partialTicks, inverse);
            GlStateManager.scale(ageProgressInv, ageProgressInv, ageProgressInv);
        }
    }/*,
    FADE() {
        @Override
        public void preRender(Map<String, Object> data, Entity entity, float partialTicks, double posX, double posY, double posZ, int particleAge, int particleMaxAge, boolean inverse) {
            float ageProgressInv = calcAgeProgressInv(particleAge, particleMaxAge, partialTicks, inverse);
            float ageProgress = 1 - ageProgressInv;
            System.out.println("Alpha: " + ageProgress);
            GlStateManager.color(1F, 1F, 1F, ageProgress);
        }
    },
    FADE_SCALE() {
        @Override
        public void preRender(Map<String, Object> data, Entity entity, float partialTicks, double posX, double posY, double posZ, int particleAge, int particleMaxAge, boolean inverse) {
            float ageProgressInv = calcAgeProgressInv(particleAge, particleMaxAge, partialTicks, inverse);
            float ageProgress = 1 - ageProgressInv;
            GlStateManager.color(1F, 1F, 1F, ageProgress);
            GlStateManager.scale(ageProgress + 0.5, ageProgress + 0.5, ageProgress + 0.5);
        }
    }*/;

    public void init(Map<String, Object> data, Random rand) {}

    public abstract void preRender(Map<String, Object> data, Entity entity, float partialTicks, double posX, double posY, double posZ, int particleAge, int particleMaxAge, boolean inverse);

    protected float randRotation(Random rand)
    {
        return (rand.nextFloat() - 0.5F) * 2;
    }

    protected float calcAgeProgressInv(int particleAge, int particleMaxAge, float partialTicks, boolean inverse) {
        double agePct = ((double) particleAge + partialTicks) / (double) particleMaxAge;
        return  (float) Math.sin((agePct * (Math.PI / 2)) + (inverse ? 0 : Math.PI / 2));
    }
}
