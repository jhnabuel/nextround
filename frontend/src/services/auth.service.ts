import { apiClient } from "../lib/apiClient";
import type {
    RegisterRequest,
    LoginRequest,
    AuthResponse,
    User,
} from "../features/auth/types/auth.types";

export const authService = {
    /**
     * Register a new user account
     */
    async register(payload: RegisterRequest): Promise<AuthResponse> {
        const response = await apiClient.post<AuthResponse>("/auth/register", payload);
        return response.data;
    },

    /**
     * Authenticate an existing user
     */
    async login(payload: LoginRequest): Promise<AuthResponse> {
        const response = await apiClient.post<AuthResponse>("/auth/login", payload);
        return response.data;
    },

    /**
     * Retrieve currently authenticated user profile
     */
    async getCurrentUser(): Promise<User> {
        const response = await apiClient.get<User>("/users/me");
        return response.data;
    },
};