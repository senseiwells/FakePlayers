package me.senseiwells.puppet.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.senseiwells.puppet.network.PuppetGamePacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public class InteractionHandlerMixin {
    @Shadow @Final ServerGamePacketListenerImpl field_28963;

    @ModifyExpressionValue(
        method = "performInteraction",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl$EntityInteraction;run(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
        )
    )
    private InteractionResult storeInteractionResultForFake(InteractionResult original) {
        if (this.field_28963 instanceof PuppetGamePacketListenerImpl fake) {
            fake.pushResult(original);
        }
        return original;
    }
}
