import React, { useMemo } from 'react';
import { StyleSheet, Text, TouchableOpacity, View, ScrollView, Platform, ActivityIndicator } from 'react-native';
import { Colors } from '../../../../constants/theme';
import { useThemeStore } from '../../../stores/themeStore';
import { SpeciesRefImage, StepSpeciesProps } from './addTaskTypes';
import MultiSelect from '../../../components/ui/MultiSelect';
import SpeciesImagePicker from './SpeciesImagePicker';

export default function StepSpecies({
  formData,
  onUpdate,
  onNext,
  onBack,
  availableSpecies = [],
  optionsLoading = false,
  poolImages = {},
  poolImagesLoading = false,
}: StepSpeciesProps) {
  const { theme } = useThemeStore();
  const c = Colors[theme as keyof typeof Colors];
  const isWeb = Platform.OS === 'web';

  // Every selected species must have 1-3 reference images
  const canProceed = useMemo(() => {
    if (formData.speciesList.length === 0) return false;
    return formData.speciesList.every((id) => {
      const imgs = formData.speciesReferenceImages[id] ?? [];
      return imgs.length >= 1 && imgs.length <= 3;
    });
  }, [formData.speciesList, formData.speciesReferenceImages]);

  const handleImagesChange = (speciesId: string, images: SpeciesRefImage[]) => {
    onUpdate({
      speciesReferenceImages: {
        ...formData.speciesReferenceImages,
        [speciesId]: images,
      },
    });
  };

  // List of selected species with their label — for rendering pickers below the multiselect
  const selectedSpeciesDetails = useMemo(() =>
    formData.speciesList.map((id) => ({
      id,
      label: availableSpecies.find((s) => String(s.id) === id)?.label ?? id,
    })),
    [formData.speciesList, availableSpecies]
  );

  return (
    <View style={[styles.container, isWeb && styles.containerWeb]}>
      <ScrollView
        style={{ flex: 1 }}
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
      >
        <Text style={[styles.heading, { color: c.text }, isWeb && styles.headingWeb]}>
          Choose species to label
        </Text>
        <Text style={[styles.subtitle, { color: c.textSecondary }, isWeb && styles.subtitleWeb]}>
          Add the species classifiers will identify, then attach 1–3 reference images per species.
        </Text>

        <MultiSelect
          options={availableSpecies}
          selectedIds={formData.speciesList}
          onToggle={(id) => {
            const sid = id as string;
            const newSpeciesList = formData.speciesList.includes(sid)
              ? formData.speciesList.filter((s) => s !== sid)
              : [...formData.speciesList, sid];
            onUpdate({ speciesList: newSpeciesList });
          }}
          placeholder="Search species..."
          loading={optionsLoading}
          emptyOnNoSearch={true}
        />

        {/* Reference image pickers — one per selected species */}
        {selectedSpeciesDetails.length > 0 && (
          <View style={styles.pickersSection}>
            <Text style={[styles.pickersHeading, { color: c.textSecondary }]}>
              REFERENCE IMAGES
            </Text>

            {poolImagesLoading && (
              <View style={styles.poolLoadingRow}>
                <ActivityIndicator size="small" color="#10B981" />
                <Text style={[styles.poolLoadingText, { color: c.textSecondary }]}>
                  Loading pool images...
                </Text>
              </View>
            )}

            {selectedSpeciesDetails.map(({ id, label }) => (
              <SpeciesImagePicker
                key={id}
                speciesId={id}
                speciesLabel={label}
                selectedImages={formData.speciesReferenceImages[id] ?? []}
                poolImages={poolImages[id] ?? []}
                poolLoading={poolImagesLoading}
                onImagesChange={handleImagesChange}
              />
            ))}
          </View>
        )}
      </ScrollView>

      <View style={[styles.footer, isWeb && styles.footerWeb]}>
        {/* Validation hint */}
        {formData.speciesList.length > 0 && !canProceed && (
          <Text style={styles.validationHint}>
            Each species needs 1–3 reference images before continuing.
          </Text>
        )}

        <View style={styles.buttonRow}>
          <TouchableOpacity
            style={[styles.backButton, { borderColor: c.border }]}
            onPress={onBack}
          >
            <Text style={[styles.backButtonText, { color: c.text }]}>← Back</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.nextButton, !canProceed && styles.buttonDisabled]}
            onPress={onNext}
            disabled={!canProceed}
          >
            <Text style={styles.nextButtonText}>Next →</Text>
          </TouchableOpacity>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container:         { flex: 1, paddingVertical: 16 },
  containerWeb:      { paddingVertical: 8 },
  scrollContent:     { paddingBottom: 16 },
  heading:           { fontSize: 22, fontWeight: '700', marginBottom: 8 },
  headingWeb:        { fontSize: 20, marginBottom: 6 },
  subtitle:          { fontSize: 14, marginBottom: 24, lineHeight: 20 },
  subtitleWeb:       { marginBottom: 16 },
  pickersSection:    { marginTop: 20, gap: 4 },
  pickersHeading:    { fontSize: 11, fontWeight: '700', letterSpacing: 0.8, marginBottom: 8 },
  poolLoadingRow:    { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 12 },
  poolLoadingText:   { fontSize: 13 },
  footer:            { paddingTop: 16 },
  footerWeb:         { paddingTop: 12 },
  validationHint:    { color: '#f59e0b', fontSize: 12, textAlign: 'center', marginBottom: 8 },
  buttonRow:         { flexDirection: 'row', gap: 12 },
  backButton:        { flex: 1, paddingVertical: 14, borderRadius: 12, alignItems: 'center', borderWidth: 1 },
  backButtonText:    { fontWeight: '700', fontSize: 16 },
  nextButton:        { flex: 1, backgroundColor: '#10B981', paddingVertical: 14, borderRadius: 12, alignItems: 'center' },
  buttonDisabled:    { backgroundColor: '#94D3B3' },
  nextButtonText:    { color: '#fff', fontWeight: '700', fontSize: 16 },
});
