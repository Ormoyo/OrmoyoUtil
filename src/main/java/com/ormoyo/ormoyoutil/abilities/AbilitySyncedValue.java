package com.ormoyo.ormoyoutil.abilities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.event.AbilityEvent.AbilityGetSyncedValueEvent;
import com.ormoyo.ormoyoutil.network.MessageGetAbilitySyncedValueOnClient;
import com.ormoyo.ormoyoutil.network.MessageGetAbilitySyncedValueOnServer;
import com.ormoyo.ormoyoutil.network.MessageInvokeMethodOnClient;
import com.ormoyo.ormoyoutil.network.MessageInvokeMethodOnServer;
import com.ormoyo.ormoyoutil.network.MessageUpdateAbilitySyncedValueOnClient;
import com.ormoyo.ormoyoutil.network.MessageUpdateAbilitySyncedValueOnServer;
import com.ormoyo.ormoyoutil.util.Utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

/**
 * SyncedValue is used for syncing the server and the client values for abilities
 * @apiNote You shouldn't use this too much because it's using reflection
 */
public class AbilitySyncedValue {
	private static Map<Class<?>, AbilitySyncedValueParserEntry> entries = Maps.newHashMap();
	private static IForgeRegistry<AbilitySyncedValueParserEntry> PARSER_REGISTRY;
	public static IForgeRegistry<AbilitySyncedValueParserEntry> getRegistry() {
		return PARSER_REGISTRY;
	}
	
