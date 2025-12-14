import type { ReactNode } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import HomePage from './pages/HomePage';
import { useAuthStore } from './store/authStore';

function PrivateRoute({ children }: { children: ReactNode }) {
  const token = useAuthStore((state) => state.token);
  return token ? children : <Navigate to='/login' />;
}

function PublicRoute({ children }: { children: ReactNode }) {
  const token = useAuthStore((state) => state.token);
  return !token ? children : <Navigate to='/' />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path='/login'
          element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          }
        />
        <Route
          path='/register'
          element={
            <PublicRoute>
              <RegisterPage />
            </PublicRoute>
          }
        />
        <Route
          path='/'
          element={
            <PrivateRoute>
              <HomePage />
            </PrivateRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
