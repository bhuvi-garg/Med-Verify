# Basic Linux Commands

You'll use these commands constantly, in every project, forever. Try each one right now in your WSL terminal.

| Command | What it means | What it does |
|---|---|---|
| `pwd` | **P**rint **W**orking **D**irectory | Shows you the full path of the folder you're currently "standing in." |
| `ls` | **L**i**s**t | Shows you the files and folders inside your current folder. |
| `ls -la` | List, "long" + "all" | Same as `ls`, but shows more detail and even hidden files (ones starting with a `.`, like `.git`). |
| `cd <folder>` | **C**hange **D**irectory | Moves you *into* a folder. |
| `cd ..` | Change Directory, up one | Moves you *up* one folder (to the parent). |
| `cd ~` | Change Directory, home | Jumps straight back to your home folder, from anywhere. |
| `mkdir <name>` | **M**a**k**e **Dir**ectory | Creates a new, empty folder. |
| `clear` | — | Clears all the clutter off your terminal screen. Doesn't undo anything, just tidies up what you can see. |

## A worked example

```bash
pwd
# /home/yourname

cd /mnt/d/Projects
ls
# MyProject   (and maybe other folders)

cd MyProject
pwd
# /mnt/d/Projects/MyProject

ls -la
# shows every file and folder, including any hidden ones like .git
```

Notice: once you're already inside `/mnt/d/Projects`, you can just type `cd MyProject` — you don't have to retype the whole path every time, only the part that gets you from *where you are* to *where you want to go*.

## Relative vs. absolute paths

- An **absolute path** always starts from the very top (`/`) — for example `/mnt/d/Projects/MyProject`. It works no matter where you currently are.
- A **relative path** is written *relative to* where you currently are — for example, if you're already standing in `/mnt/d/Projects`, then `MyProject` (or `./MyProject`) means the same thing.
- `cd ..` and `cd ~` are also relative — they depend on where you're standing when you run them.

When in doubt, run `pwd` first so you know exactly where you're starting from.

**Next:** [Exercises](exercises.md) — try these commands for yourself.
