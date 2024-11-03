package fr.cyberdodo.blockDisplayExecutor.commands;

import fr.cyberdodo.blockDisplayExecutor.BlockDisplayExecutor;
import fr.cyberdodo.blockDisplayExecutor.utils.FunctionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.List;

public class FunctionCommandExecutor implements CommandExecutor {

    private final BlockDisplayExecutor plugin;
    private final FunctionUtils functionUtils;

    public FunctionCommandExecutor(BlockDisplayExecutor plugin) {
        this.plugin = plugin;
        this.functionUtils = plugin.getFunctionUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (args.length == 0) {
            // Liste les fonctions disponibles
            List<String> availableFunctions = functionUtils.getAvailableFunctions();
            if (availableFunctions.isEmpty()) {
                sender.sendMessage("§cAucun objet disponible.");
            } else {
                sender.sendMessage("§aObjets disponibles :");
                for (String func : availableFunctions) {
                    sender.sendMessage(" - " + func);
                }
            }
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /executeFunction <nomDeLaFonction>");
            return true;
        }

        String functionName = args[0];
        List<String> commands;
        try {
            commands = functionUtils.loadFunctionCommands(functionName);
        } catch (IOException e) {
            sender.sendMessage("§cErreur lors du chargement de l'objet : " + e.getMessage());
            return true;
        }

        sender.sendMessage("§aCréation de l'objet : " + functionName);

        // Exécute les commandes de manière synchrone avec un léger délai pour éviter les lags
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= commands.size()) {
                    cancel();
                    sender.sendMessage("§aCréation de l'objet terminée.");
                    return;
                }

                String cmd = commands.get(index);
                String processedCmd = cmd;

                // Gère le sélecteur @s
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    processedCmd = processedCmd.replace("@s", player.getName());
                } else {
                    processedCmd = processedCmd.replace("@s", "CONSOLE");
                }

                // Exécute la commande depuis la console
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd);
                if (!success) {
                    sender.sendMessage("§cÉchec de l'exécution de la commande : " + processedCmd);
                }

                index++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Exécute les commandes toutes les ticks (20 ticks par seconde)

        return true;
    }
}