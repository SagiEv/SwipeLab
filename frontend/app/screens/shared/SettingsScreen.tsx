import { useThemeColor } from '@/app/hooks/use-theme-color';
import { useAuthStore } from '@/app/stores/authStore';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { useNavigation } from '@react-navigation/native';
import React from 'react';
import { StyleSheet, Switch, TouchableOpacity, View } from 'react-native';
import ScreenHeaderLayout from "../../components/layout/ScreenHeaderLayout";

export default function SettingsScreen() {
  const { logout } = useAuthStore();
  const iconColor = useThemeColor({}, 'text');

  // Placeholder states for the toggles in the wireframe
  const [notifications, setNotifications] = React.useState(true);
  const [darkMode, setDarkMode] = React.useState(false);
  const navigation = useNavigation<any>();

  return (
    <ScreenHeaderLayout 
      leftTitle="Settings"
      leftIcon={require('@/assets/images/settings.png')} 
      rightTitle="Profile"
      rightIcon={require('@/assets/images/profile.png')} 
      onRightPress={() => navigation.navigate('Profile')}
    >
      <ThemedView style={styles.container}>
        
        {/* Account Section */}
        <View style={styles.section}>
          <ThemedText type="subtitle" style={styles.sectionTitle}>Account</ThemedText>
          <TouchableOpacity 
            style={styles.row} 
            onPress={() => navigation.navigate('Profile')}
          >
            <View style={styles.rowLeft}>
              <IconSymbol name="person.fill" size={24} color={iconColor} />
              <ThemedText style={styles.rowLabel}>View Profile</ThemedText>
            </View>
            <IconSymbol name="chevron.right" size={20} color={iconColor} />
          </TouchableOpacity>
        </View>

        {/* Preferences Section */}
        <View style={styles.section}>
          <ThemedText type="subtitle" style={styles.sectionTitle}>Preferences</ThemedText>
          
          <View style={styles.row}>
            <View style={styles.rowLeft}>
              <IconSymbol name="bell.fill" size={24} color={iconColor} />
              <ThemedText style={styles.rowLabel}>Notifications</ThemedText>
            </View>
            <Switch value={notifications} onValueChange={setNotifications} />
          </View>

          <View style={styles.row}>
            <View style={styles.rowLeft}>
              <IconSymbol name="moon.fill" size={24} color={iconColor} />
              <ThemedText style={styles.rowLabel}>Dark Mode</ThemedText>
            </View>
            <Switch value={darkMode} onValueChange={setDarkMode} />
          </View>
        </View>

        {/* Support Section */}
        <View style={styles.section}>
          <ThemedText type="subtitle" style={styles.sectionTitle}>Support</ThemedText>
          <TouchableOpacity style={styles.row}>
            <ThemedText style={styles.rowLabel}>Help Center</ThemedText>
            <IconSymbol name="arrow.up.right" size={20} color={iconColor} />
          </TouchableOpacity>
        </View>

        {/* Logout Button */}
        <TouchableOpacity style={styles.logoutButton} onPress={logout}>
          <ThemedText style={styles.logoutText}>Log Out</ThemedText>
        </TouchableOpacity>

        <ThemedText style={{ textAlign: 'center', marginTop: 20, opacity: 0.4, fontSize: 12 }}>
          SwipeLab v1.0.0-alpha
        </ThemedText>
      </ThemedView>
    </ScreenHeaderLayout>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  section: { marginBottom: 30 },
  sectionTitle: { marginBottom: 15, opacity: 0.6 },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 15,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#ccc',
  },
  rowLeft: { flexDirection: 'row', alignItems: 'center' },
  rowLabel: { marginLeft: 15, fontSize: 16 },
  logoutButton: {
    marginTop: 20,
    backgroundColor: '#FF3B30',
    padding: 15,
    borderRadius: 10,
    alignItems: 'center',
  },
  logoutText: { color: 'white', fontWeight: 'bold', fontSize: 16 },
});