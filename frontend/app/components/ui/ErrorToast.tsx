import React, { useEffect, useRef } from 'react';
import { Animated, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

interface ErrorToastProps {
    message: string;
    onDismiss: () => void;
    /** Auto-dismiss delay in ms. Defaults to 4000. */
    durationMs?: number;
}

/**
 * Generic animated error toast.
 * Slides in from the top, auto-dismisses after `durationMs`, and exposes a
 * manual dismiss button. No browser/OS dialogs — fully app-native.
 */
export default function ErrorToast({ message, onDismiss, durationMs = 4000 }: ErrorToastProps) {
    const opacity    = useRef(new Animated.Value(0)).current;
    const translateY = useRef(new Animated.Value(-20)).current;

    useEffect(() => {
        // Slide in
        Animated.parallel([
            Animated.timing(opacity,    { toValue: 1, duration: 300, useNativeDriver: true }),
            Animated.timing(translateY, { toValue: 0, duration: 300, useNativeDriver: true }),
        ]).start();

        // Auto-dismiss
        const timer = setTimeout(dismiss, durationMs);
        return () => clearTimeout(timer);
    }, []);

    const dismiss = () => {
        Animated.parallel([
            Animated.timing(opacity,    { toValue: 0, duration: 250, useNativeDriver: true }),
            Animated.timing(translateY, { toValue: -20, duration: 250, useNativeDriver: true }),
        ]).start(onDismiss);
    };

    return (
        <Animated.View
            style={[styles.container, { opacity, transform: [{ translateY }] }]}
            accessibilityRole="alert"
            accessibilityLabel={`Error: ${message}`}
        >
            <Ionicons name="alert-circle" size={20} color="#fca5a5" style={styles.icon} />
            <Text style={styles.message} numberOfLines={3}>{message}</Text>
            <TouchableOpacity onPress={dismiss} hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}>
                <Ionicons name="close" size={18} color="#fff9" />
            </TouchableOpacity>
        </Animated.View>
    );
}

const styles = StyleSheet.create({
    container: {
        position: 'absolute',
        top: 16,
        left: 16,
        right: 16,
        zIndex: 999,
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#7f1d1d',
        borderLeftColor: '#ef4444',
        borderLeftWidth: 4,
        borderRadius: 12,
        padding: 14,
        shadowColor: '#000',
        shadowOpacity: 0.3,
        shadowRadius: 10,
        elevation: 8,
    },
    icon: {
        marginRight: 10,
    },
    message: {
        flex: 1,
        color: '#fef2f2',
        fontSize: 13,
        lineHeight: 18,
    },
});
