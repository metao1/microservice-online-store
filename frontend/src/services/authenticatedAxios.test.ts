import axios, { AxiosError } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { configureAuthentication, createAuthenticatedAxios } from './authenticatedAxios';

describe('authenticated Axios', () => {
  const login = vi.fn<() => Promise<void>>().mockResolvedValue(undefined);

  beforeEach(() => {
    vi.clearAllMocks();
    window.history.replaceState({}, '', '/orders?page=2');
  });

  it('refreshes with 30 seconds minimum validity and sends the bearer token', async () => {
    const getAccessToken = vi.fn().mockResolvedValue('access-token');
    configureAuthentication(getAccessToken, login);
    const client = createAuthenticatedAxios({
      adapter: async (config) => ({
        data: config.headers.get('Authorization'),
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
      }),
    });

    const response = await client.get('/protected');

    expect(getAccessToken).toHaveBeenCalledWith(30);
    expect(response.data).toBe('Bearer access-token');
  });

  it('rejects a refresh failure without sending the request', async () => {
    const refreshError = new Error('refresh failed');
    const getAccessToken = vi.fn().mockRejectedValue(refreshError);
    const adapter = vi.fn();
    configureAuthentication(getAccessToken, login);
    const client = createAuthenticatedAxios({ adapter });

    await expect(client.get('/protected')).rejects.toBe(refreshError);
    expect(adapter).not.toHaveBeenCalled();
  });

  it('starts only one login redirect for repeated 401 responses and preserves the return URL', async () => {
    configureAuthentication(vi.fn().mockResolvedValue('access-token'), login);
    const client = createAuthenticatedAxios({
      adapter: async (config) => Promise.reject(new AxiosError(
        'unauthorized',
        'ERR_BAD_REQUEST',
        config,
        undefined,
        { data: null, status: 401, statusText: 'Unauthorized', headers: {}, config },
      )),
    });

    await expect(client.get('/first')).rejects.toMatchObject({ response: { status: 401 } });
    await expect(client.get('/second')).rejects.toMatchObject({ response: { status: 401 } });

    expect(login).toHaveBeenCalledTimes(1);
    expect(login).toHaveBeenCalledWith('/orders?page=2');
  });

  it('resets the 401 redirect guard when authentication is configured again', async () => {
    const unauthorizedAdapter = async (config: Parameters<NonNullable<ReturnType<typeof axios.create>['defaults']['adapter']>>[0]) =>
      Promise.reject(new AxiosError(
        'unauthorized',
        'ERR_BAD_REQUEST',
        config,
        undefined,
        { data: null, status: 401, statusText: 'Unauthorized', headers: {}, config },
      ));
    const getAccessToken = vi.fn().mockResolvedValue('access-token');
    configureAuthentication(getAccessToken, login);
    const client = createAuthenticatedAxios({ adapter: unauthorizedAdapter });
    await expect(client.get('/first')).rejects.toMatchObject({ response: { status: 401 } });

    configureAuthentication(getAccessToken, login);
    await expect(client.get('/after-initialization')).rejects.toMatchObject({ response: { status: 401 } });

    expect(login).toHaveBeenCalledTimes(2);
  });

  it('rejects 403 responses without redirecting to login', async () => {
    configureAuthentication(vi.fn().mockResolvedValue('access-token'), login);
    const client = createAuthenticatedAxios({
      adapter: async (config) => Promise.reject(new AxiosError(
        'forbidden',
        'ERR_BAD_REQUEST',
        config,
        undefined,
        { data: null, status: 403, statusText: 'Forbidden', headers: {}, config },
      )),
    });

    await expect(client.get('/protected')).rejects.toMatchObject({ response: { status: 403 } });
    expect(login).not.toHaveBeenCalled();
  });
});
