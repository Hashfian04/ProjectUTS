package com.example.projectuts.utils;

import com.example.projectuts.R;

public class AvatarUtils {

    public static class AvatarTheme {
        public final int bgResId;
        public final int textResId;

        public AvatarTheme(int bgResId, int textResId) {
            this.bgResId = bgResId;
            this.textResId = textResId;
        }
    }

    public static String getInitials(String name) {
        if (name == null) return "?";
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return "?";
        String[] parts = trimmed.split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else if (parts[0].length() >= 2) {
            return parts[0].substring(0, 2).toUpperCase();
        } else {
            return parts[0].substring(0, 1).toUpperCase();
        }
    }

    public static AvatarTheme getAvatarTheme(String name) {
        AvatarTheme[] themes = new AvatarTheme[]{
                new AvatarTheme(R.color.avatar_blue_bg, R.color.avatar_blue_text),
                new AvatarTheme(R.color.avatar_amber_bg, R.color.avatar_amber_text),
                new AvatarTheme(R.color.avatar_purple_bg, R.color.avatar_purple_text),
                new AvatarTheme(R.color.avatar_green_bg, R.color.avatar_green_text),
                new AvatarTheme(R.color.avatar_rose_bg, R.color.avatar_rose_text)
        };
        int hash = name != null ? name.hashCode() : 0;
        int index = Math.abs(hash) % themes.length;
        return themes[index];
    }

    public static AvatarTheme getCategoryBadgeTheme(String category) {
        if (category == null) category = "Other";
        switch (category.toLowerCase()) {
            case "family":
                return new AvatarTheme(R.color.badge_family_bg, R.color.badge_family_text);
            case "friends":
                return new AvatarTheme(R.color.badge_friends_bg, R.color.badge_friends_text);
            case "work":
                return new AvatarTheme(R.color.badge_work_bg, R.color.badge_work_text);
            default:
                return new AvatarTheme(R.color.badge_other_bg, R.color.badge_other_text);
        }
    }

    public static int getCategoryIconRes(String categoryName) {
        if (categoryName == null) categoryName = "Other";
        switch (categoryName.toLowerCase()) {
            case "family":
                return R.drawable.ic_cat_family;
            case "friends":
                return R.drawable.ic_cat_friends;
            case "work":
                return R.drawable.ic_cat_work;
            default:
                return R.drawable.ic_cat_other;
        }
    }
}