# Exercises — WSL & Linux Basics

Do these in order. Try each one yourself before checking the answer — click **Answer** to reveal it.

## Exercise 1 — Open a WSL terminal

Open Windows Terminal and start a new WSL tab (not a plain Windows one).

Run:

```bash
pwd
```

Write down what it prints.

<details>
<summary>What should this look like?</summary>

Something like `/home/yourname` — this is your Linux "home folder." If instead you saw a prompt starting with `PS C:\...>`, you opened a Windows tab by mistake — close it and open a WSL one instead.
</details>

## Exercise 2 — Convert a path

Without running anything yet, work out on paper: what is the WSL path for `D:\School\Projects\Robotics`?

<details>
<summary>Answer</summary>

`/mnt/d/School/Projects/Robotics`

Every `\` becomes `/`, and `D:` becomes `/mnt/d`.
</details>

## Exercise 3 — Navigate and list

Starting from your home folder, do the following, one command at a time:

1. Go to your `D:` drive's root using its WSL path.
2. List what's there.
3. Create a new folder called `wsl-practice`.
4. Move into it.
5. Print your current location to confirm you're in the right place.

<details>
<summary>Answer</summary>

```bash
cd /mnt/d
ls
mkdir wsl-practice
cd wsl-practice
pwd
# should print /mnt/d/wsl-practice
```
</details>

## Exercise 4 — Relative vs. absolute

You're currently sitting in `/mnt/d/wsl-practice` (from Exercise 3). Without typing the full path, move back up to `/mnt/d`. Then, in a single command, jump straight back to your home folder from wherever you are.

<details>
<summary>Answer</summary>

```bash
cd ..
# now in /mnt/d

cd ~
# now in your home folder, e.g. /home/yourname
```
</details>

## Exercise 5 — Quiz (no terminal needed)

For each pair, is it the **same folder**, or **different folders**?

1. `C:\Users\Sam\Documents` and `/mnt/c/Users/Sam/Documents`
2. `D:\Code` and `/mnt/c/Code`
3. `/mnt/d/Projects` and `D:\Projects`

<details>
<summary>Answers</summary>

1. **Same** — correct `C:` → `/mnt/c` translation.
2. **Different** — this mixes up `D:` with `/mnt/c` (should be `/mnt/d`).
3. **Same** — correct `D:` → `/mnt/d` translation.
</details>
