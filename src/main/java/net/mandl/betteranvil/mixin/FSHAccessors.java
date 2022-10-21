package net.mandl.betteranvil.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ForgingScreenHandler;

@Mixin(ForgingScreenHandler.class)
public interface FSHAccessors {
	@Accessor("input")
	public abstract Inventory getInput();
	@Accessor("output")
	public abstract CraftingResultInventory getOutput();
	@Accessor("player")
	public abstract PlayerEntity getPlayer();
}
