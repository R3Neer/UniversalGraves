package eu.pb4.graves.other;


import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.graves.GenericModInfo;
import eu.pb4.graves.config.ConfigManager;
import eu.pb4.graves.ui.AllGraveListGui;
import eu.pb4.graves.ui.GraveListGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Commands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("graves")
                            .requires(Permissions.require("universal_graves.list", true))
                            .executes((ctx) -> Commands.list(ctx, false))
                            .then(literal("modify")
                                    .requires(Permissions.require("universal_graves.modify", 3))
                                    .executes((ctx) -> Commands.list(ctx, true))
                            )

                            .then(literal("player")
                                    .requires(Permissions.require("universal_graves.list_others", 3))
                                    .then(argument("player", GameProfileArgument.gameProfile())
                                            .executes((ctx) -> Commands.listOthers(ctx, false))
                                            .then(literal("modify")
                                                    .requires(Permissions.require("universal_graves.list_others.modify", 3))
                                                    .executes((ctx) -> Commands.listOthers(ctx, true))
                                            )
                                    ))

                            .then(literal("all")
                                    .requires(Permissions.require("universal_graves.list_others", 3))
                                    .executes((ctx) -> Commands.listAll(ctx, false))
                                    .then(literal("modify")
                                                    .requires(Permissions.require("universal_graves.list_others.modify", 3))
                                                    .executes((ctx) -> Commands.listAll(ctx, true))
                                            )
                            )

                            .then(literal("about").executes(Commands::about))

                            .then(literal("reload")
                                    .requires(Permissions.require("universal_graves.reload", 4))
                                    .executes(Commands::reloadConfig)
                            )
            );
        });
    }

    private static int list(CommandContext<CommandSourceStack> context, boolean canModify) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        try {
            new GraveListGui(player, player.nameAndId(), canModify, Permissions.check(context.getSource(), "universal_graves.fetch_grave", 3)).open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int listOthers(CommandContext<CommandSourceStack> context, boolean canModify) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        var profiles = new ArrayList<>(context.getArgument("player", GameProfileArgument.Result.class).getNames(context.getSource()));

        if (profiles.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("This player doesn't exist!"), false);
            return 0;
        } else if (profiles.size() > 1) {
            context.getSource().sendSuccess(() -> Component.literal("Only one player can be selected!"), false);
            return 0;
        }
        try {
            new GraveListGui(player, profiles.getFirst(), canModify, canModify && Permissions.check(context.getSource(), "universal_graves.fetch_grave.others", 3)).open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static int listAll(CommandContext<CommandSourceStack> context, boolean canModify) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        try {
            new AllGraveListGui(player, canModify, canModify && Permissions.check(context.getSource(), "universal_graves.fetch_grave.others", 3)).open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        if (ConfigManager.loadConfig(context.getSource().registryAccess())) {
            context.getSource().sendSuccess(() -> Component.literal("Reloaded config!"), false);
        } else {
            context.getSource().sendFailure(Component.literal("Error occurred while reloading config!").withStyle(ChatFormatting.RED));
        }
        return 1;
    }

    private static int about(CommandContext<CommandSourceStack> context) {
        for (var text : context.getSource().getEntity() instanceof ServerPlayer ? GenericModInfo.getAboutFull() : GenericModInfo.getAboutConsole()) {
            context.getSource().sendSuccess(() -> text, false);
        }

        return 1;
    }
}
