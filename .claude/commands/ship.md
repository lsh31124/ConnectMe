Run `./gradlew test` first.

If tests FAIL: stop immediately and report the full test failure output. Do NOT proceed.

If tests PASS: then run the following steps in order:
1. Check the current branch name.
   - If on `main`: stop and warn — direct commits to main are not allowed.
   - If on `develop`: stop and warn — work on a feature/* branch and PR into develop.
   - Otherwise (feature/*, hotfix/*, release/*): continue.
2. `git add .`
3. `git commit -m "feat: $ARGUMENTS"` — if $ARGUMENTS is empty, ask the user for a commit message before committing.
4. `git push -u origin <current-branch>`
5. Remind the user to open a PR: `<current-branch>` → `develop` (or `main` for hotfix/*).

Report the result of each step to the user.