package net.mandl.betteranvil.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;

@Mixin(AnvilScreenHandler.class)
public interface ASHAccessors {
	@Accessor("levelCost")
	public abstract Property getLevelCost();
	@Accessor("repairItemUsage")
	public abstract int getRepairItemUsage();
	@Accessor("repairItemUsage")
	public abstract void setRepairItemUsage(int usage);
	@Accessor("newItemName")
	public abstract String getNewItemName();	
}
