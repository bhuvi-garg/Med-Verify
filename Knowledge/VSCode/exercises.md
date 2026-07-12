# Exercises — VS Code Basics

## Exercise 1 — Find the parts of the window

Open VS Code (any way you like) with any folder open. Find and click on:

1. The Sidebar / Explorer.
2. The Extensions icon in the activity bar.
3. The Status Bar (bottom edge).

<details>
<summary>What am I looking for?</summary>

The Sidebar is the panel on the left showing files and folders. The Extensions icon looks like four small squares, usually in the far-left vertical bar. The Status Bar is the thin strip along the very bottom of the window — it usually shows things like the current branch (if it's a Git project) and, for WSL projects, your remote connection.
</details>

## Exercise 2 — Open the integrated terminal

With VS Code open, use the keyboard shortcut `` Ctrl+` `` to open the integrated terminal.

Run:

```bash
pwd
```

<details>
<summary>What should this look like?</summary>

If VS Code is open normally (not in WSL mode), this might print a Windows-style path. Keep this result — you'll compare it in Exercise 3.
</details>

## Exercise 3 — Open the same project in WSL mode

1. Close VS Code.
2. Open a WSL terminal.
3. `cd` into the same project folder, using its `/mnt/...` path.
4. Run `code .`.
5. Once VS Code opens, check the bottom-left corner for the green **WSL: Ubuntu** badge.
6. Open the integrated terminal again (`` Ctrl+` ``) and run `pwd`.

<details>
<summary>What should be different this time?</summary>

This time, `pwd` inside VS Code's terminal should print a Linux-style path (starting with `/mnt/...` or similar) — because VS Code itself, and its terminal, are now running inside WSL. Compare this to what you saw in Exercise 2.
</details>

## Exercise 4 — Quiz

True or false: once VS Code shows the green "WSL: Ubuntu" badge, every file you edit is a separate copy from the "real" one on your Windows drive.

<details>
<summary>Answer</summary>

**False.** There's only one copy of the file. `/mnt/d/...` and `D:\...` are two different *names* for the exact same file on the exact same disk — editing it through VS Code's WSL connection changes the same file Windows would show you at the equivalent `D:\...` path.
</details>
