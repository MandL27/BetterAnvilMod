package net.mandl.betteranvil.mixin;

import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.LiteralText;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilScreenHandler.class)
public class AnvilMixin {
	@Inject(method = "updateResult()V", at = @At("TAIL"), cancellable = true)
	public void updateResultNew(CallbackInfo info) {
		// get some vars
		int repairCost = 0, enchantCost = 0, renameCost = 0, units = 0;
		// read the input stacks
		ItemStack left = ((FSHAccessors)this).getInput().getStack(0);
		ItemStack right = ((FSHAccessors)this).getInput().getStack(1);
		((ASHAccessors)this).getLevelCost().set(0);
		((FSHAccessors)this).getOutput().setStack(0, ItemStack.EMPTY);
		// can't operate on an empty left input, so return
		// (empty right input is valid for renames)
		if (left.isEmpty())	{
			info.cancel();
		}
		// dupe left so we can alter it non-destructively
		ItemStack left2 = left.copy();
		// renaming
		// remove a name if a blank string is input
		if (StringUtils.isBlank(((ASHAccessors)this).getNewItemName())) {
			if (left.hasCustomName()) {
				renameCost = 1;
				left2.removeCustomName();
			}
		// otherwise assign the new name if it's changed
		} else if (!((ASHAccessors)this).getNewItemName().equals(left.getName().getString())) {
			renameCost = 1;
			left2.setCustomName(new LiteralText(((ASHAccessors)this).getNewItemName()));
		}
		if (!right.isEmpty()) {
			// repair case 1: unit repair
			if (left2.isDamageable() && left2.getItem().canRepair(left, right) && left2.getDamage() > 0) {
				int unit = Math.min(left2.getDamage(), left2.getMaxDamage() / 4);
				for (units = 0; unit > 0 && units < right.getCount(); units++) {
					left2.setDamage(left2.getDamage() - unit);
					repairCost++;
					unit = Math.min(left2.getDamage(), left2.getMaxDamage() / 4);
				}
			// repair case 2: fusion repair
			} else if (left2.isDamageable() && left2.isOf(right.getItem()) && left2.getDamage() > 0) {
				left2.setDamage(Math.max(0, left2.getDamage() - (right.getMaxDamage() - right.getDamage())));
				repairCost = Math.max(1, Math.round((left.getDamage() - left2.getDamage()) * 4 / (float)left2.getMaxDamage()));
				units = repairCost;
			}
			// calc repair pwp
			for (int i = 0; i < units; i++) {
				repairCost += Math.floor(Math.min(i + left.getRepairCost(), 32) / 8d);
			}
			// enchantments
			if (left2.isOf(right.getItem()) || right.isOf(Items.ENCHANTED_BOOK)) {
				int enchantability = 5;
				if (left2.getItem() instanceof ArmorItem) {
					enchantability = ((ArmorItem)left2.getItem()).getMaterial().getEnchantability();
				} else if (left2.getItem() instanceof ToolItem) {
					enchantability = ((ToolItem)left2.getItem()).getMaterial().getEnchantability();
				}
				Map<Enchantment, Integer> leftEnc = EnchantmentHelper.get(left2);
				Map<Enchantment, Integer> rightEnc = EnchantmentHelper.get(right);
				for (Enchantment r : rightEnc.keySet()) {
					// skip null enchantments in case that ends up here somehow
					if (r == null) continue;
					// skip enchantments that aren't compatible with the target item, unless we're in creative mode
					if (!(r.isAcceptableItem(left) || left.isOf(Items.ENCHANTED_BOOK) || ((FSHAccessors)this).getPlayer().getAbilities().creativeMode)) continue;
					// skip mutually exclusive enchantments
					boolean skipped = false;
					for (Enchantment l : leftEnc.keySet()) {
							if (l != r && !l.canCombine(r)) {
								skipped = true;
								break;
							}
					}
					if (skipped) continue;
					// if we got this far we know we can apply this enchantment
					// calc the new level
					int leftLvl = leftEnc.getOrDefault(r, 0);
					int rightLvl = rightEnc.get(r);
					int finalLvl = leftLvl == rightLvl ? leftLvl + 1 : Math.max(leftLvl, rightLvl);
					int deltaLvl = finalLvl - leftLvl;
					// increasing enchantment level costs the delta level
					enchantCost += deltaLvl * Math.round(enchantmentRarity(r.getRarity()) * 10d / (double)enchantability);
					// adding new enchantments also costs the triangle of existing ones
					if (leftLvl == 0)
					{
						enchantCost += Math.round(triangle(leftEnc.keySet().size()) * 10 / ((10 + enchantability) / 2d));
					}
					leftEnc.put(r, finalLvl);
				}
				EnchantmentHelper.set(leftEnc, left2);
			}
			// combine costs
			int totalCost = repairCost + enchantCost + renameCost;
			((ASHAccessors)this).getLevelCost().set(totalCost);
			if (totalCost <= 0) {
				left2 = ItemStack.EMPTY;
			} else {
				left2.setRepairCost(Math.min(left2.getRepairCost() + units, 32));
			}
			((FSHAccessors)this).getOutput().setStack(0, left2);
			((SHAccessors)this).invokeSendContentUpdates();
		}
	}

	private int triangle(int v) {
		return (v * (v + 1)) / 2;
	}

	private int enchantmentRarity(Rarity r) {
		switch (r) {
			case COMMON:
				return 1;
			case UNCOMMON:
				return 2;
			case RARE:
				return 4;
			case VERY_RARE:
				return 8;
		}
		return 1;
	}
}
