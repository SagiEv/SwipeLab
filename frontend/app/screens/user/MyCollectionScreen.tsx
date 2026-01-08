import React, { useCallback, useEffect, useState } from 'react';
import {
    ActivityIndicator,
    RefreshControl,
    ScrollView,
    StyleSheet,
    Text,
    TouchableOpacity,
    View,
} from 'react-native';
import CollectionCard from '../../components/user/CollectionCard';
import ScreenHeaderLayout from '../../components/layout/ScreenHeaderLayout/ScreenHeaderLayout';
import { useCollectionStore } from '../../stores/collectionStore';
import { SwipeDirection } from '../../types';
import { useNavigation } from '@react-navigation/native';
import { useThemeStore } from '../../stores/themeStore';
import { Colors } from '../../../constants/theme';
// Filter options removed as we only show collected bugs


export default function MyCollectionScreen() {
    const { items, stats, isLoading, fetchCollection } = useCollectionStore();
    const [refreshing, setRefreshing] = useState(false);
    const navigation = useNavigation<any>();
    const { theme } = useThemeStore();
    const themeColors = Colors[theme as keyof typeof Colors];

    useEffect(() => {
        fetchCollection();
    }, [fetchCollection]);

    const onRefresh = useCallback(async () => {
        setRefreshing(true);
        await fetchCollection();
        setRefreshing(false);
    }, [fetchCollection]);


    if (isLoading && items.length === 0) {
        return (
            <View style={[styles.loadingContainer, { backgroundColor: themeColors.background }]}>
                <ActivityIndicator size="large" color="#4B7BE5" />
            </View>
        );
    }

    return (
        <ScreenHeaderLayout
            leftIcon={require('../../../assets/images/collection.png')}
            leftTitle="My Collection"
            rightIcon={require('../../../assets/images/stats.png')}
            rightTitle="Stats"
            onRightPress={() => navigation.navigate('Stats')}
            contentContainerStyle={{ padding: 0 }}
        >
            <ScrollView
                style={[styles.container, { backgroundColor: themeColors.background }]}
                contentContainerStyle={styles.content}
                refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
            >
                {/* Collection Summary */}
                <View style={styles.statsContainer}>
                    <View style={[styles.statCard, { backgroundColor: themeColors.card }]}>
                        <Text style={styles.statNumber}>{items.length}</Text>
                        <Text style={[styles.statLabel, { color: themeColors.textSecondary }]}>Species Collected</Text>
                    </View>
                </View>

                {/* Collection Grid */}
                {items.length === 0 ? (
                    <View style={styles.emptyState}>
                        <Text style={styles.emptyEmoji}>📷</Text>
                        <Text style={[styles.emptyTitle, { color: themeColors.text }]}>No items yet</Text>
                        <Text style={[styles.emptySubtitle, { color: themeColors.textSecondary }]}>
                            Complete tasks by identifying bugs to add them to your collection!
                        </Text>
                    </View>
                ) : (
                    <View style={styles.grid}>
                        {items.map((item) => (
                            <CollectionCard key={item.id} item={item} />
                        ))}
                    </View>
                )}

                <View style={{ height: 40 }} />
            </ScrollView>
        </ScreenHeaderLayout>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f7fa',
    },
    // Main scroll content centered with max width
    content: {
        padding: 16,
        width: '100%',
        maxWidth: 600, // Limit width on large screens (Web/Tablet)
        alignSelf: 'center',
    },
    loadingContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#f5f7fa',
    },
    statsContainer: {
        marginBottom: 20,
        alignItems: 'center',
    },
    statCard: {
        backgroundColor: '#fff',
        paddingVertical: 16,
        paddingHorizontal: 32,
        borderRadius: 16,
        alignItems: 'center',
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
        elevation: 3,
    },
    statNumber: {
        fontSize: 32,
        fontWeight: 'bold',
        color: '#4B7BE5',
    },
    statLabel: {
        fontSize: 14,
        color: '#6b7280',
        marginTop: 4,
        fontWeight: '600',
    },
    // Styles for filters removed

    grid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        justifyContent: 'flex-start',
        gap: '2%',
    },
    emptyState: {
        alignItems: 'center',
        paddingVertical: 60,
    },
    emptyEmoji: {
        fontSize: 64,
        marginBottom: 16,
    },
    emptyTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#1f2937',
        marginBottom: 8,
    },
    emptySubtitle: {
        fontSize: 14,
        color: '#6b7280',
        textAlign: 'center',
        paddingHorizontal: 40,
    },
});
