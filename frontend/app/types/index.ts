// TypeScript types
export type SwipeDirection = 'yes' | 'no' | 'dont-know' | 'trash';

export interface Question {
  id: string;
  text: string;
  imageUrl: string;
  referenceImages: string[];
}

export interface SwipeResult {
  questionId: string;
  answer: SwipeDirection;
  timestamp: Date;
}

export interface CollectionItem {
  id: string;
  imageUrl: string;
  label: SwipeDirection;
  taskId: number;
  taskName: string;
  taskStatus: 'COMPLETED' | 'IN_PROGRESS';
  speciesName: string;
  scientificName: string;
  description: string;
  question: string;
  labeledAt: string;
}

export interface CollectionStats {
  total: number;
  yes: number;
  no: number;
  dontKnow: number;
  trash: number;
}
