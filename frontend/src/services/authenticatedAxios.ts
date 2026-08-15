import axios, { AxiosInstance, CreateAxiosDefaults } from 'axios';

type GetAccessToken = (minValidity: number) => Promise<string | undefined>;
type Login = (returnTo?: string) => Promise<void>;

let getAccessToken: GetAccessToken | undefined;
let login: Login | undefined;
let loginRedirectStarted = false;

export function configureAuthentication(tokenProvider: GetAccessToken, loginHandler: Login): void {
  getAccessToken = tokenProvider;
  login = loginHandler;
  loginRedirectStarted = false;
}

export function createAuthenticatedAxios(config?: CreateAxiosDefaults): AxiosInstance {
  const client = axios.create(config);

  client.interceptors.request.use(async (request) => {
    if (!getAccessToken) {
      throw new Error('Authentication has not been initialized');
    }

    const token = await getAccessToken(30);
    if (!token) {
      throw new Error('Unable to obtain an access token');
    }

    request.headers.set('Authorization', `Bearer ${token}`);
    return request;
  });

  client.interceptors.response.use(
    (response) => response,
    async (error) => {
      if (axios.isAxiosError(error) && error.response?.status === 401 && !loginRedirectStarted) {
        loginRedirectStarted = true;
        await login?.(window.location.pathname + window.location.search);
      }
      return Promise.reject(error);
    },
  );

  return client;
}
