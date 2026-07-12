# Exercises — Git & GitHub Basics

These exercises use a brand-new **practice repository** you create yourself — not a real project — so it's safe to experiment freely. Do them in order.

## Setup — create a scratch repository

```bash
mkdir git-practice
cd git-practice
git init
git config user.name "Your Name"
git config user.email "your.email@example.com"
```

`git init` turns an ordinary folder into a Git repository — this is different from `git clone`, since there's no existing project to copy; you're starting from nothing.

## Exercise 1 — Your first commit

1. Create a file called `notes.txt` with any text inside it.
2. Run `git status`. What category does `notes.txt` show up under?
3. Stage it, then commit it with a short message.
4. Run `git status` again.

<details>
<summary>Answer</summary>

```bash
echo "hello git" > notes.txt
git status
# shows notes.txt under "Untracked files"

git add notes.txt
git commit -m "Add notes.txt"

git status
# nothing to commit, working tree clean
```
</details>

## Exercise 2 — See your history

Run:

```bash
git log --oneline
```

<details>
<summary>What am I looking at?</summary>

One line per commit, newest at the top, each with a short identifier (a "commit hash") and its message. Right now you should see just your one commit from Exercise 1.
</details>

## Exercise 3 — Make a change and see the difference

1. Add another line to `notes.txt`.
2. Before staging anything, run `git diff`.
3. Then stage and commit it.

<details>
<summary>What should git diff show?</summary>

It shows the exact lines added (usually marked with a `+`) — this is how you can double-check *exactly* what you're about to stage, before you do.
</details>

## Exercise 4 — Create and use a branch

1. Create a new branch called `add-todo-list`, and switch to it.
2. Create a new file `todo.txt` with a couple of lines in it.
3. Stage and commit it.
4. Run `git log --oneline` — do you see this new commit?
5. Switch back to your original branch (probably called `main` or `master`) with `git checkout main`.
6. Run `ls` — is `todo.txt` there?

<details>
<summary>Answer</summary>

```bash
git checkout -b add-todo-list
echo "buy milk" > todo.txt
git add todo.txt
git commit -m "Add todo list"
git log --oneline
# shows this new commit

git checkout main
ls
# todo.txt is NOT here — it only exists on the add-todo-list branch
```

This is the whole point of a branch: `main` is completely unaffected until you merge.
</details>

## Exercise 5 — Merge it locally

While on `main`, merge your branch in:

```bash
git merge add-todo-list
```

Run `ls` again.

<details>
<summary>What should happen?</summary>

`todo.txt` should now appear on `main` too — the branch's commit has been combined in. Since `main` hadn't changed since you branched off, this is a simple "fast-forward" merge (no conflict possible).
</details>

## Exercise 6 — Create a real merge conflict (on purpose!)

This is the exercise most beginners are afraid of — but it's much less scary once you've done it once on purpose, with nothing important at stake.

1. On `main`, open `notes.txt` and change its first line to `"Line changed on main"`. Commit it.
2. Create a new branch `edit-notes` from **before** that change — actually, for this exercise, instead: create the branch first, change the first line there to `"Line changed on branch"`, commit it, *then* go back to `main` and make a *different* change to that same first line, and commit that too. Now both `main` and `edit-notes` have changed the same line, differently.
3. Try to merge: `git merge edit-notes`.

<details>
<summary>What happens?</summary>

Git will report a merge conflict and mark the file. Open `notes.txt` — you'll see something like:

```
<<<<<<< HEAD
Line changed on main
=======
Line changed on branch
>>>>>>> edit-notes
```

To resolve it: edit the file so it says whatever you actually want the final result to be (delete the `<<<<<<<`, `=======`, and `>>>>>>>` markers too), then:

```bash
git add notes.txt
git commit
```

That completes the merge. This is exactly the situation described in file 04 — now you've seen it happen for real, in a safe practice repo.
</details>

## Exercise 7 — Quiz

1. What's the difference between `git add` and `git commit`?
2. What's the difference between `git commit` and `git push`?
3. Why does creating a branch not affect `main` at all, until you merge?

<details>
<summary>Answers</summary>

1. `git add` stages a change (picks it to be included in the *next* commit); `git commit` actually saves that staged snapshot permanently, with a message.
2. `git commit` only saves the snapshot on your own computer; `git push` sends your commits to GitHub (or wherever your remote is) so others can see them.
3. A branch is a separate line of commits — `main` simply doesn't include any of the new commits on your branch until you explicitly merge them in.
</details>
