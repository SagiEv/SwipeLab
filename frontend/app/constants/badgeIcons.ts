import { ImageSourcePropType } from "react-native";

/**
 * Lab-themed mascot icons for profile badges.
 *
 * React Native's `require` needs static string literals, so every icon is
 * registered here by the badge's title (as seeded in the backend, see
 * V6__add_gamification_updates.sql). Crop each mascot into its own square,
 * transparent PNG and drop it in `assets/images/badges/` using these names.
 */
const BADGE_ICONS: Record<string, ImageSourcePropType> = {
    "First Swipe": require("../../assets/images/badges/badge_first_swipe.png"),
    "10 Swipes": require("../../assets/images/badges/badge_10_swipes.png"),
    "100 Swipes": require("../../assets/images/badges/badge_100_swipes.png"),
    "3 Day Streak": require("../../assets/images/badges/badge_streak_3.png"),
    "7 Day Streak": require("../../assets/images/badges/badge_streak_7.png"),
    "1000 Points": require("../../assets/images/badges/badge_points_1000.png"),
};

/** Mascot used for any badge without a specific icon mapping. */
const DEFAULT_BADGE_ICON: ImageSourcePropType = require("../../assets/images/badges/badge_default.png");

/** Returns the lab-themed mascot icon for a badge title, falling back to the default mascot. */
export const getBadgeIcon = (title: string): ImageSourcePropType =>
    BADGE_ICONS[title] ?? DEFAULT_BADGE_ICON;
