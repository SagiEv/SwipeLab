// components/layout/ScreenHeaderLayout.tsx
import React from "react";
import { Image, StyleSheet, Text, TouchableOpacity, View } from "react-native";
import { ScreenHeaderLayoutProps } from "./ScreenHeaderLayout.types";

export default function ScreenHeaderLayout({
  leftIcon,
  leftTitle,
  rightIcon,
  rightTitle,
  onRightPress,
  centerIcon,
  centerTitle,
  onCenterPress,
  children,
  contentContainerStyle,
}: ScreenHeaderLayoutProps) {
  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        {/* Left (current screen) */}
        <View style={styles.leftHeaderItem}>
          <Image source={leftIcon} style={styles.icon} />
          <Text style={styles.title} numberOfLines={1}>{leftTitle}</Text>
        </View>

        {/* Center (optional navigation action) */}
        {centerTitle && (
          <View style={styles.centerWrapper}>
            <TouchableOpacity
              style={styles.centerHeaderItem}
              onPress={onCenterPress}
              disabled={!onCenterPress}
            >
              {centerIcon && <Image source={centerIcon} style={styles.icon} />}
              <Text style={styles.button}>{centerTitle}</Text>
            </TouchableOpacity>
          </View>
        )}

        {/* Right (navigation action) */}
        <TouchableOpacity
          style={styles.rightHeaderItem}
          onPress={onRightPress}
          disabled={!onRightPress}
        >
          <Image source={rightIcon} style={styles.icon} />
          <Text style={styles.button} numberOfLines={1}>{rightTitle}</Text>
        </TouchableOpacity>
      </View>

      {/* Screen Content */}
      <View style={[styles.content, contentContainerStyle]}>{children}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    padding: 12,
    borderBottomWidth: 1,
    borderColor: "#E0E0E0",
    position: 'relative', // Ensure absolute child is relative to header
  },
  leftHeaderItem: {
    alignItems: "center",
    flexDirection: "row",
    gap: 8,
    flex: 1,
    zIndex: 1,
  },
  centerWrapper: {
    position: "absolute",
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    alignItems: "center",
    justifyContent: "center",
    zIndex: 0,
    pointerEvents: 'box-none',
  },
  centerHeaderItem: {
    alignItems: "center",
    justifyContent: "center",
  },
  rightHeaderItem: {
    alignItems: "center",
    flexDirection: "row",
    gap: 8,
    flex: 1,
    justifyContent: "flex-end",
    zIndex: 1,
  },
  icon: {
    width: 28,
    height: 28,
    resizeMode: "contain",
  },
  title: {
    fontSize: 18,
    fontWeight: "600",
    flex: 1,
  },
  button: {
    fontSize: 14,
    fontWeight: "600",
    textAlign: "center",
  },
  content: {
    flex: 1,
    padding: 12,
  },
});
