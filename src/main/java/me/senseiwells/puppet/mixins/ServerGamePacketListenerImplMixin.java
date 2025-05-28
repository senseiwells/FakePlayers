package me.senseiwells.puppet.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.senseiwells.puppet.network.PuppetGamePacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @ModifyExpressionValue(
        method = "handleUseItemOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"
        )
    )
    private InteractionResult storeUseOnResultForFake(InteractionResult original) {
        if ((Object) this instanceof PuppetGamePacketListenerImpl fake) {
            fake.pushResult(original);
        }
        return original;
    }

    @ModifyExpressionValue(
        method = "handleUseItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
        )
    )
    private InteractionResult storeUseResultForFake(InteractionResult original) {
        if ((Object) this instanceof PuppetGamePacketListenerImpl fake) {
            fake.pushResult(original);
        }
        return original;
    }
}
