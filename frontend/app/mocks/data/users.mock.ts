export interface User {
  id: string; // username
  username: string;
  description?: string; // e.g. "Student" or "Expert"
}

export const usersMock: User[] = [
  { id: 'user_1', username: 'user_1', description: 'Student' },
  { id: 'user_2', username: 'user_2', description: 'Student' },
  { id: 'expert_1', username: 'expert_1', description: 'Expert' },
  { id: 'volunteer_a', username: 'volunteer_a', description: 'Volunteer' },
  { id: 'volunteer_b', username: 'volunteer_b', description: 'Volunteer' },
  { id: 'researcher_x', username: 'researcher_x', description: 'Researcher' },
  { id: 'researcher_y', username: 'researcher_y', description: 'Researcher' },
  { id: 'student_bonus', username: 'student_bonus', description: 'Student' },
  // Add more as needed
];
