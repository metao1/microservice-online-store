import { describe, expect, it } from 'vitest';
import { readKeycloakConfig } from './keycloakConfig';

describe('readKeycloakConfig', () => {
  it('returns valid Keycloak configuration', () => {
    expect(readKeycloakConfig({
      VITE_KEYCLOAK_URL: 'http://localhost:8080',
      VITE_KEYCLOAK_REALM: 'bookstore',
      VITE_KEYCLOAK_CLIENT_ID: 'bookstore-frontend',
    })).toEqual({
      url: 'http://localhost:8080',
      realm: 'bookstore',
      clientId: 'bookstore-frontend',
    });
  });

  it('reports every missing Keycloak variable', () => {
    expect(() => readKeycloakConfig({})).toThrow(
      'Missing Keycloak configuration: VITE_KEYCLOAK_URL, VITE_KEYCLOAK_REALM, VITE_KEYCLOAK_CLIENT_ID',
    );
  });
});
