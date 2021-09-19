package mc.altlore.mixin;

import com.google.common.collect.Lists;
import mc.altlore.client.Altlore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(ItemStack.class)
public class OverrideTooltipTextMixin {
    @Inject(method = "getTooltip", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void addHasAltLore(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        if (!isPressed() && hasLore()) {
            list.add(
                new TranslatableText("message.altlore.holdKey_1")
                    .formatted(Formatting.byName(Altlore.config.promptFormatting))
                    .append(Altlore.keyBinding.getBoundKeyLocalizedText())
                    .append(new TranslatableText("message.altlore.holdKey_2"))
            );
        }
    }
    
    @Inject(method = "getTooltip", at = @At("HEAD"), cancellable = true)
    public void replaceDisplay(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        if (isPressed() && hasLore()) {
            List<Text> list = Lists.newArrayList();
    
            list.addAll(
                Arrays.stream(
                    Altlore.lore.get(((ItemStack)(Object)this).getItem()))
                    .map(LiteralText::new)
                    .collect(Collectors.toCollection(ArrayList::new)
                    )
            );
            
            cir.setReturnValue(list);
        }
    }
    
    private boolean hasLore() {
        return Altlore.lore.containsKey(((ItemStack)(Object)this).getItem());
    }
    
    private boolean isPressed() {
        if (((KeyBindingCurrentAccessor)Altlore.keyBinding).getBoundKey().getCode() == -1) return false;
        
        return InputUtil.isKeyPressed(
            MinecraftClient.getInstance().getWindow().getHandle(),
            ((KeyBindingCurrentAccessor)Altlore.keyBinding).getBoundKey().getCode()
        );
    }
}
