import React from 'react';
import { StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { Colors } from '../../../../constants/theme';
import { useThemeStore } from '../../../stores/themeStore';
import { StepProps } from './addTaskTypes';

export default function StepName({ formData, onUpdate, onNext }: StepProps) {
  const { theme } = useThemeStore();
  const themeColors = Colors[theme as keyof typeof Colors];
  const canProceed = formData.name.trim().length > 0;

  return (
    <View style={styles.container}>
      <Text style={[styles.heading, { color: themeColors.text }]}>What's the name of your task?</Text>
      <Text style={[styles.subtitle, { color: themeColors.textSecondary }]}>
        Choose a clear, descriptive name for the classification task.
      </Text>

      <TextInput
        style={[styles.input, { backgroundColor: themeColors.card, borderColor: themeColors.border, color: themeColors.text }]}
        value={formData.name}
        onChangeText={(text) => onUpdate({ name: text })}
        placeholder="e.g., Pollinator Identification Survey"
        placeholderTextColor={themeColors.textSecondary}
        autoFocus
      />

      <View style={styles.footer}>
        <TouchableOpacity
          style={[styles.nextButton, !canProceed && styles.buttonDisabled]}
          onPress={onNext}
          disabled={!canProceed}
        >
          <Text style={styles.nextButtonText}>Next →</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, paddingVertical: 16 },
  heading: { fontSize: 22, fontWeight: '700', marginBottom: 8 },
  subtitle: { fontSize: 14, marginBottom: 24, lineHeight: 20 },
  input: {
    borderWidth: 1,
    borderRadius: 12,
    padding: 14,
    fontSize: 16,
  },
  footer: { marginTop: 'auto', paddingTop: 24 },
  nextButton: {
    backgroundColor: '#10B981',
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
  },
  buttonDisabled: { backgroundColor: '#94D3B3' },
  nextButtonText: { color: '#fff', fontWeight: '700', fontSize: 16 },
});
