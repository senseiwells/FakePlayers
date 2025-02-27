package me.senseiwells.players.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.senseiwells.players.FakePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyExpressionValue(
        method = "attack",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z",
            opcode = Opcodes.GETFIELD
        )
    )
    private boolean onResetDeltaMovement(boolean original, Entity target) {
        return target.hurtMarked && !(target instanceof FakePlayer);
    }
}
