/**
 * Tests for useAssignTask cache-refresh behaviour (Issue: myTasksScreen stale cache).
 *
 * Why: invalidateQueries only marks a query stale; it will NOT trigger a network
 * request unless the component re-renders or window is re-focused.
 * refetchQueries(type:'active') forces an immediate fetch for every mounted subscriber,
 * which is what we want when the user presses the assign (+) button.
 */
import { renderHook, act } from '@testing-library/react-hooks';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';
import { useAssignTask, QUERY_KEYS } from '../queries';

// ── Mocks ────────────────────────────────────────────────────────────────────

jest.mock('expo-secure-store', () => ({
  getItemAsync: jest.fn(() => Promise.resolve('mock_token')),
}));

jest.mock('../../stores/authStore', () => ({
  useAuthStore: {
    getState: jest.fn(() => ({
      setIsBanned: jest.fn(),
      setSessionExpiredMessage: jest.fn(),
      logout: jest.fn(),
    })),
  },
}));

const mockFetch = jest.fn();
global.fetch = mockFetch;

// ── Helpers ──────────────────────────────────────────────────────────────────

const buildWrapper = (queryClient: QueryClient) => {
  return ({ children }: { children: React.ReactNode }) =>
    React.createElement(QueryClientProvider, { client: queryClient }, children);
};

// ── Tests ────────────────────────────────────────────────────────────────────

describe('useAssignTask', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
    });
    jest.clearAllMocks();
  });

  afterEach(() => {
    queryClient.clear();
  });

  // Happy flow: successful assignment triggers immediate refetch of all three queries
  it('refetches myTasks, availableTasks, and statistics on successful assignment', async () => {
    // Simulate a successful POST /tasks/:id/assign response
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ taskId: 42, name: 'Example' }),
      clone: () => ({ json: async () => ({}) }),
    });

    const refetchQueriesSpy = jest.spyOn(queryClient, 'refetchQueries');

    const wrapper = buildWrapper(queryClient);
    const { result } = renderHook(() => useAssignTask(), { wrapper });

    await act(async () => {
      await result.current.mutateAsync(42);
    });

    // All three cache buckets must be scheduled for an active refetch
    const calledKeys = refetchQueriesSpy.mock.calls.map(
      (args) => JSON.stringify((args[0] as any)?.queryKey),
    );

    expect(calledKeys).toContain(JSON.stringify(QUERY_KEYS.myTasks));
    expect(calledKeys).toContain(JSON.stringify(QUERY_KEYS.availableTasks));
    expect(calledKeys).toContain(JSON.stringify(QUERY_KEYS.statistics));
  });

  // Edge case: 409 Conflict (already assigned) must NOT trigger any cache refetch
  it('does NOT refetch queries on 409 (already assigned) error', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 409,
      json: async () => ({ message: 'Task already assigned' }),
      clone: () => ({ json: async () => ({}) }),
    });

    const refetchQueriesSpy = jest.spyOn(queryClient, 'refetchQueries');

    const wrapper = buildWrapper(queryClient);
    const { result } = renderHook(() => useAssignTask(), { wrapper });

    await act(async () => {
      try {
        await result.current.mutateAsync(42);
      } catch {
        // expected rejection
      }
    });

    expect(refetchQueriesSpy).not.toHaveBeenCalled();
  });
});
