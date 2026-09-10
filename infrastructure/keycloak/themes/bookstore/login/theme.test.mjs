import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

test('theme inherits supported Keycloak markup and loads storefront styles', () => {
  const root = new URL('./', import.meta.url);
  const properties = readFileSync(new URL('theme.properties', root), 'utf8');
  const css = readFileSync(new URL('resources/css/login.css', root), 'utf8');
  assert.match(properties, /^parent=keycloak\.v2$/m);
  assert.match(properties, /^styles=css\/styles\.css css\/login\.css$/m);
  assert.match(css, /#kc-login/);
  assert.match(css, /#keycloak-bg/);
  assert.match(css, /^#keycloak-bg \.pf-v5-c-login__container \{$/m);
  const mainSelector = /^#keycloak-bg \.pf-v5-c-login__main \{$/m;
  assert.match(css, mainSelector);
  assert.doesNotMatch('#keycloak-bg .pf-v5-c-login__main-body {', mainSelector);
  assert.match(css, /^#keycloak-bg \.pf-v5-c-login__main-body \{$/m);
  assert.match(css, /^#keycloak-bg \.pf-v5-c-alert\.pf-m-danger \{$/m);
  assert.match(css, /#kc-register-form #kc-form-buttons input\[type='submit'\]/);
  assert.match(css, /:focus-visible/);
  assert.match(css, /@media/);
});
