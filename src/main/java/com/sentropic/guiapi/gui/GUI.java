package com.sentropic.guiapi.gui;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.sentropic.guiapi.packet.WrapperPlayServerTitle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class GUI {
    public static boolean sendingPacket = false;

    private final List<GUIComponent> guiComponents = new ArrayList<>();
    private final Player player;
    private String rawJson;
    private boolean changed = true;
    private final WrapperPlayServerTitle packet;

    public GUI(Player player) {
        this.player = player;
        packet = new WrapperPlayServerTitle();
        packet.setAction(EnumWrappers.TitleAction.ACTIONBAR);
    }

    public Player getPlayer() { return player; }

    public void putOnTop(@NotNull GUIComponent guiComponent) {
        remove(guiComponent.getId());
        guiComponents.add(guiComponent);
        changed = true;
    }

    public void putUnderneath(@NotNull GUIComponent guiComponent) {
        remove(guiComponent.getId());
        guiComponents.add(0, guiComponent);
        changed = true;
    }

    public boolean update(@NotNull GUIComponent guiComponent) {
        boolean success = false;
        String id = guiComponent.getId();
        for (ListIterator<GUIComponent> iterator = guiComponents.listIterator(); iterator.hasNext(); ) {
            GUIComponent component = iterator.next();
            if (component.getId().equals(id)) {
                iterator.set(guiComponent);
                changed = true;
                success = true;
                break;
            }
        }
        return success;
    }

    public boolean putAfter(String after, @NotNull GUIComponent guiComponent) {
        boolean success = false;
        remove(guiComponent.getId());
        int i = 0;
        for (GUIComponent component : guiComponents) {
            i++;
            if (component.getId().equals(after)) {
                guiComponents.add(i, guiComponent);
                changed = true;
                success = true;
                break;
            }
        }
        return success;
    }

    public boolean putBefore(String before, @NotNull GUIComponent guiComponent) {
        boolean success = false;
        remove(guiComponent.getId());
        int i = -1;
        for (GUIComponent component : guiComponents) {
            i++;
            if (component.getId().equals(before)) {
                guiComponents.add(i, guiComponent);
                changed = true;
                success = true;
                break;
            }
        }
        return success;
    }

    public boolean remove(String id) {
        boolean success = guiComponents.removeIf(guiComponent -> guiComponent.getId().equals(id));
        changed = success || changed;
        return success;
    }

    public boolean removeIf(Predicate<GUIComponent> predicate) {
        return guiComponents.removeIf(predicate);
    }

    private void build() {
        StringBuilder builder = new StringBuilder("[{\"text\":\"");
        int offset = 0;
        Font font = Font.DEFAULT;

        for (GUIComponent component : guiComponents) {
            for (GUIComponent word : component.byWord()) {
                if (!font.equals(Font.DEFAULT)) {
                    builder.append("\",\"font\":\"")
                           .append(font)
                           .append("\"},{\"text\":\"");
                }
                builder.append(spacesOf(offset+word.getLeftOffset()));
                font = word.getFont();
                if (!font.equals(Font.DEFAULT)) {
                    builder.append("\",\"font\":\"minecraft:default\"},{\"text\":\"");
                }
                builder.append(word.getText());
                offset = word.getRightOffset();
            }
        }

        if (!font.equals(Font.DEFAULT)) {
            builder.append("\",\"font\":\"")
                   .append(font)
                   .append("\"},{\"text\":\"");
        }
        builder.append(spacesOf(offset));
        builder.append("\",\"font\":\"minecraft:default\"}]");

        rawJson = builder.toString();
        packet.setTitle(WrappedChatComponent.fromJson(rawJson));
        changed = false;
    }

    public String getRawJson() {
        if (changed) { build(); }
        return rawJson;
    }

    public void play() {
        if (changed) { build(); }
        sendingPacket = true;
        packet.sendPacket(player);
        sendingPacket = false;
    }

    public static String spacesOf(int amount) {
        Map<Integer,String> spaces;
        if (amount == 0) { return ""; } else if (amount > 0) {
            spaces = POS_SPACES;
        } else {
            amount = -amount;
            spaces = NEG_SPACES;
        }
        StringBuilder builder = new StringBuilder();
        while (amount > 1024) {
            builder.append(spaces.get(1024));
            amount -= 1024;
        }
        int power = 1;
        while (amount/power >= 1) {
            power *= 2;
        }
        while (amount > 0) {
            if (amount > 8) {
                power /= 2;
                if (amount >= power) {
                    builder.append(spaces.get(power));
                    amount -= power;
                }
            } else {
                builder.append(spaces.get(amount));
                break;
            }
        }
        return builder.toString();
    }

    public static boolean isSendingPacket() { return sendingPacket; }

    private static final Map<Integer,String> POS_SPACES = new HashMap<Integer,String>() {{
        put(1024, "\uF82F");
        put(512, "\uF82E");
        put(256, "\uF82D");
        put(128, "\uF82C");
        put(64, "\uF82B");
        put(32, "\uF82A");
        put(16, "\uF829");
        put(8, "\uF828");
        put(7, "\uF827");
        put(6, "\uF826");
        put(5, "\uF825");
        put(4, "\uF824");
        put(3, "\uF823");
        put(2, "\uF822");
        put(1, "\uF821");
    }};
    private static final Map<Integer,String> NEG_SPACES = new HashMap<Integer,String>() {{
        put(1, "\uF801");
        put(2, "\uF802");
        put(3, "\uF803");
        put(4, "\uF804");
        put(5, "\uF805");
        put(6, "\uF806");
        put(7, "\uF807");
        put(8, "\uF808");
        put(16, "\uF809");
        put(32, "\uF80A");
        put(64, "\uF80B");
        put(128, "\uF80C");
        put(256, "\uF80D");
        put(512, "\uF80E");
        put(1024, "\uF80F");
    }};
}
