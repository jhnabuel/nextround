import {
    createContext,
    useContext,
    useState,
    useEffect,
    useCallback,
    type ReactNode,
} from "react";

import { authService } from "../services/auth.service";
import type {
    User,
    LoginRequest,
    RegisterRequest,
} from "../features/auth/auth.types";

export interface User {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
}

interface AuthContextType {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (credentials: LoginRequest) => Promise<void>;
    register: (payload: RegisterRequest) => Promise<void>;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const TOKEN_KEY = "token";

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(() =>
        localStorage.getItem(TOKEN_KEY)
    );
    const [isLoading, setIsLoading] = useState<boolean>(true);

    // 1. Session Restoration on App Mount
    useEffect(() => {
        async function restoreSession() {
            const storedToken = localStorage.getItem(TOKEN_KEY);

            if (!storedToken) {
                setIsLoading(false);
                return;
            }

            try {
                const currentUser = await authService.getCurrentUser();
                setUser(currentUser);
                setToken(storedToken);
            } catch {
                // Token invalid, expired, or rejected with 401
                localStorage.removeItem(TOKEN_KEY);
                setToken(null);
                setUser(null);
            } finally {
                setIsLoading(false);
            }
        }

        restoreSession();
    }, []);

    // 2. Login Flow
    const login = useCallback(async (credentials: LoginRequest) => {
        setIsLoading(true);
        try {
            const authData = await authService.login(credentials);
            localStorage.setItem(TOKEN_KEY, authData.token);
            setToken(authData.token);
            setUser(authData.user);
        } finally {
            setIsLoading(false);
        }
    }, []);

    // 3. Register Flow
    const register = useCallback(async (payload: RegisterRequest) => {
        setIsLoading(true);
        try {
            const authData = await authService.register(payload);
            localStorage.setItem(TOKEN_KEY, authData.token);
            setToken(authData.token);
            setUser(authData.user);
        } finally {
            setIsLoading(false);
        }
    }, []);

    // 4. Logout Flow
    const logout = useCallback(() => {
        localStorage.removeItem(TOKEN_KEY);
        setToken(null);
        setUser(null);
    }, []);

    const value: AuthContextType = {
        user,
        token,
        isAuthenticated: Boolean(token && user),
        isLoading,
        login,
        register,
        logout,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextType {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
}