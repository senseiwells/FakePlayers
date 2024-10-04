package me.senseiwells.players.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import me.senseiwells.players.network.FakeGamePacketListenerImpl;
import me.senseiwells.players.FakePlayer;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @WrapOperation(
        method = "placeNewPlayer",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/network/ServerGamePacketListenerImpl;"
        )
    )
    private ServerGamePacketListenerImpl onConstructGamePacketListener(
        MinecraftServer server,
        Connection connection,
        ServerPlayer player,
        CommonListenerCookie cookie,
        Operation<ServerGamePacketListenerImpl> original
    ) {
        if (player instanceof FakePlayer) {
            return new FakeGamePacketListenerImpl(server, connection, player, cookie);
        }
        return original.call(server, connection, player, cookie);
    }

    @WrapOperation(
        method = "respawn",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"
        )
    )
    private ServerPlayer onConstructServerPlayer(
        MinecraftServer server,
        ServerLevel level,
        GameProfile profile,
        ClientInformation clientInformation,
        Operation<ServerPlayer> original,
        ServerPlayer previous
    ) {
        if (previous instanceof FakePlayer) {
            return new FakePlayer(server, level, profile);
        }
        return original.call(server, level, profile, clientInformation);
    }
}
