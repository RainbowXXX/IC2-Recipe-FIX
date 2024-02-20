package site.rainbowx.icrecipefix.mixin;

import com.google.gson.JsonObject;
import ic2.api.recipe.IRecipeInput;
import ic2.core.recipe.v2.IntegerOutputRecipeSerializer;
import ic2.core.recipe.v2.RecipeHolder;
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
 * 注入类 IntegerOutputRecipeSerializer
 */
@Mixin({IntegerOutputRecipeSerializer.class})
public abstract class IntegerOutputRecipeSerializerMixin {
    @Unique
    private static final Map<Identifier, String> mp = new TreeMap<>();

    /**
     * 影子方法
     * 从json对象中读取指定合成表
     * @param id 合成表的路径
     * @param json json对象
     * @return 合成表
     */
    @Shadow public abstract RecipeHolder<IRecipeInput, Integer> read(Identifier id, JsonObject json);

    @Inject(at = @At(value = "TAIL"), method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lic2/core/recipe/v2/RecipeHolder;")
    public void afterReadJson(Identifier id, JsonObject json, CallbackInfoReturnable<RecipeHolder<IRecipeInput, Integer>> ci) {
        RecipeHolder<IRecipeInput, Integer> ret = ci.getReturnValue();
        if(!IntegerOutputRecipeSerializerMixin.mp.containsKey(ret.getId())) {
            IntegerOutputRecipeSerializerMixin.mp.put(ret.getId(), json.toString());
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "read(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lic2/core/recipe/v2/RecipeHolder;", cancellable = true)
    public void beforeReadBuf(Identifier id, PacketByteBuf buf, CallbackInfoReturnable<RecipeHolder<IRecipeInput, Integer>> ci) {
        String jsonStr = buf.readString();
        JsonObject obj = JsonHelper.deserialize(jsonStr);
        ci.setReturnValue(read(id, obj));
        ci.cancel();
    }

    @Inject(at = @At(value = "HEAD"), method = "write(Lnet/minecraft/network/PacketByteBuf;Lic2/core/recipe/v2/RecipeHolder;)V", cancellable = true)
    public void beforeWriteBuf(PacketByteBuf buf, RecipeHolder<IRecipeInput, Integer> recipe, CallbackInfo ci) {
        String str = mp.get(recipe.getId());
        buf.writeString(str);
        ci.cancel();
    }
}
