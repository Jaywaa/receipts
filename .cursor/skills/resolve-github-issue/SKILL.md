---
name: resolve-github-issue
description: Fetch the next open GitHub issue, evaluate it against the codebase, plan and implement a fix, verify it compiles, and commit/push to main. Use when the user asks to work on the next issue, resolve a GitHub issue, pick up an issue, or fix the next bug.
---

# Resolve Next GitHub Issue

Automated workflow: fetch an open issue, evaluate, plan, implement, verify, and push to main.

## Prerequisites

- `gh` CLI authenticated (`gh auth status`)
- Git repo initialized with a GitHub remote
- On the `main` branch with a clean working tree

## First: Create a todo checklist

Before starting, create a `TodoWrite` checklist to track progress:

```
1. Sync main and fetch next issue        [in_progress]
2. Explore codebase and evaluate issue    [pending]
3. Plan the solution                      [pending]
4. Implement                              [pending]
5. Verify build and lints                 [pending]
6. Commit and push to main                [pending]
7. Comment summary on the issue           [pending]
8. Confirm issue closure                  [pending]
```

Update each todo as you progress. Only one should be `in_progress` at a time.

## Workflow

### Step 1: Sync main and fetch the next open issue

Pull latest before doing anything:

```bash
git checkout main
git pull --rebase origin main
```

Then fetch open issues, excluding any that already have linked pull requests:

```bash
gh issue list --state open --limit 10 --json number,title,body,labels,assignees
```

For each candidate, check if it already has a linked PR:

```bash
gh issue view <number> --json closedByPullRequests --jq '.closedByPullRequests | length'
```

Pick the oldest unassigned issue with no linked PRs (lowest number). If all are assigned, pick the oldest assigned to the current user. Show the user which issue you're picking up and why.

### Step 2: Explore the codebase and evaluate the issue

This step is critical — do not skip or rush it.

**2a. Read project conventions.** Read the project's `AGENTS.md` (if it exists) to understand architecture, conventions, and key paths.

**2b. Explore relevant code.** Use the Task tool with `subagent_type="explore"` to investigate the areas of the codebase related to the issue. Be specific in your explore prompt — include the issue title, description, and what you need to find out. Set thoroughness to "medium" or "very thorough" depending on issue complexity.

Example explore prompt:
> "Issue #N: '<title>'. <body summary>. Find all code related to <topic>. Identify the relevant files, current behavior, and how the code is structured in this area. Report back file paths, key functions, and any existing patterns."

If the issue touches multiple unrelated areas, launch parallel explore subagents.

**2c. Evaluate.** With the exploration results, determine:

- **Validity**: Does the issue describe a real problem or reasonable feature given the current code?
- **Scope**: Which files and modules are affected?
- **Complexity**: Small fix, moderate change, or large feature?

If the issue is invalid or already resolved, comment on it and close it:

```bash
gh issue close <number> --comment "This appears to be resolved / not applicable because ..."
```

Then move on to the next issue.

### Step 3: Plan the solution

Before writing code, outline:

1. **Root cause / rationale** — why the change is needed
2. **Approach** — what you'll change and why this approach over alternatives
3. **Files to modify** — specific files and what changes each needs
4. **Risk areas** — anything that could break or needs extra care

Follow established patterns discovered during exploration rather than introducing new ones. Respect the conventions in `AGENTS.md`.

### Step 4: Implement

Make the changes following the plan. Key rules:

- Follow existing code style and architecture
- Keep changes minimal and focused on the issue
- Don't refactor unrelated code
- Add or update tests if the project has them
- Reference the issue number in code comments only if it adds clarity

### Step 5: Verify it compiles

Run the project's build command to confirm nothing is broken.

If the build fails, fix the errors and re-verify. Do not proceed until the build succeeds.

Also run `ReadLints` on all modified files and fix any introduced linter errors.

### Step 6: Commit and push to main

Ensure you're on `main`:

```bash
git checkout main
```

Stage and commit with a message that references the issue:

```bash
git add -A
git commit -m "$(cat <<'EOF'
<type>: <concise summary>

<optional body explaining why, not what>

Closes #<issue_number>
EOF
)"
```

Commit types: `feat`, `fix`, `refactor`, `docs`, `chore`, `test`, `style`.

Push to remote:

```bash
git push origin main
```

### Step 7: Comment summary on the issue

Post a resolution summary as a comment on the issue so there's a clear record of what was done:

```bash
gh issue comment <number> --body "$(cat <<'EOF'
## Resolved in <commit_sha>

### Summary of changes

<1-3 bullet points explaining what changed and why>

### Files modified

- `path/to/file` — <brief description of change>
EOF
)"
```

### Step 8: Confirm closure

Verify the issue was auto-closed by the `Closes #N` keyword. If not, close it manually:

```bash
gh issue close <number> --comment "Resolved in <commit_sha>"
```

## Error Handling

- **No GitHub remote**: Ask the user to set one up (`gh repo create` or `git remote add origin ...`)
- **Not on main**: Stash changes, checkout main, pull latest, reapply
- **Merge conflicts on push**: Pull with rebase first (`git pull --rebase origin main`), resolve conflicts, then push
- **Build failure after fix attempts**: Report the issue to the user with the error output and ask for guidance

## Output

After completion, report to the user:
- Issue number and title resolved
- Summary of changes made
- Commit SHA
- Any notes or follow-up items
