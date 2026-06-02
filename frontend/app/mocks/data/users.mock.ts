export interface User {
  id: string; // username
  username: string;
  description?: string; // e.g. "Student" or "Expert"
  credibilityScore: number; // composite credibility score (0–100)
}

export const usersMock: User[] = [
  { id: 'user_1',       username: 'user_1',       description: 'Student',    credibilityScore: 20 },
  { id: 'user_2',       username: 'user_2',       description: 'Student',    credibilityScore: 35 },
  { id: 'expert_1',     username: 'expert_1',     description: 'Expert',     credibilityScore: 95 },
  { id: 'volunteer_a',  username: 'volunteer_a',  description: 'Volunteer',  credibilityScore: 50 },
  { id: 'volunteer_b',  username: 'volunteer_b',  description: 'Volunteer',  credibilityScore: 62 },
  { id: 'researcher_x', username: 'researcher_x', description: 'Researcher', credibilityScore: 80 },
  { id: 'researcher_y', username: 'researcher_y', description: 'Researcher', credibilityScore: 73 },
  { id: 'student_bonus',username: 'student_bonus',description: 'Student',    credibilityScore: 45 },
];
