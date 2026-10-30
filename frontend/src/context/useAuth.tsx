import React, { createContext, useContext, useState, type ReactNode } from "react";

export interface User {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
}

interface AuthContextType {
    user: User | null;
    token: string | null;
    loading: boolean;
    login: (token: string, user: User) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function getInitialAuth(): { token: string | null; user: User | null } {
    try {
        const savedToken = localStorage.getItem("authToken");
        const savedUser = localStorage.getItem("authUser");

        if (savedToken && savedUser) {
            return {
                token: savedToken,
                user: JSON.parse(savedUser) as User,
            };
        }
    } catch {
        localStorage.removeItem("authToken");
        localStorage.removeItem("authUser");
    }

    return { token: null, user: null };
}

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [authState, setAuthState] = useState(getInitialAuth);
    // Initial sync read from localStorage completes before first render
    const loading = false;

    const login = (newToken: string, newUser: User) => {
        localStorage.setItem("authToken", newToken);
        localStorage.setItem("authUser", JSON.stringify(newUser));
        setAuthState({ token: newToken, user: newUser });
    };

    const logout = () => {
        localStorage.removeItem("authToken");
        localStorage.removeItem("authUser");
        setAuthState({ token: null, user: null });
    };

    return (
        <AuthContext.Provider
            value={{
                user: authState.user,
                token: authState.token,
                loading,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
};