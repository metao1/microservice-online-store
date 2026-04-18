# Test Coverage Improvement PR
```
./gradlew test
```bash
Run tests locally with:
## Preview

- Mocking: Unit tests mock, IT tests use real dependencies
- Edge Cases: Focus on business rule violations
- Test Organization: Layer-based (`domain/model/**`, `infrastructure/application/**`)
Reference the "Further Considerations" section from the plan:
## Further Notes

- [ ] Assertions use AssertJ fluent API
- [ ] No `@Ignore` tests without justification
- [ ] Domain events verified (if applicable)
- [ ] Edge cases are covered
- [ ] Test names are descriptive
- [ ] Tests follow the existing patterns in the codebase
- [ ] All new tests pass locally


