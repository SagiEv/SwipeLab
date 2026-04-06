import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

const USE_MOCKS = __DEV__

export async function apiFetch(
  input: RequestInfo,
  init?: RequestInit
): Promise<Response> {
  const url = typeof input === 'string' ? input : input.url
  const method = (init?.method ?? 'GET').toUpperCase() as any

  // if (USE_MOCKS) {
  //   const mockResponse = await mockRouter(url, method, init)
  //   if (mockResponse) {
  //     console.log('[MOCK]', method, url)
  //     return mockResponse
  //   }
  // }


  const backendUrl = process.env.EXPO_PUBLIC_API_URL ||
    (Platform.OS === "web"
      ? "http://localhost:8080"
      : "http://172.20.10.8:8080"); //real IP for IOS&ANDROID


  // Get token from storage
  let token;
  if (Platform.OS === 'web') {
    token = localStorage.getItem("token");
  } else {
    token = await SecureStore.getItemAsync("token");
  }

  const fullUrl = backendUrl + input;
  console.log("[apiFetch] Full exact URL being fetch'ed:", fullUrl);
  console.log("[apiFetch] Header Token Present:", token ? "YES (length " + token.length + ")" : "NO (null/undefined)");

  return fetch(fullUrl, {
    ...init,
    headers: {
      ...(init?.headers ?? {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  })
}
