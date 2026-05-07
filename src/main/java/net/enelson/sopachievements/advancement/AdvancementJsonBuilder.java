package net.enelson.sopachievements.advancement;

import net.enelson.sopachievements.model.AchievementCategory;
import net.enelson.sopachievements.model.AchievementDefinition;

public final class AdvancementJsonBuilder {

    private AdvancementJsonBuilder() {
    }

    public static String build(AchievementDefinition definition, AchievementCategory category, String namespace) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        if (!definition.isRoot()) {
            json.append("\"parent\":\"").append(namespace).append(":").append(escape(definition.getParentId())).append("\",");
        }
        json.append("\"display\":{")
                .append("\"icon\":{\"item\":\"minecraft:").append(escape(definition.getIconMaterial().toLowerCase())).append("\"},")
                .append("\"title\":{\"text\":\"").append(escape(stripLegacy(definition.getTitle()))).append("\"},")
                .append("\"description\":{\"text\":\"").append(escape(stripLegacy(definition.getDescription()))).append("\"},")
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
