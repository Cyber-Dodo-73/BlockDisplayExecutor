package fr.cyberdodo.blockDisplayExecutor.utils;

import fr.cyberdodo.blockDisplayExecutor.BlockDisplayExecutor;
import org.bukkit.NamespacedKey;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class FunctionUtils {

    private final BlockDisplayExecutor plugin;
    private final File functionsFolder;
    private final NamespacedKey functionKey;

    public FunctionUtils(BlockDisplayExecutor plugin) {
        this.plugin = plugin;
        this.functionsFolder = new File(plugin.getDataFolder(), "functions");
        this.functionKey = new NamespacedKey(plugin, "functionName");

        // Création du dossier functions s'il n'existe pas
        if (!functionsFolder.exists()) {
            functionsFolder.mkdirs();
            plugin.getLogger().info("Created functions directory at " + functionsFolder.getPath());
        }
        createImportFunctionFolder();

    }

    private void createImportFunctionFolder() {
        File importFolder = new File(plugin.getDataFolder(), "importFunction");
        if (!importFolder.exists()) {
            boolean created = importFolder.mkdirs();
            if (created) {
                plugin.getLogger().info("Le dossier 'importFunction' a été créé à " + importFolder.getPath());
            } else {
                plugin.getLogger().warning("Impossible de créer le dossier 'importFunction' à " + importFolder.getPath());
            }
        }
    }

    public File getFunctionsFolder() {
        return functionsFolder;
    }

    public NamespacedKey getFunctionKey() {
        return functionKey;
    }

    public boolean functionExists(String functionName) {
        File functionFile = new File(functionsFolder, functionName + ".bde");
        return functionFile.exists();
    }

    /**
     * Récupère la liste des fonctions disponibles dans le dossier functions.
     *
     * @return Une liste des noms de fonctions disponibles.
     */
    public List<String> getAvailableFunctions() {
        if (functionsFolder == null || !functionsFolder.exists()) {
            return new ArrayList<>();
        }
        File[] files = functionsFolder.listFiles((dir, name) -> name.endsWith(".bde"));
        if (files == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(files)
                .map(file -> file.getName().replace(".bde", ""))
                .collect(Collectors.toList());
    }

    /**
     * Charge les commandes depuis un fichier .mcfunction en utilisant le nom de la fonction.
     *
     * @param functionName Le nom de la fonction (sans l'extension .mcfunction).
     * @return Une liste de commandes à exécuter.
     * @throws IOException Si une erreur se produit lors de la lecture du fichier.
     */
    public List<String> loadFunctionCommands(String functionName) throws IOException {
        File functionFile = new File(functionsFolder, functionName + ".bde");
        return loadFunctionCommands(functionFile);
    }

    /**
     * Charge les commandes depuis un fichier .mcfunction spécifique.
     *
     * @param functionFile Le fichier .mcfunction à charger.
     * @return Une liste de commandes à exécuter.
     * @throws IOException Si une erreur se produit lors de la lecture du fichier.
     */
    public List<String> loadFunctionCommands(File functionFile) throws IOException {
        if (!functionFile.exists()) {
            throw new FileNotFoundException("Function file not found: " + functionFile.getName());
        }

        List<String> commands = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(functionFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Ignore les lignes vides et les commentaires en début de ligne
                }
                // Supprime les commentaires hors des chaînes de caractères
                String processedLine = removeCommentsOutsideStrings(line);
                if (!processedLine.isEmpty()) {
                    commands.add(processedLine);
                }
            }
        }
        return commands;
    }

    private String removeCommentsOutsideStrings(String line) {
        boolean inString = false;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inString = !inString;
                result.append(c);
            } else if (c == '#' && !inString) {
                // Commentaire trouvé hors d'une chaîne de caractères
                break;
            } else {
                result.append(c);
            }
        }
        return result.toString().trim();
    }
}
