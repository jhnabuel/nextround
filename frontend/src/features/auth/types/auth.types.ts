export interface User {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
}

export interface RegisterRequest {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    tokenType: string;
    expiresIn?: number;
    user: User;
}

export interface ApiError {
    status: number;
    message: string;
    errors?: Record<string, string>;
    timestamp?: string;
}