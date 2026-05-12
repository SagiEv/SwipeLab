import { useNavigation } from '@react-navigation/native';
import React, { useCallback } from 'react';
import { useMyTasks, useAvailableTasks, useStatistics, useAssignTask } from "../../api/queries";
import { Alert, RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Colors } from '../../../constants/theme';

import ScreenHeaderLayout from '../../components/layout/ScreenHeaderLayout/ScreenHeaderLayout';
import TaskCard from '../../components/user/TaskCard';
import { useThemeStore } from '../../stores/themeStore';
import { useSwipeStore } from '../../stores/swipeStore';


export default function UserMyTasksScreen() {
    const navigation = useNavigation<any>();
    const { theme } = useThemeStore();
    const themeColors = Colors[theme as keyof typeof Colors];
    const { setActiveTaskId } = useSwipeStore();

    const { data: stats, isLoading: statsLoading, refetch: refetchStats } = useStatistics();
    const { data: tasks = [], isLoading: tasksLoading, refetch: refetchTasks } = useMyTasks();
    const { data: availableTasks = [], isLoading: availableLoading, refetch: refetchAvailable } = useAvailableTasks();

    const refreshing = statsLoading || tasksLoading || availableLoading;

    const loadData = useCallback(() => {
        refetchStats();
        refetchTasks();
        refetchAvailable();
    }, [refetchStats, refetchTasks, refetchAvailable]);

    const assignTask = useAssignTask();

    const handleAddTask = async (taskId: number) => {
        try {
            await assignTask.mutateAsync(taskId);
            // Cache is refreshed via onSuccess in useAssignTask; nothing more needed here.
        } catch (err: any) {
            Alert.alert('Could not add task', err?.message ?? 'An unexpected error occurred.');
        }
    };

    const handleTaskPress = (task: any) => {
        navigation.navigate('TaskDetails', { task });
    };


    const handleLogout = () => {
        if (navigation.canGoBack()) {
            navigation.goBack();
        }
    };

    // UI guard: filter explore list against assigned IDs as a second line of defense.
    // The backend exclusion query is the primary guard; this prevents any stale-cache leakage.
    const assignedTaskIds = new Set((tasks as any[]).map((t) => t.taskId));
    const filteredExploreTasks = (availableTasks as any[]).filter((t) => !assignedTaskIds.has(t.taskId));

    // Calculate total completion
    const totalAssigned = tasks.length;
    let totalImages = 0;
    let totalClassified = 0;
    tasks.forEach((t: any) => {
        totalImages += t.progress?.totalImages ?? t.totalImages ?? 0;
        totalClassified += t.progress?.imagesClassified ?? t.imagesClassified ?? 0;
    });
    const completionPercent = totalImages > 0 ? Math.round((totalClassified / totalImages) * 100) : 0;

    const handlePlayTask = (taskId: number) => {
        // Persist chosen task in global store, then navigate to the Swipe tab
        setActiveTaskId(taskId);
        navigation.navigate('SwipeLab');
    };

    return (
        <ScreenHeaderLayout
            leftIcon={require('../../../assets/images/tasks.png')}
            leftTitle="My Tasks"
            rightIcon={require('../../../assets/images/stats.png')}
            rightTitle={`Completion: ${completionPercent}%`}
            contentContainerStyle={{ padding: 0 }}
        >
            <ScrollView
                style={{ backgroundColor: themeColors.background }}
                contentContainerStyle={[styles.scrollContent, { backgroundColor: themeColors.background }]}
                showsVerticalScrollIndicator={false}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={loadData} tintColor={themeColors.text} />
                }
            >
                {/* ... existing content ... */}
                {/* Section Title */}
                <View style={[styles.sectionHeader, { marginBottom: 10 }]}>
                    <Text style={[styles.sectionTitle, { color: themeColors.text }]}>Assigned Tasks</Text>
                </View>

                {tasks.length === 0 && (
                    <Text style={[styles.emptyState, { color: themeColors.textSecondary }]}>No tasks assigned yet.</Text>
                )}

                {tasks.map((task: any) => {
                    // Calculate progress
                    const totalImages = task.progress?.totalImages ?? task.totalImages ?? 0;
                    const imagesClassified = task.progress?.imagesClassified ?? task.imagesClassified ?? 0;
                    const progress = totalImages > 0 ? Math.round((imagesClassified / totalImages) * 100) : 0;

                    return (
                        <TaskCard
                            key={task.taskId}
                            title={task.name}
                            description={task.description}
                            species={Array.isArray(task.targetSpecies) ? task.targetSpecies.map((s: any) => s.name ?? s) : []}
                            imagesClassified={imagesClassified}
                            progress={progress}
                            onPlay={() => handlePlayTask(task.taskId)}
                            onPress={() => handleTaskPress(task)}
                            actionType="play"
                        />
                    );
                })}

                <View style={[styles.sectionHeader, { marginTop: 30 }]}>
                    <Text style={[styles.sectionTitle, { color: '#0EA5E9' }]}>Explore Tasks</Text>
                </View>

                {filteredExploreTasks.map((task: any) => {
                    // Calculate progress
                    const totalImages = task.progress?.totalImages ?? task.totalImages ?? 0;
                    const imagesClassified = task.progress?.imagesClassified ?? task.imagesClassified ?? 0;
                    const progress = totalImages > 0 ? Math.round((imagesClassified / totalImages) * 100) : 0;

                    return (
                        <TaskCard
                            key={task.taskId}
                            title={task.name}
                            description={task.description}
                            species={Array.isArray(task.targetSpecies) ? task.targetSpecies.map((s: any) => s.name ?? s) : []}
                            imagesClassified={imagesClassified}
                            progress={progress}
                            onPlay={() => handleAddTask(task.taskId)}
                            onPress={() => handleTaskPress(task)}
                            actionType="add"
                        />
                    );
                })}
                {filteredExploreTasks.length === 0 && (
                    <Text style={[styles.emptyState, { color: themeColors.textSecondary }]}>No public tasks available.</Text>
                )}

            </ScrollView>
        </ScreenHeaderLayout>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#fff',
    },
    scrollContent: {
        padding: 16,
        paddingBottom: 80, // Space for bottom bar
    },
    sectionHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 20,
        marginTop: 10,
    },
    titleBlock: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    sectionTitle: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#004d40', // Dark teal
    },
    completionBlock: {
        alignItems: 'center',
    },
    completionText: {
        fontSize: 12,
        color: '#666',
    },
    emptyState: {
        textAlign: 'center',
        marginTop: 40,
        color: '#888',
        fontSize: 16,
    }
});
