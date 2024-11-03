package fr.cyberdodo.blockDisplayExecutor;

import fr.cyberdodo.blockDisplayExecutor.commands.FunctionCommandExecutor;
import fr.cyberdodo.blockDisplayExecutor.commands.GiveCustomEggCommand;
import fr.cyberdodo.blockDisplayExecutor.commands.ImportFunctionCommand;
import fr.cyberdodo.blockDisplayExecutor.listeners.ArmorStandHitListener;
import fr.cyberdodo.blockDisplayExecutor.listeners.EggPlaceListener;

import fr.cyberdodo.blockDisplayExecutor.utils.FunctionUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BlockDisplayExecutor extends JavaPlugin {

    private FunctionUtils functionUtils;

    @Override
    public void onEnable() {
        // Initialisation de FunctionUtils
        functionUtils = new FunctionUtils(this);

        // Enregistrement des commandes
        this.getCommand("executeFunction").setExecutor(new FunctionCommandExecutor(this));
        this.getCommand("giveCustomEgg").setExecutor(new GiveCustomEggCommand(this));
        this.getCommand("importFunction").setExecutor(new ImportFunctionCommand(this));

        // Enregistrement des écouteurs
        this.getServer().getPluginManager().registerEvents(new EggPlaceListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ArmorStandHitListener(this), this);
    }

    public FunctionUtils getFunctionUtils() {
        return functionUtils;
    }
}