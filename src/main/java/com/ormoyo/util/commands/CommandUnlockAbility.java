package com.ormoyo.util.commands;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.ormoyo.util.OrmoyoUtil;
import com.ormoyo.util.abilities.Ability;
import com.ormoyo.util.abilities.AbilityEntry;
import com.ormoyo.util.capability.CapabilityHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandUnlockAbility extends CommandBase {
	@Override
	public String getName() {
		return "unlockability";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/unlockability <Ability> [Player]";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		if(args.length == 1) {
			Set<ResourceLocation> set = Sets.newHashSet();
			for(AbilityEntry entry : Ability.getRegistry().getValuesCollection()) {
				try {
					Ability ability = entry.newInstance(CommandBase.getCommandSenderAsPlayer(sender));
					if(ability.isVisable()) {
						set.add(entry.getRegistryName());
					}
				} catch (PlayerNotFoundException e) {
					e.printStackTrace();
				}
				
			}
			return CommandBase.getListOfStringsMatchingLastWord(args, set);
		}
		if(args.length == 2) {
			return CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		}
		return Collections.emptyList();
	}
	
	private static String LANG_PREFIX = "command." + OrmoyoUtil.MODID + ".";

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length >= 1) {
			Ability ability = this.getAbilityByName(server, sender, args[0]);
			if(ability != null && ability.isVisable()) {
				TextComponentString abilityTranslation = new TextComponentString(ability.getTranslatedName().getFormattedText().toLowerCase());
				if(args.length == 2) {
					EntityPlayer player = CommandBase.getPlayer(server, sender, args[1]);
					if(player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null).isAbilityUnlocked(ability)) {
						sender.getCommandSenderEntity().sendMessage(new TextComponentTranslation(LANG_PREFIX + "already_unlocked"));
						return;
					}
					if(player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null).UnlockAbility(ability)) {
						sender.getCommandSenderEntity().sendMessage(new TextComponentString(String.format("The player %1$s unlocked the ability %2$s", player.getName(), abilityTranslation.getFormattedText())));
						player.sendMessage(new TextComponentString(String.format("You unlocked the ablity %s", abilityTranslation.getFormattedText())));
					}
				}else {
					if(sender.getCommandSenderEntity().getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null).isAbilityUnlocked(ability)) {
						sender.getCommandSenderEntity().sendMessage(new TextComponentTranslation(LANG_PREFIX + "already_unlocked"));
						return;
					}
					if(sender.getCommandSenderEntity().getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null).UnlockAbility(ability)) {
						sender.getCommandSenderEntity().sendMessage(new TextComponentString(String.format("You unlocked the ablity %s", abilityTranslation.getFormattedText())));
					}
				}
			}else {
				sender.getCommandSenderEntity().sendMessage(new TextComponentTranslation(LANG_PREFIX + "ability_fail"));
			}
		}else {
			throw new WrongUsageException(this.getUsage(sender));
		}
	}
	
	private Ability getAbilityByName(MinecraftServer server, ICommandSender sender, String argument) {
		for(ResourceLocation location : Ability.getRegistry().getKeys()) {
			if(location.equals(new ResourceLocation(argument))) {
				try {
					return Ability.getRegistry().getValue(new ResourceLocation(argument)).newInstance(CommandBase.getCommandSenderAsPlayer(sender));
				} catch (PlayerNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
