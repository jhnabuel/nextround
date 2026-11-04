import { create } from 'zustand';
import { authService } from "../services/auth.service"
import type { User, LoginRequest, RegisterRequest } from "../features/auth/types/auth.types"

interface AuthState {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    isLoading: boolean;

    login: (credentials: LoginRequest) => Promise<void>;
    register: (payload: RegisterRequest) => Promise<void>;
    logout: () => void;
    restoreSession: () => Promise<void>;
    fetchCurrentUser: () => Promise<void>;
}

const TOKEN_KEY = "token";

export const useAuthStore = create<AuthState>((set, get) => ({
    user: null,
    token: localStorage.getItem(TOKEN_KEY),
    isAuthenticated: Boolean(localStorage.getItem(TOKEN_KEY)),
    isLoading: true,

    // 1. Login State Update
    login: async (credentials: LoginRequest) => {
        // DO NOT touch isLoading here; local isSubmitting handles the button spinner
        const response = await authService.login(credentials);
        localStorage.setItem(TOKEN_KEY, response.token);
        set({
            token: response.token,
            user: response.user,
            isAuthenticated: true,
        });
    },

    // 2. Register State Update
    register: async (payload: RegisterRequest) => {
        // DO NOT touch isLoading here; local isSubmitting handles the button spinner
        const response = await authService.register(payload);
        localStorage.setItem(TOKEN_KEY, response.token);
        set({
            token: response.token,
            user: response.user,
            isAuthenticated: true,
        });
    },

    // 3. Logout State Cleanup
    logout: () => {
        localStorage.removeItem(TOKEN_KEY);
        set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
        });
    },

    // 4. On-demand Current User Fetch
    fetchCurrentUser: async () => {
        try {
            const currentUser = await authService.getCurrentUser();
            set({ user: currentUser, isAuthenticated: true });
        } catch {
            get().logout();
        }
    },

    // 5. Restore Authentication on Application Reload & Handle Expired/Invalid JWTs
    restoreSession: async () => {
        const token = localStorage.getItem(TOKEN_KEY);
        if (!token) {
            set({ user: null, token: null, isAuthenticated: false, isLoading: false });
            return;
        }

        try {
            const currentUser = await authService.getCurrentUser();
            set({
                user: currentUser,
                token,
                isAuthenticated: true,
            });
        } catch {
            // Token expired or invalid -> execute state cleanup
            get().logout();
        } finally {
            set({ isLoading: false });
        }
    },
}));