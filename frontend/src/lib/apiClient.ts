import axios, { type InternalAxiosRequestConfig, type AxiosError } from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";

export const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

// Request Interceptor: Attach JWT Bearer header
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem("token");
    if (token && config.headers){
        config.headers.Authorization = `Bearer ${token}`
    }
    return config;
}, (error: AxiosError) => Promise.reject(error)
);


// Response Interceptor: Handle global 401s
apiClient.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
        if (error.response?.status === 401) {
            localStorage.removeItem("token");
            // Optional: window.location.href = "/login";
        }
        return Promise.reject(error);
    }
);
