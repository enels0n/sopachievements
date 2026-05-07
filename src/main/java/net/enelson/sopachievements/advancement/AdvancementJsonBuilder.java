package net.enelson.sopachievements.advancement;

import net.enelson.sopachievements.model.AchievementCategory;
import net.enelson.sopachievements.model.AchievementDefinition;

public final class AdvancementJsonBuilder {

    private AdvancementJsonBuilder() {
    }

    public static String build(AchievementDefinition definition, AchievementCategory category, String namespace) {
        String titleKey = definition.getNameKey();
        String titleFallback = definition.getNameKeyFallback();
        String titleText = definition.getTitle();
        String loreKey = definition.getLoreKey();
        String loreFallback = definition.getLoreKeyFallback();
        String loreText = definition.getDescription();

        if (definition.isRoot() && category != null) {
            if (category.getNameKey() != null && !category.getNameKey().trim().isEmpty()) {
                titleKey = category.getNameKey();
                titleFallback = category.getNameKeyFallback();
                titleText = category.getTitle();
            }
            if (category.getLoreKey() != null && !category.getLoreKey().trim().isEmpty()) {
                loreKey = category.getLoreKey();
                loreFallback = category.getLoreKeyFallback();
                loreText = category.getDescription();
            }
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        if (!definition.isRoot()) {
            json.append("\"parent\":\"").append(namespace).append(":").append(escape(definition.getParentId())).append("\",");
        }
        json.append("\"display\":{")
                .append("\"icon\":{\"item\":\"minecraft:").append(escape(definition.getIconMaterial().toLowerCase())).append("\"},")
                .append("\"title\":").append(buildTextComponent(titleKey, titleFallback, titleText)).append(",")
                .append("\"description\":").append(buildTextComponent(loreKey, loreFallback, loreText)).append(",")
                .append("\"frame\":\"").append(escape(definition.getFrame().toLowerCase())).append("\",")
                .append("\"show_toast\":").append(definition.isShowToast()).append(",")
                .append("\"announce_to_chat\":").append(definition.isAnnounceToChat()).append(",")
                .append("\"hidden\":").append(definition.isHidden()).append(",")
                .append("\"x\":").append(definition.getX()).append(",")
                .append("\"y\":").append(definition.getY());
        if (definition.isRoot()) {
            json.append(",\"background\":\"").append(escape(category.getBackground())).append("\"");
        }
        json.append("},");
        json.append("\"criteria\":{\"done\":{\"trigger\":\"minecraft:impossible\"}},");
        json.append("\"requirements\":[[\"done\"]]");
        json.append("}");
        return json.toString();
    }

    private static String buildTextComponent(String translateKey, String fallback, String plainText) {
        String normalizedKey = translateKey == null ? "" : translateKey.trim();
        if (!normalizedKey.isEmpty()) {
            StringBuilder json = new StringBuilder();
            json.append("{\"translate\":\"").append(escape(normalizedKey)).append("\"");
            String normalizedFallback = stripLegacy(fallback == null || fallback.trim().isEmpty() ? plainText : fallback);
            if (!normalizedFallback.isEmpty()) {
                json.append(",\"fallback\":\"").append(escape(normalizedFallback)).append("\"");
            }
            json.append("}");
            return json.toString();
        }
        return "{\"text\":\"" + escape(stripLegacy(plainText)) + "\"}";
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
