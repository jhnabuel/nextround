import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

interface User{
    id: string;
    email: string;
    firstName: string;
    lastName:string;
}

interface AuthContextType{
    user: User | null;
    token: string | null;
    loading: boolean;
    login: (token: string, user:User) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{children: ReactNode}> = ({children}) => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        try {
            const savedToken = localStorage.getItem('authToken');
            const savedUser = localStorage.getItem('authUser');

            if (savedToken && savedUser) {
                setToken(savedToken);
                setUser(JSON.parse(savedUser));
            }
        } catch {
            localStorage.removeItem('authToken');
            localStorage.removeItem('authUser');
        } finally {
            setLoading(false);
        }
    }, []);

    const login = (newToken: string, newUser: User) => {
        localStorage.setItem('authToken', newToken);
        localStorage.setItem('authUser', JSON.stringify(newUser));
        setToken(newToken);
        setUser(newUser);
    }

    const logout = () =>{
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUser');
        setToken(null);
        setUser(null);
    }

    return(
        <AuthContext.Provider value={{ user, token, loading, login, logout }}>
            {children}
        </AuthContext.Provider>
    );

};

// Custom hook for easier consumption
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};