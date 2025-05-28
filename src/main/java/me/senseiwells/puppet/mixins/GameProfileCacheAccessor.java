package me.senseiwells.puppet.mixins;

import com.mojang.authlib.GameProfileRepository;
import net.minecraft.server.players.GameProfileCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameProfileCache.class)
public interface GameProfileCacheAccessor {
    @Mutable
    @Accessor
    void setProfileRepository(GameProfileRepository repository);
}
