import { FC, PropsWithChildren, useCallback, useEffect, useRef, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useAuthContext } from '../context/AuthContext';

const ProtectedRoute: FC<PropsWithChildren> = ({ children }) => {
  const { initialized, isAuthenticated, login } = useAuthContext();
  const location = useLocation();
  const loginStarted = useRef(false);
  const [loginFailed, setLoginFailed] = useState(false);

  const startLogin = useCallback(() => {
    if (loginStarted.current) return;

    loginStarted.current = true;
    setLoginFailed(false);
    void login(location.pathname + location.search).catch(() => {
      loginStarted.current = false;
      setLoginFailed(true);
    });
  }, [location.pathname, location.search, login]);

  useEffect(() => {
    if (initialized && !isAuthenticated) startLogin();
  }, [initialized, isAuthenticated, startLogin]);

  if (loginFailed) {
    return (
      <div role="alert">
        <p>Unable to start sign in. Please try again.</p>
        <button type="button" onClick={startLogin}>Retry</button>
      </div>
    );
  }

  if (!initialized || !isAuthenticated) {
    return null;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
