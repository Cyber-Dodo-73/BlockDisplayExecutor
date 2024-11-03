package fr.cyberdodo.blockDisplayExecutor.commands;

import fr.cyberdodo.blockDisplayExecutor.BlockDisplayExecutor;
import fr.cyberdodo.blockDisplayExecutor.utils.FunctionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class GiveCustomEggCommand implements CommandExecutor {

    public static final String CUSTOM_EGG_NAME_PREFIX = ChatColor.GOLD + "Structure : ";

    private final BlockDisplayExecutor plugin;
    private final FunctionUtils functionUtils;

    public GiveCustomEggCommand(BlockDisplayExecutor plugin) {
        this.plugin = plugin;
        this.functionUtils = plugin.getFunctionUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (args.length == 0) {
            // Liste les fonctions disponibles
            List<String> availableFunctions = functionUtils.getAvailableFunctions();
            if (availableFunctions.isEmpty()) {
                sender.sendMessage("§cAucune fonction disponible.");
            } else {
                sender.sendMessage("§aFonctions disponibles :");
                for (String func : availableFunctions) {
                    sender.sendMessage(" - " + func);
                }
            }
            return true;
        }

        if (args.length > 2) {
            sender.sendMessage("§cUsage: /giveCustomEgg <nomDeLaFonction> [joueur]");
            return true;
        }

        String functionName = args[0];
        Player targetPlayer;

        if (args.length == 2) {
            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage("§cLe joueur spécifié est introuvable.");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cSeuls les joueurs peuvent recevoir l'œuf personnalisé.");
                return true;
            }
            targetPlayer = (Player) sender;
        }

        // Vérifie si la fonction existe
        if (!functionUtils.functionExists(functionName)) {
            sender.sendMessage("§cLa fonction spécifiée n'existe pas.");
            return true;
        }

        ItemStack egg = createFunctionEgg(functionName);
        targetPlayer.getInventory().addItem(egg);

        if (targetPlayer.equals(sender)) {
            sender.sendMessage(ChatColor.GREEN + "Vous avez reçu l'objet : '" + functionName + "' !");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Vous avez donné l'objet "+ functionName +" à " + targetPlayer.getName() + " !");
            targetPlayer.sendMessage(ChatColor.GREEN + "Vous avez reçu l'objet '" + functionName + "' !");
        }

        return true;
    }

    public ItemStack createFunctionEgg(String functionName) {
        ItemStack egg = new ItemStack(Material.STRUCTURE_VOID); // Utilisez n'importe quel œuf ou objet
        ItemMeta meta = egg.getItemMeta();
        meta.setDisplayName(CUSTOM_EGG_NAME_PREFIX + ChatColor.YELLOW + functionName);
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "Placez-le pour faire apparaître la structure."));
        // Stocke le nom de la fonction dans les métadonnées
        meta.getPersistentDataContainer().set(functionUtils.getFunctionKey(), PersistentDataType.STRING, functionName);
        egg.setItemMeta(meta);
        return egg;
    }
}
