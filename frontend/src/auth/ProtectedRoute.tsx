import { FC, PropsWithChildren, useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import { useAuthContext } from '../context/AuthContext';

const ProtectedRoute: FC<PropsWithChildren> = ({ children }) => {
  const { initialized, isAuthenticated, login } = useAuthContext();
  const location = useLocation();
  const loginStarted = useRef(false);

  useEffect(() => {
    if (!initialized || isAuthenticated || loginStarted.current) {
      return;
    }

    loginStarted.current = true;
    void login(location.pathname + location.search);
  }, [initialized, isAuthenticated, location.pathname, location.search, login]);

  if (!initialized || !isAuthenticated) {
    return null;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
