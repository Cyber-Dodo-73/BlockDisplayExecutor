package fr.cyberdodo.blockDisplayExecutor.listeners;

import fr.cyberdodo.blockDisplayExecutor.BlockDisplayExecutor;
import fr.cyberdodo.blockDisplayExecutor.commands.GiveCustomEggCommand;
import fr.cyberdodo.blockDisplayExecutor.utils.FunctionUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.*;

public class EggPlaceListener implements Listener {

    private final BlockDisplayExecutor plugin;
    private final FunctionUtils functionUtils;
    private static final String CUSTOM_EGG_NAME_PREFIX = GiveCustomEggCommand.CUSTOM_EGG_NAME_PREFIX;

    public EggPlaceListener(BlockDisplayExecutor plugin) {
        this.plugin = plugin;
        this.functionUtils = plugin.getFunctionUtils();
    }

    @EventHandler
    public void onPlayerUseEgg(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName() && meta.getDisplayName().startsWith(CUSTOM_EGG_NAME_PREFIX)) {
                    event.setCancelled(true); // Empêche le placement par défaut de l'œuf

                    Player player = event.getPlayer();
                    Block clickedBlock = event.getClickedBlock();
                    Location spawnLocation = clickedBlock.getRelative(event.getBlockFace()).getLocation().add(0.5, 0, 0.5);

                    // Récupère le nom de la fonction depuis les métadonnées
                    String functionName = meta.getPersistentDataContainer().get(functionUtils.getFunctionKey(), PersistentDataType.STRING);
                    if (functionName == null || functionName.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "L'œuf ne contient pas de fonction valide.");
                        return;
                    }

                    // Vérifie si la fonction existe
                    if (!functionUtils.functionExists(functionName)) {
                        player.sendMessage(ChatColor.RED + "La fonction '" + functionName + "' n'existe pas.");
                        return;
                    }

                    // Fait apparaître un armor stand invisible
                    ArmorStand armorStand = spawnInvisibleArmorStand(spawnLocation, functionName);

                    // Exécute la fonction
                    UUID armorStandUUID = armorStand.getUniqueId();
                    executeFunctionAtLocation(functionName, spawnLocation, armorStandUUID);

                    // Réduit le nombre d'œufs
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        ItemStack itemInHand = event.getItem();
                        itemInHand.setAmount(itemInHand.getAmount() - 1);
                    }
                }
            }
        }
    }

    private ArmorStand spawnInvisibleArmorStand(Location location, String functionName) {
        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setMarker(false); // Mettre à true si vous ne voulez pas de hitbox
        armorStand.setGravity(false);
        armorStand.setInvulnerable(false);
        armorStand.setCustomName("FunctionArmorStand");
        armorStand.setCustomNameVisible(false);
        armorStand.addScoreboardTag("FunctionArmorStand");

        // Stocker le nom de la fonction dans le PersistentDataContainer de l'armor stand
        armorStand.getPersistentDataContainer().set(functionUtils.getFunctionKey(), PersistentDataType.STRING, functionName);

        return armorStand;
    }

    private void executeFunctionAtLocation(String functionName, Location location, UUID armorStandUUID) {
        List<String> commands;
        try {
            commands = functionUtils.loadFunctionCommands(functionName);
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors du chargement de l'objet : " + e.getMessage());
            return;
        }

        // Remplacer les placeholders
        List<String> processedCommands = new ArrayList<>();
        for (String cmd : commands) {
            String processedCmd = cmd
                    .replace("{armorStandUUID}", armorStandUUID.toString())
                    .replace("{x}", String.valueOf(location.getX()))
                    .replace("{y}", String.valueOf(location.getY()))
                    .replace("{z}", String.valueOf(location.getZ()));
            processedCommands.add(processedCmd);
        }

        // Exécute les commandes
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= processedCommands.size()) {
                    cancel();
                    return;
                }

                String cmd = processedCommands.get(index);

                // Exécute la commande depuis la console
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                if (!success) {
                    plugin.getLogger().warning("Échec de l'exécution de la commande : " + cmd);
                }

                index++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}