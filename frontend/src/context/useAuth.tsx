import { useAuthStore } from "../store/useAuthStore";

export type { User } from "../features/auth/types/auth.types";

export function useAuth() {
    return useAuthStore();
}

export default useAuth;