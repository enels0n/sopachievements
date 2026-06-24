package net.enelson.sopachievements.advancement;

import net.enelson.sopachievements.SopAchievementsPlugin;
import net.enelson.sopachievements.model.AchievementCategory;
import net.enelson.sopachievements.model.AchievementDefinition;
import net.enelson.sopachievements.model.AchievementRegistryModel;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class AdvancementDataPackWriter {

    private static final String PACK_NAME = "SopAchievements-generated";

    private final SopAchievementsPlugin plugin;

    public AdvancementDataPackWriter(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    public void write(AchievementRegistryModel model) throws IOException {
        File packFolder = getPackFolder();
        if (packFolder == null) {
            plugin.getLogger().warning("Failed to resolve datapack folder for SopAchievements.");
            return;
        }
        deleteRecursively(packFolder);
        File advancementRoot = new File(packFolder, "data/" + namespace() + "/" + advancementDirectoryName());
        if (!advancementRoot.mkdirs() && !advancementRoot.exists()) {
            throw new IOException("Failed to create datapack advancement folder: " + advancementRoot.getAbsolutePath());
        }
        writeString(new File(packFolder, "pack.mcmeta"), packMetaJson());
        for (AchievementCategory category : model.getCategories().values()) {
            writeString(
                    new File(advancementRoot, AdvancementJsonBuilder.categoryRootId(category.getId()) + ".json"),
                    AdvancementJsonBuilder.buildCategoryRoot(category)
            );
        }
        for (Map.Entry<String, AchievementDefinition> entry : model.getAchievements().entrySet()) {
            AchievementDefinition definition = entry.getValue();
            AchievementCategory category = model.getCategories().get(definition.getCategoryId());
            if (category == null) {
                continue;
            }
            String json = AdvancementJsonBuilder.build(definition, category, namespace());
            writeString(new File(advancementRoot, definition.getId() + ".json"), json);
        }
    }

    public File getPackFolder() {
        if (Bukkit.getWorlds().isEmpty()) {
            return null;
        }
        World world = Bukkit.getWorlds().get(0);
        File worldFolder = world.getWorldFolder();
        return new File(worldFolder, "datapacks/" + PACK_NAME);
    }

    public String getPackReference() {
        return "file/" + PACK_NAME;
    }

    private String namespace() {
        return plugin.getConfig().getString("settings.namespace", "sopachievements");
    }

    private String advancementDirectoryName() {
        String version = detectedVersion();
        if (version.startsWith("1.16.")) {
            return "advancements";
        }
        return "advancement";
    }

    private String packMetaJson() {
        int packFormat = resolveDataPackFormat();
        return "{\n"
                + "  \"pack\": {\n"
                + "    \"description\": \"Generated SopAchievements advancements\",\n"
                + "    \"pack_format\": " + packFormat + "\n"
                + "  }\n"
                + "}\n";
    }

    private int resolveDataPackFormat() {
        String version = detectedVersion();
        if (version == null) {
            return 48;
        }
        if (version.startsWith("1.16.")) {
            return 6;
        }
        if (version.startsWith("1.21.0") || version.startsWith("1.21.1")) {
            return 48;
        }
        if (version.startsWith("1.21.")) {
            return 81;
        }
        return 48;
    }

    private String detectedVersion() {
        String version = Bukkit.getServer() == null ? null : Bukkit.getServer().getBukkitVersion();
        if (version == null) {
            return "";
        }
        int dashIndex = version.indexOf('-');
        if (dashIndex > 0) {
            version = version.substring(0, dashIndex);
        }
        return version;
    }

    private void writeString(File file, String value) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create folder: " + parent.getAbsolutePath());
        }
        Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        try {
            writer.write(value);
        } finally {
            writer.close();
        }
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        if (!file.delete()) {
            file.deleteOnExit();
        }
    }
}
