# External Product Seed Design

## Context

The inventory `generator` profile loads a 10 MB product dataset from `classpath:data/products.txt`, while the boot JAR intentionally excludes `data/**`. This makes the generator profile fail inside the container.

## Design

- Keep the dataset outside the application JAR.
- Bind-mount `inventory-microservice/src/main/resources/data/products.txt` into the inventory container at `/app/data/products.txt` as read-only.
- Configure `ProductGenerator` through a Spring `Resource` property:
  - Compose: `file:/app/data/products.txt`
  - Default: `classpath:data/products.txt` for IDE and tests.
- Create the generator only under the existing `generator` profile.
- Validate resource readability during generator startup and fail with a clear configuration error when the profile is enabled without the file.

## Testing

- A focused unit test verifies configured resource loading and the missing-resource error.
- Compose configuration verifies the read-only bind mount and external resource property.
- A rebuilt inventory container verifies the generator no longer logs `FileNotFoundException`.

## Non-Goals

- No database migration or production seed mechanism.
- No named Docker volume; the dataset remains version-controlled and host-readable.
- No change to normal inventory startup when the `generator` profile is disabled.
