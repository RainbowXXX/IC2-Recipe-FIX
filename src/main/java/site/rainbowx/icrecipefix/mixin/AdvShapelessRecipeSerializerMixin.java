package site.rainbowx.icrecipefix.mixin;

import com.google.gson.JsonObject;
import ic2.core.recipe.AdvShapelessRecipe;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author RainbowX
 * 注入类 AdvShapelessRecipe.Serializer
 */
@Mixin(AdvShapelessRecipe.Serializer.class)
public abstract class AdvShapelessRecipeSerializerMixin {
    @Unique
    private static final Map<Identifier, String> mp = new TreeMap<>();

    /**
     * 影子方法
     * 从json对象中读取指定合成表
     * @param id 合成表的路径
     * @param json json对象
     * @return 合成表
     */
    @Shadow
    public abstract AdvShapelessRecipe read(Identifier id, JsonObject json);

    @Inject(at = @At(value = "TAIL"), method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lic2/core/recipe/AdvShapelessRecipe;")
    public void afterReadJson(Identifier id, JsonObject json, CallbackInfoReturnable<AdvShapelessRecipe> ci) {
        AdvShapelessRecipe ret = ci.getReturnValue();
        if(!mp.containsKey(ret.getId())) {
            mp.put(ret.getId(), json.toString());
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "read(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lic2/core/recipe/AdvShapelessRecipe;", cancellable = true)
    public void beforeReadBuf(Identifier id, PacketByteBuf buf, CallbackInfoReturnable<AdvShapelessRecipe> ci) {
        String jsonStr = buf.readString();
        JsonObject obj = JsonHelper.deserialize(jsonStr);
        ci.setReturnValue(read(id, obj));
        ci.cancel();
    }

    @Inject(at = @At(value = "HEAD"), method = "write(Lnet/minecraft/network/PacketByteBuf;Lic2/core/recipe/AdvShapelessRecipe;)V", cancellable = true)
    public void beforeWriteBuf(PacketByteBuf buf, AdvShapelessRecipe recipe, CallbackInfo ci) {
        String str = mp.get(recipe.getId());
        buf.writeString(str);
        ci.cancel();
    }
}
