import { createNativeStackNavigator } from "@react-navigation/native-stack";
import React from "react";
import { StyleSheet, View } from "react-native";
import SettingsScreen from "../screens/shared/SettingsScreen";
import LeaderboardScreen from "../screens/user/LeaderboardScreen";
import UserMyTasksScreen from "../screens/user/UserMyTasksScreen";
import SwipeScreen from "../screens/user/SwipeScreen";
import ChallengesScreen from "../screens/user/ChallengesScreen";
import StatsScreen from "../screens/user/StatsScreen";
import BottomBar from "./components/BottomBar";
import TaskDetailsScreen from "../screens/user/TaskDetailsScreen";
import TopBar from "./components/TopBar";

const Stack = createNativeStackNavigator();

export default function UserNavigator() {
  return (
    <View style={styles.container}>
      {/* Top Bar removed from here as UserHeader is in the screen, or we keep it? 
          The design shows a header inside the screen. 
          Let's double check providing `headerShown: false` covers it. 
          The current code has `<TopBar />` outside the navigator. 
          For "My Tasks" screen, it has its own header.
          We might want to conditionally hide TopBar or just leave it for other screens?
          Actually, let's keep it simple. The UserMyTasksScreen has its own header.
          If `TopBar` is the generic "SwipeLab" header, maybe we don't need it on the Home screen if it has a custom user header.
          However, I'll focus on the Navigator changes first.
      */}
      {/* TopBar might conflict with UserHeader visually. 
          For now, I'll leave TopBar but maybe the user wants it removed for this screen?
          The instruction doesn't say. I'll stick to the plan.
      */}


      {/* Middle Navigator */}
      <View style={styles.content}>


        <Stack.Navigator screenOptions={{ headerShown: false }}>
          <Stack.Screen name="SwipeLab" component={SwipeScreen} />
          <Stack.Screen name="Tasks" component={UserMyTasksScreen} />
          <Stack.Screen name="TaskDetails" component={TaskDetailsScreen} />
          <Stack.Screen name="Challenges" component={ChallengesScreen} />
          <Stack.Screen name="UserSettings" component={SettingsScreen} />
          <Stack.Screen name="Stats" component={StatsScreen} />
          <Stack.Screen name="Leaderboard" component={LeaderboardScreen} />
          {/* other screens */}
        </Stack.Navigator>
      </View>

      {/* Bottom Bar */}
      <BottomBar
        items={[
          { label: "Home", route: "SwipeLab", icon: require("../../assets/images/home.png") },
          { label: "My Tasks", route: "Tasks", icon: require("../../assets/images/tasks.png") },
          { label: "Challenges", route: "Challenges", icon: require("../../assets/images/leaderboard.png") },
          { label: "Stats", route: "Stats", icon: require("../../assets/images/stats.png") },
          { label: "Settings", route: "UserSettings", icon: require("../../assets/images/settings.png") },
        ]}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1, // Fill entire screen
  },
  content: {
    flex: 1, // Take remaining space between top and bottom
  },
});