	/**
	 * Sets the ability field value on the opposite side(If you're on server side this will change the ability client counterpart field value, and same otherwise)
	 * @param ability The ability to be set the value on
	 * @param fieldName The value field name
	 * @param value The value to be set on the opposite side
	 */
	public static<T> void setValue(Ability ability, String fieldName, T value) {
		if(ability.getOwner() == null) return;
		if(ability.getOwner().getEntityWorld().isRemote) {
			OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageUpdateAbilitySyncedValueOnServer(ability, fieldName, value, ability.getOwner()));
		}else {
			OrmoyoUtil.NETWORK_WRAPPER.sendTo(new MessageUpdateAbilitySyncedValueOnClient(ability, fieldName, value, ability.getOwner()), (EntityPlayerMP) ability.getOwner());
		}
	}
	
	/**
	 * Sets the ability field value on the opposite side(If you're on server side this will change the ability client counterpart field value, and same otherwise)
	 * @param ability The ability to be set the value on
	 * @param superClass The super class of the ability if you want to change a field on the super class value for the ability
	 * @param fieldName The value field name
	 * @param value The value to be set on the opposite side
	 */
	public static<T> void setValue(Ability ability, Class<? extends Ability> superClass, String fieldName, T value) {
		if(ability.getOwner() == null) return;
		if(ability.getOwner().getEntityWorld().isRemote) {
			OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageUpdateAbilitySyncedValueOnServer(ability, superClass, fieldName, value, ability.getOwner()));
		}else {
			OrmoyoUtil.NETWORK_WRAPPER.sendTo(new MessageUpdateAbilitySyncedValueOnClient(ability, superClass, fieldName, value, ability.getOwner()), (EntityPlayerMP) ability.getOwner());
		}
	}
	
	
	/**
	 * Gets the ability field value on the opposite side(If you're on server side this will get the ability client counterpart field value, and same otherwise) and to get the field a {@link AbilityGetSyncedValueEvent} will be fired
	 * @param ability The ability to get the value from
	 * @param fieldName The value field name
	 * @return A uuid to be used on the {@link AbilityGetSyncedValueEvent}
	 */
	public static UUID getValue(Ability ability, String fieldName) {
		if(ability.getOwner() == null) return null;
		UUID id = UUID.randomUUID();
		if(ability.getOwner().getEntityWorld().isRemote) {
			OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageGetAbilitySyncedValueOnServer(ability, fieldName, id, ability.getOwner()));
		}else {
			OrmoyoUtil.NETWORK_WRAPPER.sendTo(new MessageGetAbilitySyncedValueOnClient(ability, fieldName, id, ability.getOwner()), (EntityPlayerMP) ability.getOwner());
		}
		return id;
	}
	
	/**
	 * Gets the ability field value on the opposite side(If you're on server side this will get the ability client counterpart field value, and same otherwise) and to get the field a {@link AbilityGetSyncedValueEvent} will be fired
	 * @param ability The ability to get the value from
	 * @param fieldName The value field name
	 * @param id The id to be used on the {@link AbilityGetSyncedValueEvent}
	 */
	public static void getValue(Ability ability, String fieldName, UUID id) {
		if(ability.getOwner() == null) return;
		if(ability.getOwner().getEntityWorld().isRemote) {
			OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageGetAbilitySyncedValueOnServer(ability, fieldName, id, ability.getOwner()));
		}else {
			OrmoyoUtil.NETWORK_WRAPPER.sendTo(new MessageGetAbilitySyncedValueOnClient(ability, fieldName, id, ability.getOwner()), (EntityPlayerMP) ability.getOwner());
		}
	}
	
	/**
	 * Gets the ability field value on the opposite side(If you're on server side this will get the ability client counterpart field value, and same otherwise) and to get the field a {@link AbilityGetSyncedValueEvent} will be fired
	 * @param ability The ability to get the value from
	 * @param superClass The super class of the ability if you want to get a field on the super class value for the ability
	 * @param fieldName The value field name
	 * @return A uuid to be used on the {@link AbilityGetSyncedValueEvent}
	 */
	public static UUID getValue(Ability ability, Class<? extends Ability> superClass, String fieldName) {
		if(ability.getOwner() == null) return null;
		UUID id = UUID.randomUUID();
		if(ability.getOwner().getEntityWorld().isRemote) {
			OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageGetAbilitySyncedValueOnServer(ability, superClass, fieldName, id, ability.getOwner()));
		}else {
			OrmoyoUtil.NETWORK_WRAPPER.sendTo(new MessageGetAbilitySyncedValueOnClient(ability, superClass, fieldName, id, ability.getOwner()), (EntityPlayerMP) ability.getOwner());
		}
		return id;
	}
	
	/**
	 * Gets the ability field value on the opposite side(If you're on server side this will get the ability client counterpart field value, and same otherwise) and to get the field a {@link AbilityGetSyncedValueEvent} will be fired
	 * @param ability The ability to get the value from
	 * @param superClass The super class of the ability if you want to get a field on the super class value for the ability
	 * @param fieldName The value field name
	 * @param id The id to be used on the {@link AbilityGetSyncedValueEvent}
	 */
	public static void getValue(Ability ability, Class<? extends Ability> superClass, String fieldName, UUID id) {
		if(ability.getOwner() == null) return;
		if(ability.getOwner().getEntityWorld().isRemote) {
			OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageGetAbilitySyncedValueOnServer(ability, superClass, fieldName, id, ability.getOwner()));
		}else {
			OrmoyoUtil.NETWORK_WRAPPER.sendTo(new MessageGetAbilitySyncedValueOnClient(ability, superClass, fieldName, id, ability.getOwner()), (EntityPlayerMP) ability.getOwner());
		}
	}
	
	/**
	 * Invoke a method on the opposite side(If you're on server side this will invoke a method on the ability client counterpart, and same otherwise)
	 * @param ability The ability to invoke the method on
	 * @param methodName The method name
	 * @param methodArgs The method args
	 */
	public static void invokeMethod(Ability ability, String methodName, Object...methodArgs) {
		if(ability.getOwner().getEntityWorld().isRemote) {
			OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageInvokeMethodOnServer(ability, methodName, ability.getOwner(), methodArgs));
		}else {
			OrmoyoUtil.NETWORK_WRAPPER.sendTo(new MessageInvokeMethodOnClient(ability, methodName, ability.getOwner(), methodArgs), (EntityPlayerMP) ability.getOwner());
		}
	}
	
	public static Map<Class<?>, AbilitySyncedValueParserEntry> getEntries() {
		return entries;
	}
	
	public static interface ISyncedValueParser<T> {
		void write(Writer writer, EntityPlayer player, T value);
		T read(Reader reader, EntityPlayer player);
		
		/**
		 * Called when the server writes the synced value to the client
		 * @param server The server
		 * @param writer The writer
		 * @param player The player
		 * @param value The value
		 */
		default void writeToClient(MinecraftServer server, Writer writer, EntityPlayer player, T value) {}
		/**
		 * Called when the client writes the synced value to the server
		 * @param client The client
		 * @param writer The writer
		 * @param player The player
		 * @param value The value
		 */
		@SideOnly(Side.CLIENT)
		default void writeToServer(Minecraft client, Writer writer, EntityPlayer player, T value) {}
		
		/**
		 * If you read to the server you need to write to the server
		 * @param server The server
		 * @param reader The reader
		 * @param player TODO
		 */
		default T readToServer(MinecraftServer server, Reader reader, EntityPlayer player) {return null;}
		
		/**
		 * If you read to the client you need to write to the client
		 * @param client The client
		 * @param reader The reader
		 * @param player TODO
		 */
		@SideOnly(Side.CLIENT)
		default T readToClient(Minecraft client, Reader reader, EntityPlayer player) {return null;}
		
		public static class Writer {
			private ByteBuf buffer;
			public Writer(ByteBuf buffer) {
				this.buffer = buffer;
			}
			
			public void writeString(String s) {
				ByteBufUtils.writeUTF8String(buffer, s);
			}
			
			public void writeInt(int i) {
				buffer.writeInt(i);
			}
			
			public void writeDouble(double d) {
				buffer.writeDouble(d);
			}
			
			public void writeFloat(float f) {
				buffer.writeFloat(f);
			}
			
			public void writeShort(short s) {
				buffer.writeShort(s);
			}
			
			public void writeLong(long l) {
				buffer.writeLong(l);
			}
			
			public void writeBoolean(boolean b) {
				buffer.writeBoolean(b);
			}
		}
		
		public static class Reader {
			private ByteBuf buffer;
			public Reader(ByteBuf buffer) {
				this.buffer = buffer;
			}
			
			public String readString() {
				return ByteBufUtils.readUTF8String(buffer);
			}
			
			public int readInt() {
				return buffer.readInt();
			}
			
			public double readDouble() {
				return buffer.readDouble();
			}
			
			public float readFloat() {
				return buffer.readFloat();
			}
			
			public short readShort() {
				return buffer.readShort();
			}
			
			public long readLong() {
				return buffer.readLong();
			}
			
			public boolean readBoolean() {
				return buffer.readBoolean();
			}
		}
	}
	
	public static class Parsers {
		public static final ISyncedValueParser<String> STRING = new ISyncedValueParser<String>() {
			@Override
			public void write(Writer writer, EntityPlayer player, String value) {
				writer.writeString(value);
			}

			@Override
			public String read(Reader reader, EntityPlayer player) {
				return reader.readString();
			}
		};
		
		public static final ISyncedValueParser<Integer> INTEGER = new ISyncedValueParser<Integer>() {
			@Override
			public void write(Writer writer, EntityPlayer player, Integer value) {
				writer.writeInt(value);
			}

			@Override
			public Integer read(Reader reader, EntityPlayer player) {
				return reader.readInt();
			}
		};
		
		public static final ISyncedValueParser<Double> DOUBLE = new ISyncedValueParser<Double>() {
			@Override
			public void write(Writer writer, EntityPlayer player, Double value) {
				writer.writeDouble(value);
			}

			@Override
			public Double read(Reader reader, EntityPlayer player) {
				return reader.readDouble();
			}
		};
		
		public static final ISyncedValueParser<Float> FLOAT = new ISyncedValueParser<Float>() {
			@Override
			public void write(Writer writer, EntityPlayer player, Float value) {
				writer.writeFloat(value);
			}

			@Override
			public Float read(Reader reader, EntityPlayer player) {
				return reader.readFloat();
			}
		};
		
		public static final ISyncedValueParser<Short> SHORT = new ISyncedValueParser<Short>() {
			@Override
			public void write(Writer writer, EntityPlayer player, Short value) {
				writer.writeShort(value);
			}

			@Override
			public Short read(Reader reader, EntityPlayer player) {
				return reader.readShort();
			}
		};
		
		public static final ISyncedValueParser<Long> LONG = new ISyncedValueParser<Long>() {
			@Override
			public void write(Writer writer, EntityPlayer player, Long value) {
				writer.writeLong(value);
			}

			@Override
			public Long read(Reader reader, EntityPlayer player) {
				return reader.readLong();
			}
		};
		
		public static final ISyncedValueParser<Boolean> BOOLEAN = new ISyncedValueParser<Boolean>() {
			@Override
			public void write(Writer writer, EntityPlayer player, Boolean value) {
				writer.writeBoolean(value);
			}

			@Override
			public Boolean read(Reader reader, EntityPlayer player) {
				return reader.readBoolean();
			}
		};
		
		@SuppressWarnings("rawtypes")
		public static final ISyncedValueParser<Enum> ENUM = new ISyncedValueParser<Enum>() {
			@Override
			public void write(Writer writer, EntityPlayer player, Enum value) {
				writer.writeString(value.getDeclaringClass().getName());
				writer.writeString(value.name());
			}

			@SuppressWarnings("unchecked")
			@Override
			public Enum<?> read(Reader reader, EntityPlayer player) {
				try {
					return Enum.valueOf((Class<? extends Enum>) Class.forName(reader.readString()), reader.readString());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		
		public static final ISyncedValueParser<Entity> ENTITY = new ISyncedValueParser<Entity>() {
			@Override
			public void write(Writer writer, EntityPlayer player, Entity value) {
				writer.writeInt(value.getEntityId());
			}

			@Override
			public Entity read(Reader reader, EntityPlayer player) {
				return player.world.getEntityByID(reader.readInt());
			}
		};
		
		public static final ISyncedValueParser<ResourceLocation> RESOURCE_LOCATION = new ISyncedValueParser<ResourceLocation>() {
			@Override
			public void write(Writer writer, EntityPlayer player, ResourceLocation value) {
				writer.writeString(value.getResourceDomain());
				writer.writeString(value.getResourcePath());
			}

			@Override
			public ResourceLocation read(Reader reader, EntityPlayer player) {
				return new ResourceLocation(reader.readString(), reader.readString());
			}
		};
		
		public static final ISyncedValueParser<Vec3d> VEC3D = new ISyncedValueParser<Vec3d>() {
			@Override
			public void write(Writer writer, EntityPlayer player, Vec3d value) {
				writer.writeDouble(value.x);
				writer.writeDouble(value.y);
				writer.writeDouble(value.z);
			}

			@Override
			public Vec3d read(Reader reader, EntityPlayer player) {
				return new Vec3d(reader.readDouble(), reader.readDouble(), reader.readDouble());
			}
		};
		
		public static final ISyncedValueParser<Vec3i> VEC3I = new ISyncedValueParser<Vec3i>() {
			@Override
			public void write(Writer writer, EntityPlayer player, Vec3i value) {
				writer.writeInt(value.getX());
				writer.writeInt(value.getY());
				writer.writeInt(value.getZ());
			}

			@Override
			public Vec3i read(Reader reader, EntityPlayer player) {
				return new Vec3i(reader.readInt(), reader.readInt(), reader.readInt());
			}
		};
		
		public static final ISyncedValueParser<DamageSource> DAMAGE_SOURCE = new ISyncedValueParser<DamageSource>() {
			@Override
			public void write(Writer writer, EntityPlayer player, DamageSource value) {
				writer.writeString(value.damageType);
				writer.writeBoolean(value.isDifficultyScaled());
				writer.writeBoolean(value.isFireDamage());
				writer.writeBoolean(value.isUnblockable());
				writer.writeBoolean(value.isMagicDamage());
				writer.writeBoolean(value.isExplosion());
				writer.writeBoolean(value.isProjectile());
				writer.writeBoolean(value.isDamageAbsolute());
				writer.writeBoolean(value.canHarmInCreative());
				if(value instanceof EntityDamageSource) {
					if(value.getTrueSource() != null) {
						writer.writeBoolean(true);
						ENTITY.write(writer, player, value.getTrueSource());
						if(value instanceof EntityDamageSourceIndirect) {
							if(value.getImmediateSource() != null) {
								writer.writeBoolean(true);
								ENTITY.write(writer, player, value.getImmediateSource());
							}else {
								writer.writeBoolean(false);
							}
						}else {
							writer.writeBoolean(false);
						}
					}else {
						writer.writeBoolean(false);
					}
				}else {
					writer.writeBoolean(false);
				}
			}

			@Override
			public DamageSource read(Reader reader, EntityPlayer player) {
				DamageSource source = new DamageSource(reader.readString());
				if(reader.readBoolean()) {
					source.setDifficultyScaled();
				}
				if(reader.readBoolean()) {
					source.setFireDamage();
				}
				if(reader.readBoolean()) {
					source.setDamageBypassesArmor();
				}
				if(reader.readBoolean()) {
					source.setMagicDamage();
				}
				if(reader.readBoolean()) {
					source.setExplosion();
				}
				if(reader.readBoolean()) {
					source.setProjectile();
				}
				if(reader.readBoolean()) {
					source.setDamageIsAbsolute();
				}
				if(reader.readBoolean()) {
					source.setDamageAllowedInCreativeMode();
				}
				if(reader.readBoolean()) {
					EntityDamageSource entityDamage = (EntityDamageSource) Utils.copyDamageSource(source, new EntityDamageSource(source.damageType, ENTITY.read(reader, player)));
					if(reader.readBoolean()) {
						Entity indirectEntity = ENTITY.read(reader, player);
						entityDamage = new EntityDamageSourceIndirect(entityDamage.damageType, entityDamage.getTrueSource(), indirectEntity);
					}
					return entityDamage;
				}
				return source;
			}
		};
		
		public static final ISyncedValueParser<java.util.UUID> UUID = new ISyncedValueParser<java.util.UUID>() {
			@Override
			public void write(Writer writer, EntityPlayer player, java.util.UUID value) {
				writer.writeString(value.toString());
			}

			@Override
			public java.util.UUID read(Reader reader, EntityPlayer player) {
				return java.util.UUID.fromString(reader.readString());
			}
		};
	}
	
	public static class AbilitySyncedValueParserEntry extends Impl<AbilitySyncedValueParserEntry> {
		private final ISyncedValueParser<?> parser;
		
		public<T> AbilitySyncedValueParserEntry(Class<T> clazz, ISyncedValueParser<T> parser, ResourceLocation name) {
			this.parser = parser;
			entries.put(clazz, this);
			this.setRegistryName(name);
		}
		
		public<T> AbilitySyncedValueParserEntry(Class<T> clazz, ISyncedValueParser<T> parser, String name) {
			this.parser = parser;
			entries.put(clazz, this);
			this.setRegistryName(name);
		}
		
		public ISyncedValueParser<?> getParser() {
			return this.parser;
		}
	}
	
	/**
	 * If a field has this annotation only the server can change the field value through {@link AbilitySyncedValue} for the client
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface OnlyChangableForServer {}
	/**
	 * If a field has this annotation only the client can change the field value through {@link AbilitySyncedValue} for the server
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface OnlyChangableForClient {}
	/**
	 * If a field has this annotation {@link AbilitySyncedValue} can change his value
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface ChangeableValue {}
	/**
	 * If a method has this annotation only the server can invoke the method through {@link AbilitySyncedValue} for the client
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface OnlyInvokableForServer {}
	/**
	 * If a method has this annotation only the client can invoke the method through {@link AbilitySyncedValue} for the server
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface OnlyInvokableForClient {}
	/**
	 * If a method has this annotation {@link AbilitySyncedValue} can invoke the method
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface InvokableMethod {}
	
	@EventBusSubscriber(modid = OrmoyoUtil.MODID)
	private static class EventHandler {
		@SubscribeEvent
		public static void onNewRegistry(RegistryEvent.NewRegistry event) {
			PARSER_REGISTRY = new RegistryBuilder<AbilitySyncedValueParserEntry>().setName(new ResourceLocation(OrmoyoUtil.MODID, "synced_value")).setType(AbilitySyncedValueParserEntry.class).setIDRange(0, 2048).create();
		}
		
		@SubscribeEvent
		public static void registerSyncedValues(RegistryEvent.Register<AbilitySyncedValueParserEntry> event) {
			event.getRegistry().register(new AbilitySyncedValueParserEntry(String.class, Parsers.STRING, new ResourceLocation(OrmoyoUtil.MODID, "string")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(Integer.class, Parsers.INTEGER, new ResourceLocation(OrmoyoUtil.MODID, "integer")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(Double.class, Parsers.DOUBLE, new ResourceLocation(OrmoyoUtil.MODID, "double")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(Float.class, Parsers.FLOAT, new ResourceLocation(OrmoyoUtil.MODID, "float")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(Short.class, Parsers.SHORT, new ResourceLocation(OrmoyoUtil.MODID, "short")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(Long.class, Parsers.LONG, new ResourceLocation(OrmoyoUtil.MODID, "long")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(Boolean.class, Parsers.BOOLEAN, new ResourceLocation(OrmoyoUtil.MODID, "boolean")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(Enum.class, Parsers.ENUM, new ResourceLocation(OrmoyoUtil.MODID, "enum")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(Entity.class, Parsers.ENTITY, new ResourceLocation(OrmoyoUtil.MODID, "entity")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(ResourceLocation.class, Parsers.RESOURCE_LOCATION, new ResourceLocation(OrmoyoUtil.MODID, "resource_location")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(Vec3d.class, Parsers.VEC3D, new ResourceLocation(OrmoyoUtil.MODID, "vec3d")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(Vec3i.class, Parsers.VEC3I, new ResourceLocation(OrmoyoUtil.MODID, "vec3i")));
			event.getRegistry().register(new AbilitySyncedValueParserEntry(DamageSource.class, Parsers.DAMAGE_SOURCE, new ResourceLocation(OrmoyoUtil.MODID, "damage_source")));
		}
	}
}
