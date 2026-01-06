import React from "react";
import { Image, StyleSheet, Text, TouchableOpacity, View } from "react-native";
import profileImg from "../../../assets/images/profile.png";
import { useAuthStore } from "../../stores/authStore";
import { useModeStore } from "../../stores/modeStore";
import { Ionicons } from '@expo/vector-icons';

export default function AdminTopBar() {
  const { logout, username } = useAuthStore();
  const { setMode } = useModeStore();

  const handleSwitchToPlayer = () => {
    setMode("USER");
  };

  return (
    <View style={styles.container}>
      {/* Profile */}
      <View style={styles.profileSection}>
        <View style={styles.avatarContainer}>
          <Image
            source={profileImg}
            style={styles.avatar}
          />
          <Text style={styles.username}>{username || "Admin"}</Text>
        </View>
      </View>

      {/* Blue card - Statistics or Title */}
      <View style={styles.card}>
        <Text style={styles.text}>Manager Dashboard</Text>
        <Text style={styles.subText}>Overview</Text>
      </View>

      {/* Right Section: Switch & Logout */}
      <View style={styles.rightSection}>
        <TouchableOpacity onPress={handleSwitchToPlayer} style={styles.switchBtn}>
          <Ionicons name="game-controller-outline" size={20} color="#0EA5E9" />
          <Text style={styles.logoutText}>Play</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.logoutBtn} onPress={logout}>
          <Ionicons name="log-out-outline" size={24} color="#EF4444" style={{ marginBottom: 2 }} />
          <Text style={styles.logoutText}>Logout</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
    alignItems: "center",
    backgroundColor: "#fff",
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
  avatar: { width: 40, height: 40, borderRadius: 20, marginBottom: 4, backgroundColor: '#ddd' },
  username: {
    fontSize: 12,
    color: '#888',
    fontWeight: '600',
  },

  card: {
    backgroundColor: "#0EA5E9",
    paddingHorizontal: 20,
    paddingVertical: 8,
    borderRadius: 12,
    alignItems: "center",
    justifyContent: 'center',
    height: 70, // Fixed height for consistency
  },
  text: { color: "white", fontWeight: "700", fontSize: 13, lineHeight: 18 },
  subText: { color: "white", fontSize: 10, opacity: 0.9 },

  rightSection: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  switchBtn: {
    alignItems: 'center',
    marginRight: 16,
  },
  logoutBtn: {
    alignItems: "center",
    justifyContent: "center"
  },
  logoutText: {
    fontSize: 10,
    color: "#888",
    marginTop: 2
  },
});
