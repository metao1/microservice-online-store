import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const realm = JSON.parse(readFileSync(new URL('./bookstore-realm.json', import.meta.url), 'utf8'));
const frontend = realm.clients.find((client) => client.clientId === 'bookstore-frontend');

test('realm requires PKCE and disables unsafe browser grants', () => {
  assert.equal(realm.realm, 'bookstore');
  assert.equal(realm.registrationAllowed, true);
  assert.equal(frontend.publicClient, true);
  assert.equal(frontend.standardFlowEnabled, true);
  assert.equal(frontend.implicitFlowEnabled, false);
  assert.equal(frontend.directAccessGrantsEnabled, false);
  assert.equal(frontend.attributes['pkce.code.challenge.method'], 'S256');
  assert.deepEqual(frontend.redirectUris, ['http://localhost:3000/*']);
  assert.deepEqual(frontend.webOrigins, ['http://localhost:3000']);
});

test('realm separates customer and administrator access', () => {
  const roleNames = realm.roles.realm.map((role) => role.name);
  assert.ok(roleNames.includes('CUSTOMER'));
  assert.ok(roleNames.includes('ADMIN'));
  assert.ok(realm.defaultRole.composite);
  assert.ok(realm.defaultRole.composites.realm.includes('CUSTOMER'));
});
