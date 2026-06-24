package net.enelson.sopachievements.advancement;

import net.enelson.sopachievements.model.AchievementCategory;
import net.enelson.sopachievements.model.AchievementDefinition;

public final class AdvancementJsonBuilder {

    private static final String CATEGORY_ROOT_PREFIX = "__category_";

    private AdvancementJsonBuilder() {
    }

    public static String build(AchievementDefinition definition, AchievementCategory category, String namespace) {
        String titleKey = definition.getNameKey();
        String titleFallback = definition.getNameKeyFallback();
        String titleText = definition.getTitle();
        String loreKey = definition.getLoreKey();
        String loreFallback = definition.getLoreKeyFallback();
        String loreText = definition.getDescription();

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"parent\":\"").append(namespace).append(":").append(escape(resolveParentId(definition, category))).append("\",");
        json.append("\"display\":{")
                .append("\"icon\":").append(buildIconComponent(definition.getIconMaterial())).append(",")
                .append("\"title\":").append(buildTextComponent(titleKey, titleFallback, titleText)).append(",")
                .append("\"description\":").append(buildTextComponent(loreKey, loreFallback, loreText)).append(",")
                .append("\"frame\":\"").append(escape(definition.getFrame().toLowerCase())).append("\",")
                .append("\"show_toast\":").append(definition.isShowToast()).append(",")
                .append("\"announce_to_chat\":").append(definition.isAnnounceToChat()).append(",")
                .append("\"hidden\":").append(definition.isHidden()).append(",")
                .append("\"x\":").append(definition.getX()).append(",")
                .append("\"y\":").append(definition.getY());
        json.append("},");
        json.append("\"criteria\":{\"done\":{\"trigger\":\"minecraft:impossible\"}},");
        json.append("\"requirements\":[[\"done\"]]");
        json.append("}");
        return json.toString();
    }

    public static String buildCategoryRoot(AchievementCategory category) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"display\":{")
                .append("\"icon\":").append(buildIconComponent(category.getIconMaterial())).append(",")
                .append("\"title\":").append(buildTextComponent(category.getNameKey(), category.getNameKeyFallback(), category.getTitle())).append(",")
                .append("\"description\":").append(buildTextComponent(category.getLoreKey(), category.getLoreKeyFallback(), category.getDescription())).append(",")
                .append("\"frame\":\"task\",")
                .append("\"show_toast\":false,")
                .append("\"announce_to_chat\":false,")
                .append("\"hidden\":false,")
                .append("\"x\":0,")
                .append("\"y\":0,")
                .append("\"background\":\"").append(escape(normalizeBackground(category.getBackground()))).append("\"");
        json.append("},");
        json.append("\"criteria\":{\"auto\":{\"trigger\":\"minecraft:tick\"}},");
        json.append("\"requirements\":[[\"auto\"]]");
        json.append("}");
        return json.toString();
    }

    public static String categoryRootId(String categoryId) {
        return CATEGORY_ROOT_PREFIX + categoryId;
    }

    private static String resolveParentId(AchievementDefinition definition, AchievementCategory category) {
        if (!definition.isRoot()) {
            return definition.getParentId();
        }
        return categoryRootId(category == null ? "main" : category.getId());
    }

    private static String buildIconComponent(String iconMaterial) {
        String material = escape(iconMaterial.toLowerCase());
        return "{\"id\":\"minecraft:" + material + "\",\"item\":\"minecraft:" + material + "\"}";
    }

    private static String buildTextComponent(String translateKey, String fallback, String plainText) {
        String normalizedFallback = stripLegacy(fallback == null || fallback.trim().isEmpty() ? plainText : fallback);
        String normalizedKey = translateKey == null ? "" : translateKey.trim();
        if (!normalizedKey.isEmpty()) {
            return "{\"translate\":\"" + escape(normalizedKey) + "\"}";
        }
        return "{\"text\":\"" + escape(normalizedFallback) + "\"}";
    }

    private static String normalizeBackground(String background) {
        if (background == null || background.trim().isEmpty()) {
            return "minecraft:textures/block/stone.png";
        }
        String normalized = background.trim();
        if ("minecraft:textures/gui/advancements/backgrounds/stone.png".equalsIgnoreCase(normalized)) {
            return "minecraft:textures/block/stone.png";
        }
        return normalized;
    }

    private static String stripLegacy(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("(?i)&[0-9A-FK-ORX]", "");
    }

    private static String escape(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
