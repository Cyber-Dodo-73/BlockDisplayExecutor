package fr.cyberdodo.blockDisplayExecutor.commands;

import fr.cyberdodo.blockDisplayExecutor.BlockDisplayExecutor;
import fr.cyberdodo.blockDisplayExecutor.utils.FunctionUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ImportFunctionCommand implements CommandExecutor {

    private final BlockDisplayExecutor plugin;
    private final FunctionUtils functionUtils;

    public ImportFunctionCommand(BlockDisplayExecutor plugin) {
        this.plugin = plugin;
        this.functionUtils = plugin.getFunctionUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        File importFolder = new File(plugin.getDataFolder(), "importFunction");
        if (!importFolder.exists() || !importFolder.isDirectory()) {
            sender.sendMessage(ChatColor.RED + "Le dossier 'importFunction' n'existe pas.");
            return true;
        }

        File[] files = importFolder.listFiles((dir, name) -> name.endsWith(".mcfunction"));
        if (files == null || files.length == 0) {
            sender.sendMessage(ChatColor.RED + "Aucun fichier .mcfunction trouvé dans le dossier 'importFunction'.");
            return true;
        }

        int importedCount = 0;

        for (File file : files) {
            try {
                // Read the content of the file
                List<String> commands = readFunctionFile(file);

                // Adapt the commands
                List<String> adaptedCommands = adaptCommands(commands);

                // Save the adapted function
                String functionName = file.getName().replace(".mcfunction", "");
                saveAdaptedFunction(functionName, adaptedCommands);

                // Déplacer le fichier original vers le dossier archive
                moveFileToArchive(file);

                importedCount++;
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Erreur lors de l'importation de l'objet '" + file.getName() + "': " + e.getMessage());
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Importation terminée. " + importedCount + " objet(s) importée(s).");
        return true;
    }

    private List<String> readFunctionFile(File file) throws IOException {
        return functionUtils.loadFunctionCommands(file);
    }

    private List<String> adaptCommands(List<String> commands) {
        for (int i = 0; i < commands.size(); i++) {
            String cmd = commands.get(i);

            // Remplacer "~ ~ ~" par "{x} {y} {z}"
            cmd = cmd.replaceAll("~\\s*~\\s*~", "{x} {y} {z}");

            // Remplacer '@s' par '{armorStandUUID}'
            cmd = cmd.replaceAll("@s", "{armorStandUUID}");

            // Ajouter le tag aux block displays
            if (cmd.contains("summon block_display")) {
                String tag = "bde_" + "{armorStandUUID}";

                // Trouver l'index du début des données NBT après les coordonnées
                int summonIndex = cmd.indexOf("summon block_display") + "summon block_display".length();

                // Avancer jusqu'aux coordonnées
                int coordsIndex = summonIndex;
                for (int j = 0; j < 3; j++) {
                    // Ignorer les espaces
                    while (coordsIndex < cmd.length() && cmd.charAt(coordsIndex) == ' ') {
                        coordsIndex++;
                    }
                    // Ignorer les caractères des coordonnées
                    while (coordsIndex < cmd.length() && cmd.charAt(coordsIndex) != ' ') {
                        coordsIndex++;
                    }
                }

                // Extraire la chaîne NBT
                int nbtStartIndex = cmd.indexOf("{", coordsIndex);
                if (nbtStartIndex != -1) {
                    String nbtData = cmd.substring(nbtStartIndex);

                    // Ajouter le tag au NBT principal et aux passagers
                    nbtData = insertTagIntoNBT(nbtData, tag);

                    // Reconstruire la commande
                    cmd = cmd.substring(0, nbtStartIndex) + nbtData;
                } else {
                    // Pas de données NBT, ajouter des données NBT avec le tag
                    cmd += " {Tags:[\"" + tag + "\"]}";
                }
            }

            commands.set(i, cmd);
        }

        return commands;
    }


    private String insertTagIntoNBT(String nbtData, String tag) {
        // Ajouter le tag à l'entité principale
        nbtData = insertTagIntoFirstNBT(nbtData, tag);

        // Ajouter le tag aux passagers
        nbtData = nbtData.replaceAll("(\\{\\s*id:\"[^\"]+\"\\s*,)", "$1Tags:[\"" + tag + "\"],");

        return nbtData;
    }

    private String insertTagIntoFirstNBT(String nbtData, String tag) {
        // Trouver la position du premier '{' qui commence les données NBT
        int firstBraceIndex = nbtData.indexOf("{");
        if (firstBraceIndex != -1) {
            // Insérer le tag après la première '{'
            return nbtData.substring(0, firstBraceIndex + 1) + "Tags:[\"" + tag + "\"]," + nbtData.substring(firstBraceIndex + 1);
        } else {
            // Pas de données NBT, ajouter le tag
            return "{Tags:[\"" + tag + "\"]}";
        }
    }

    private void saveAdaptedFunction(String functionName, List<String> commands) throws IOException {
        File functionFile = new File(functionUtils.getFunctionsFolder(), functionName + ".bde");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(functionFile))) {
            for (String cmd : commands) {
                writer.write(cmd);
                writer.newLine();
            }
        }
    }

    private void moveFileToArchive(File file) {
        File archiveFolder = new File(plugin.getDataFolder(), "importFunction/archive");
        if (!archiveFolder.exists()) {
            boolean created = archiveFolder.mkdirs();
            if (!created) {
                plugin.getLogger().warning("Impossible de créer le dossier d'archive à " + archiveFolder.getPath());
                return;
            }
        }
        File destination = new File(archiveFolder, file.getName());
        try {
            Files.move(file.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().warning("Erreur lors du déplacement du fichier " + file.getName() + " vers l'archive : " + e.getMessage());
        }
    }
}
