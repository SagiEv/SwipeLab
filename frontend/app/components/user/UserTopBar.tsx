import React from "react";
import { Image, StyleSheet, Text, TouchableOpacity, View } from "react-native";
import { useAuthStore } from "../../stores/authStore";
import { useModeStore } from "../../stores/modeStore"; // Import mode store
import { Ionicons } from '@expo/vector-icons';

interface UserTopBarProps {
  username?: string;
  score?: number;
  rank?: string;
  streak?: number;
  onLogout?: () => void;
}

export default function UserTopBar({
  username: propUsername,
  score = 520,
  rank = "#12",
  streak = 36,
  onLogout
}: UserTopBarProps) {
  const { logout, role } = useAuthStore(); // Get role
  const { setMode } = useModeStore(); // Get setMode
  const storeUsername = "Player";

  const displayUsername = propUsername || storeUsername;
  const handleLogout = onLogout || logout;

  // Format score if it's a number
  const displayScore = typeof score === 'number' ? score.toLocaleString() : score;
  const displayStreak = streak;

  const handleSwitchToManager = () => {
    setMode("ADMIN");
  };

  return (
    <View style={styles.container}>
      {/* User Profile */}
      <View style={styles.profileSection}>
        <View style={styles.avatarContainer}>
          <Image
            source={require("../../../assets/images/profile.png")}
            style={styles.avatar}
          />
          <Text style={styles.username}>{displayUsername}</Text>
        </View>
      </View>

      {/* Stats Block (Center) - Blue Card style */}
      <View style={styles.statsBlock}>
        <Text style={styles.statsText}>Score: {displayScore}</Text>
        <Text style={styles.statsText}>Rank: {rank}</Text>
        <Text style={styles.statsText}>{displayStreak} days streak</Text>
      </View>

      {/* Right Section: Switch Mode or Logout */}
      <View style={styles.rightSection}>
        {role === 'ADMIN' && (
          <TouchableOpacity onPress={handleSwitchToManager} style={styles.switchBtn}>
            <Ionicons name="briefcase-outline" size={20} color="#0EA5E9" />
            <Text style={styles.logoutText}>Manager</Text>
          </TouchableOpacity>
        )}

        <TouchableOpacity onPress={handleLogout} style={styles.logoutSection}>
          <Ionicons name="log-out-outline" size={24} color="#EF4444" style={{ marginBottom: 2 }} />
          <Text style={styles.logoutText}>Logout</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  profileSection: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarContainer: {
    alignItems: 'center',
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#ddd',
    marginBottom: 4,
  },
  username: {
    fontSize: 12,
    color: '#888',
    fontWeight: '600',
  },
  statsBlock: {
    backgroundColor: '#0EA5E9', // Light blue
    paddingHorizontal: 20,
    paddingVertical: 8,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    height: 70, // Fixed height for consistency
  },
  statsText: {
    color: '#fff',
    fontWeight: '700',
    fontSize: 13,
    lineHeight: 18,
  },
  rightSection: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  switchBtn: {
    alignItems: 'center',
    marginRight: 16,
  },
  logoutSection: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  logoutText: {
    fontSize: 10,
    color: '#888',
  },
});
