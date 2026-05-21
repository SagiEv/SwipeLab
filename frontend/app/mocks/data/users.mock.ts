export interface User {
  id: string; // username
  username: string;
  description?: string; // e.g. "Student" or "Expert"
  score: number;         // credibility score (0–100)
}

export const usersMock: User[] = [
  { id: 'user_1',       username: 'user_1',       description: 'Student',    score: 20 },
  { id: 'user_2',       username: 'user_2',       description: 'Student',    score: 35 },
  { id: 'expert_1',     username: 'expert_1',     description: 'Expert',     score: 95 },
  { id: 'volunteer_a',  username: 'volunteer_a',  description: 'Volunteer',  score: 50 },
  { id: 'volunteer_b',  username: 'volunteer_b',  description: 'Volunteer',  score: 62 },
  { id: 'researcher_x', username: 'researcher_x', description: 'Researcher', score: 80 },
  { id: 'researcher_y', username: 'researcher_y', description: 'Researcher', score: 73 },
  { id: 'student_bonus',username: 'student_bonus',description: 'Student',    score: 45 },
];
