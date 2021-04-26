package com.ormoyo.ormoyoutil.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ormoyo.ormoyoutil.OrmoyoUtil;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(modid = OrmoyoUtil.MODID)
public class Utils {
	private static final Set<ITick> tickables = Sets.newHashSet();
	private static final Set<Animation> animations = Sets.newHashSet();
	
	/**
	 * Performs a consumer after amount of ticks
	 * @param consumer The consumer to perform
	 * @param consumerValue The value the consumer need to perform
	 * @param tickAmount The ticks to wait to perform
	 */
	public static<T> void performConsumerAfterAmountOfTicks(Consumer<T> consumer, T consumerValue, int tickAmount) {
		performConsumerAfterAmountOfTicks(consumer, consumerValue, tickAmount, 1);
	}
	
	/**
	 * Performs a consumer after amount of ticks for multiple times
	 * @param consumer The consumer to perform
	 * @param consumerValue The value the consumer need to perform
	 * @param tickAmount The ticks to wait to perform
	 * @param performAmount The amount of times to perform
	 */
	public static<T> void performConsumerAfterAmountOfTicks(Consumer<T> consumer, T consumerValue, int tickAmount, int performAmount) {
		tickables.add(new ConsumerPerform<T>(consumer, consumerValue, tickAmount, performAmount));
	}
	
	public static<T> void performAnimation(Animation animation) {
		animations.add(animation);
	}
	
	public static int randomInt(int min, int max) {
        Random r = new Random();
        return min + (int) (r.nextDouble() * ((max - min) + 1));
	}
	
	public static int randomInt(int min, int max, Random random) {
		return  min + (int) (random.nextDouble() * ((max - min) + 1));
	}
	
	public static double randomDouble(double min, double max) {
		return min + (max - min) * new Random().nextDouble();
	}
	
	public static double randomDouble(double min, double max, Random random) {
		return min + (max - min) * random.nextDouble();
	}
	
	@SideOnly(Side.CLIENT)
	public static void convertColorToRender(Color color) {
		GlStateManager.color(color.getRed() / 255, color.getGreen() / 255, color.getBlue() / 255, color.getAlpha() / 255);
	}
	
	@SideOnly(Side.CLIENT)
	public static int interpolateInt(int value, int prevValue, float partialTicks) {
		return Math.round(prevValue + (value - prevValue) * partialTicks);
	}
	
	@SideOnly(Side.CLIENT)
	public static double interpolateDouble(double value, double prevValue, float partialTicks) {
		return prevValue + (value - prevValue) * partialTicks;
	}
	
	@SideOnly(Side.CLIENT)
	public static float interpolateFloat(float value, float prevValue, float partialTicks) {
		return prevValue + (value - prevValue) * partialTicks;
	}
	
	@SideOnly(Side.CLIENT)
	public static Vec3d interpolateVec3d(Vec3d value, Vec3d prevValue, float partialTicks) {
		return new Vec3d(prevValue.x + (value.x - prevValue.x) * partialTicks, 
				prevValue.y + (value.y - prevValue.y) * partialTicks, 
				prevValue.z + (value.z - prevValue.z) * partialTicks);
	}
	
	public static DamageSource copyDamageSource(DamageSource source, DamageSource target) {
		if(source != null) {
			if(source.isDifficultyScaled()) {
				target.setDifficultyScaled();
			}
			if(source.isFireDamage()) {
				target.setFireDamage();
			}
			if(source.isUnblockable()) {
				target.setDamageBypassesArmor();
			}
			if(source.isMagicDamage()) {
				target.setMagicDamage();
			}
			if(source.isExplosion()) {
				target.setExplosion();
			}
			if(source.isProjectile()) {
				target.setProjectile();
			}
			if(source.isDamageAbsolute()) {
				target.setDamageIsAbsolute();
			}
			if(source.canHarmInCreative()) {
				target.setDamageAllowedInCreativeMode();
			}
			return target;
		}
		return null;
	}
	
