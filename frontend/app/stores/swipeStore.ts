import { create } from 'zustand';

interface SwipeStore {
  dataBatch: any[];
  currentIndex: number;

  setBatch: (items: any[]) => void;
  nextCard: () => void;
  clearBatch: () => void;
  getCurrentImage: () => any | null;
}

export const useSwipeStore = create<SwipeStore>((set, get) => ({
  dataBatch: [],
  currentIndex: 0,

  setBatch: (items: any[]) => {
    set({ dataBatch: items, currentIndex: 0 });
  },

  nextCard: () => {
    set((state) => ({ currentIndex: state.currentIndex + 1 }));
  },

  clearBatch: () => {
    set({ dataBatch: [], currentIndex: 0 });
  },

  getCurrentImage: () => {
    const { dataBatch, currentIndex } = get();
    return dataBatch[currentIndex] || null;
  },
}));