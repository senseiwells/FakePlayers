package me.senseiwells.puppet.mixins;

import com.mojang.authlib.GameProfileRepository;
import net.minecraft.server.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Services.class)
public interface ServicesAccessor {
    @Mutable
    @Accessor
    void setProfileRepository(GameProfileRepository repository);
}