	public static Entity getEntityEntityLookingAt(EntityLivingBase entity, float reachDistance) {
		Vec3d vec = getPosEntityLookingAt(entity, reachDistance);
		EntityRayResult<Entity> result = raytraceEntities(entity.world, new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ), vec, false, false, true);
		if(!result.entities.isEmpty()) {
			Map<Double, Entity> distances = Maps.newHashMap();
			for(Entity hit : result.entities) {
				if(hit == entity) continue;
				distances.put((double)entity.getDistance(hit), hit);
			}
			if(!distances.isEmpty()) {
				return distances.get(Collections.min(distances.keySet()));
			}
		}
		return null;
	}
	
	public static<T extends Entity> T getEntityEntityLookingAt(Class<T> classEntity, EntityLivingBase entity, float reachDistance) {
		Vec3d vec = getPosEntityLookingAt(entity, reachDistance);
		EntityRayResult<T> result = raytraceEntities(classEntity, entity.world, new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ), vec, false, false, true);
		if(!result.entities.isEmpty()) {
			Map<Double, T> distances = Maps.newHashMap();
			for(T hit : result.entities) {
				if(hit == entity) continue;
				distances.put((double)entity.getDistance(hit), (T) hit);
			}
			if(!distances.isEmpty()) {
				return distances.get(Collections.min(distances.keySet()));
			}
		}
		return null;
	}
	
    public static Vec3d getPosEntityLookingAt(EntityLivingBase entity, float reachDistance) {
	    Vec3d pos = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
	    Vec3d look = entity.getLook(1.0F);
	    Vec3d vec = pos.addVector(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
		return vec;
    }
	
    public static double getYawBetweenEntities(Entity first, Entity second) {
        return Math.atan2(first.posZ - second.posZ, first.posX - second.posX) * (180 / Math.PI) + 90;
    }
	
    public static double getPitchBetweenEntities(Entity first, Entity second) {
		double dx = first.posX - second.posX;
		double dz = first.posZ - second.posZ;
        return Math.atan2((first.posY + first.getEyeHeight()) - (second.posY + (second.height / 2.0F)), Math.sqrt(dx * dx + dz * dz)) * 180 / Math.PI;
    }
    
    public static double getYawBetweenVec(Vec3d first, Vec3d second) {
        return MathHelper.atan2(first.z - second.z, first.x - second.x) * 180 / Math.PI + 90;
    }
    
    public double getPitchBetweenVec(Vec3d first, Vec3d second) {
		double dx = first.x - second.x;
		double dz = first.z - second.z;
        return Math.atan2(first.y - second.y, Math.sqrt(dx * dx + dz * dz)) * 180 / Math.PI;
    }
    
    public static double getYawBetweenEntityAndVec(Entity entity, Vec3d vec) {
        return MathHelper.atan2(entity.posZ - vec.z, entity.posX - vec.x) * 180 / Math.PI + 90;
    }
    
    public static double getPitchBetweenEntityAndVec(Entity entity, Vec3d vec) {
		double dx = entity.posX - vec.x;
		double dz = entity.posZ - vec.z;
        return Math.atan2((entity.posY + entity.getEyeHeight()) - vec.y, Math.sqrt(dx * dx + dz * dz)) * 180 / Math.PI;
    }
    
	public static float getYawFromFacing(EnumFacing facing) {
		switch(facing) {
		case NORTH:
			return 0;
		case EAST:
			return 90;
		case SOUTH:
			return 180;
		case WEST:
			return 270;
		default:
			throw new IllegalStateException("Unable to get yaw from facing " + facing);
		}
	}
	
	public static float getPitchFromFacing(EnumFacing facing) {
		switch(facing) {
		case UP:
			return -90;
		case DOWN:
			return 90;
		default:
			throw new IllegalStateException("Unable to get pitch from facing " + facing);
		}
	}
    
	public static boolean doesBlockHaveCollison(World world, BlockPos pos) {
		return world.getBlockState(pos).getCollisionBoundingBox(world, pos) != Block.NULL_AABB;
	}
	
	public static EntityRayResult<Entity> raytraceEntities(World world, Vec3d from, Vec3d to, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        EntityRayResult<Entity> result = new EntityRayResult<Entity>();
        result.setBlockHit(world.rayTraceBlocks(new Vec3d(from.x, from.y, from.z), to, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock));
        double collidePosX;
        double collidePosY;
        double collidePosZ;
        if (result.getBlockHit() != null) {
            collidePosX = result.getBlockHit().hitVec.x;
            collidePosY = result.getBlockHit().hitVec.y;
            collidePosZ = result.getBlockHit().hitVec.z;
        }else {
            collidePosX = 30;
            collidePosY = 30;
            collidePosZ = 30;
        }
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(Math.min(from.x, collidePosX), Math.min(from.y, collidePosY), Math.min(from.z, collidePosZ), Math.max(from.x, collidePosX), Math.max(from.y, collidePosY), Math.max(from.z, collidePosZ)).grow(1, 1, 1));
        for (Entity entity : entities) {
            float pad = entity.getCollisionBorderSize() + 0.5f;
            AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(pad, pad, pad);
            RayTraceResult hit = aabb.calculateIntercept(from, to);
            if (aabb.contains(from)) {
                result.addEntityHit(entity);
            } else if (hit != null) {
                result.addEntityHit(entity);
            }
        }
        return result;
	}
	
	public static<T extends Entity> EntityRayResult<T> raytraceEntities(Class<T> classEntity, World world, Vec3d from, Vec3d to, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        EntityRayResult<T> result = new EntityRayResult<T>();
        result.setBlockHit(world.rayTraceBlocks(new Vec3d(from.x, from.y, from.z), to, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock));
        double collidePosX;
        double collidePosY;
        double collidePosZ;
        if (result.getBlockHit() != null) {
            collidePosX = result.getBlockHit().hitVec.x;
            collidePosY = result.getBlockHit().hitVec.y;
            collidePosZ = result.getBlockHit().hitVec.z;
        }else {
            collidePosX = 30;
            collidePosY = 30;
            collidePosZ = 30;
        }
        List<T> entities = world.getEntitiesWithinAABB(classEntity, new AxisAlignedBB(Math.min(from.x, collidePosX), Math.min(from.y, collidePosY), Math.min(from.z, collidePosZ), Math.max(from.x, collidePosX), Math.max(from.y, collidePosY), Math.max(from.z, collidePosZ)).grow(1, 1, 1));
        for (T entity : entities) {
            float pad = entity.getCollisionBorderSize() + 0.5f;
            AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(pad, pad, pad);
            RayTraceResult hit = aabb.calculateIntercept(from, to);
            if (aabb.contains(from)) {
                result.addEntityHit(entity);
            } else if (hit != null) {
                result.addEntityHit(entity);
            }
        }
        return result;
	}

	public static class EntityRayResult<T extends Entity> {
        private RayTraceResult blockHit;

        private List<T> entities = new ArrayList<>();

        public RayTraceResult getBlockHit() {
            return blockHit;
        }

        public void setBlockHit(RayTraceResult blockHit) {
            this.blockHit = blockHit;
        }

        public void addEntityHit(T entity) {
            entities.add(entity);
        }
        
        public List<T> getEntityHits() {
        	return Collections.unmodifiableList(this.entities);
        }
	}
	
	@EventBusSubscriber(modid = OrmoyoUtil.MODID)
	private static class EventHandler {
		@SubscribeEvent(priority = EventPriority.LOW)
		public static void onPlayerTickEvent(TickEvent.WorldTickEvent event) {
			if(event.phase == Phase.END) {
				for(Iterator<ITick> iterator = tickables.iterator(); iterator.hasNext();) {
				    ITick tickable = iterator.next();
				    tickable.onUpdate(iterator);
				}
				for(Iterator<Animation> iterator = animations.iterator(); iterator.hasNext();) {
					Animation anim = iterator.next();
					anim.onUpdate(iterator);
				}
			}
		}
		
		@SubscribeEvent(priority = EventPriority.LOW)
		@SideOnly(Side.CLIENT)
		public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
			for(Animation animation : animations) {
				animation.renderPreOverlay(event);
			}
		}
		
		@SubscribeEvent(priority = EventPriority.LOW)
		@SideOnly(Side.CLIENT)
		public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
			for(Animation animation : animations) {
				animation.renderPostOverlay(event);
			}
		}
		
		@SubscribeEvent(priority = EventPriority.LOW)
		@SideOnly(Side.CLIENT)
		public static void onRenderWorld(RenderWorldLastEvent event) {
			for(Animation animation : animations) {
				animation.renderWorld(event);
			}
		}
		
		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public static void onDisconnectionFromServer(ClientDisconnectionFromServerEvent event) {
			tickables.clear();
			animations.clear();
		}
	}
}
