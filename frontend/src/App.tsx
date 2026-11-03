import { RouterProvider } from 'react-router-dom';
import './App.css'
import { router } from "./routes";
import { AuthProvider } from './context/useAuth';
import { useEffect } from 'react';
import { useAuthStore } from "./store/useAuthStore";

function App() {
    const restoreSession = useAuthStore((state) => state.restoreSession());

    useEffect(()=>{
        restoreSession();
    }, [restoreSession]);

    
  return (
      <AuthProvider>
        <RouterProvider router={router} />;)
      </AuthProvider>)
}

export default App
