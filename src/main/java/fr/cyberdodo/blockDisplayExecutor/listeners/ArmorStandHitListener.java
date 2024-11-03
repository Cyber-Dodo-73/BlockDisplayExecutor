package fr.cyberdodo.blockDisplayExecutor.listeners;

import fr.cyberdodo.blockDisplayExecutor.BlockDisplayExecutor;
import fr.cyberdodo.blockDisplayExecutor.commands.GiveCustomEggCommand;
import fr.cyberdodo.blockDisplayExecutor.utils.FunctionUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;

public class ArmorStandHitListener implements Listener {

    private final BlockDisplayExecutor plugin;
    private final FunctionUtils functionUtils;
    private GiveCustomEggCommand giveCustomEggCommand;

    public ArmorStandHitListener(BlockDisplayExecutor plugin) {
        this.plugin = plugin;
        this.functionUtils = plugin.getFunctionUtils();
        this.giveCustomEggCommand = new GiveCustomEggCommand(plugin);
    }

    @EventHandler
    public void onArmorStandHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        // Vérifie si l'entité est une ArmorStand avec notre clé personnalisée
        if (entity instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) entity;
            String functionName = armorStand.getPersistentDataContainer().get(functionUtils.getFunctionKey(), PersistentDataType.STRING);

            if (functionName != null) {
                // Annuler l'événement pour empêcher le comportement par défaut
                event.setCancelled(true);

                // Supprimer les block displays associés
                removeAssociatedBlockDisplays(armorStand);

                // Supprimer l'armor stand
                armorStand.remove();

                // Rendre l'œuf au joueur s'il s'agit d'un joueur
                if (event.getDamager() instanceof Player) {
                    Player player = (Player) event.getDamager();
                    ItemStack itemStack = giveCustomEggCommand.createFunctionEgg(functionName);
                    player.getInventory().addItem(itemStack);

                    player.sendMessage(ChatColor.GREEN + "L'œuf de la fonction '" + functionName + "' a été rendu dans votre inventaire.");
                }
            }
        }
    }

    private void removeAssociatedBlockDisplays(ArmorStand armorStand) {
        String armorStandUUID = armorStand.getUniqueId().toString();
        String tag = "bde_" + armorStandUUID;

        // Parcourir toutes les entités dans le monde de l'armor stand
        for (Entity entity : armorStand.getWorld().getEntities()) {
            if (entity.getScoreboardTags().contains(tag)) {
                entity.remove();
            }
        }
    }
}