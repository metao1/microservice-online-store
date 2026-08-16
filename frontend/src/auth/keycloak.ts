import Keycloak from 'keycloak-js';
import { readKeycloakConfig } from './keycloakConfig';

export const keycloak = new Keycloak(readKeycloakConfig(import.meta.env));
