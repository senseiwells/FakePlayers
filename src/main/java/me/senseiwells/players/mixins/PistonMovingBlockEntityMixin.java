package me.senseiwells.players.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.senseiwells.players.FakePlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(PistonMovingBlockEntity.class)
public class PistonMovingBlockEntityMixin {
    @WrapOperation(
        method = "moveCollidedEntities",
        constant = @Constant(classValue = ServerPlayer.class)
    )
    private static boolean onMoveNonPlayers(
        Object object,
        Operation<Boolean> original
    ) {
        if (object instanceof FakePlayer) {
            return false;
        }
        return original.call(object);
    }
}
