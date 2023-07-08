package shadows.wstweaks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = WitherSkeletonTweaks.MODID)
public class WSTEvents {

	@SubscribeEvent
	public static void witherTransform(LivingSpawnEvent.SpecialSpawn event) {
		if (event.getEntity() instanceof Skeleton skeleton && !skeleton.isRemoved()) {
			RandomSource rand = event.getLevel().getRandom();
			if (!event.getLevel().isClientSide()) {
				if (skeleton.level.dimension() == Level.NETHER || WSTConfig.allBiomes && event.getLevel().getRawBrightness(skeleton.blockPosition(), 0) < 9 && rand.nextFloat() < WSTConfig.allBiomesChance) {
					event.setCanceled(true);
					skeleton.getPersistentData().putBoolean("wst.removed", true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void join(EntityJoinLevelEvent e) {
		if (e.getEntity() instanceof Skeleton skeleton && e.getEntity().getPersistentData().getBoolean("wst.removed")) {
			e.setCanceled(true);
			WitherSkeleton witherSkel = skeleton.convertTo(EntityType.WITHER_SKELETON, true);
			if (witherSkel == null) return;
			ForgeEventFactory.onLivingConvert(skeleton, witherSkel);
			if (WSTConfig.giveBows) witherSkel.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
		}
	}

	@SubscribeEvent
	public static void handleDropsEvent(LivingDropsEvent event) {
		delSwords(event);
	}

	public static void delSwords(LivingDropsEvent event) {
		if (WSTConfig.delSwords && !event.getEntity().level.isClientSide && event.getEntity() instanceof AbstractSkeleton) {

			List<ItemEntity> toRemove = new ArrayList<>();
			for (ItemEntity entity : event.getDrops()) {
				ItemStack stack = entity.getItem();
				if (stack.getItem() == Items.STONE_SWORD || stack.getItem() == Items.BOW) {
					CompoundTag tag = stack.getTag();
					if (tag != null && (tag.contains("Damage") && tag.getAllKeys().size() > 2 || tag.getAllKeys().size() > 1)) continue;
					toRemove.add(entity);
				}
			}

			for (ItemEntity i : toRemove)
				event.getDrops().remove(i);
		}
	}
}
