package net.mandl.betteranvil.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.screen.ScreenHandler;

@Mixin(ScreenHandler.class)
public interface SHAccessors {
	@Invoker("sendContentUpdates")
	public abstract void invokeSendContentUpdates();
}
