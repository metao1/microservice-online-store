export interface KeycloakEnvironment {
  readonly VITE_KEYCLOAK_URL?: string;
  readonly VITE_KEYCLOAK_REALM?: string;
  readonly VITE_KEYCLOAK_CLIENT_ID?: string;
}

const REQUIRED_VARIABLES = [
  'VITE_KEYCLOAK_URL',
  'VITE_KEYCLOAK_REALM',
  'VITE_KEYCLOAK_CLIENT_ID',
] as const;

export function readKeycloakConfig(env: KeycloakEnvironment) {
  const values = {
    VITE_KEYCLOAK_URL: env.VITE_KEYCLOAK_URL?.trim(),
    VITE_KEYCLOAK_REALM: env.VITE_KEYCLOAK_REALM?.trim(),
    VITE_KEYCLOAK_CLIENT_ID: env.VITE_KEYCLOAK_CLIENT_ID?.trim(),
  };
  const missing = REQUIRED_VARIABLES.filter((name) => !values[name]);

  if (missing.length > 0) {
    throw new Error(`Missing Keycloak configuration: ${missing.join(', ')}`);
  }

  return {
    url: values.VITE_KEYCLOAK_URL!,
    realm: values.VITE_KEYCLOAK_REALM!,
    clientId: values.VITE_KEYCLOAK_CLIENT_ID!,
  };
}
