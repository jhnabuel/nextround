import { RouterProvider } from 'react-router-dom';
import './App.css'
import { router } from "./routes";
import { useEffect } from 'react';
import { useAuthStore } from "./store/useAuthStore";

function App() {
    const restoreSession = useAuthStore((state) => state.restoreSession);

    useEffect(()=>{
        restoreSession();
    }, [restoreSession]);


  return (
      <RouterProvider router={router} />
    )
}

export default App
