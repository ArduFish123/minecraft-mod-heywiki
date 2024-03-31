package wiki.mc.rtfw.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiki.mc.rtfw.RTFWClient;
import wiki.mc.rtfw.WikiPage;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (RTFWClient.readKey.matchesKey(keyCode, scanCode)) {
            Slot slot = this.focusedSlot;
            if (slot != null && slot.hasStack()) {
                String translationKey = slot.getStack().getItem().getTranslationKey();
                WikiPage.fromTranslationKey(translationKey).openInBrowser();
            }
        }
    }
}