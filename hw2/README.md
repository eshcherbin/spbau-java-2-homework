# Nucleus VCS

[![Build Status](https://travis-ci.org/eshcherbin/spbau-java-2-homework.svg?branch=hw2)](https://travis-ci.org/eshcherbin/spbau-java-2-homework)

A simple Version Control System that provides some _core_ VCS functionality

## Usage

Command line options:
- `nucleus init [<path>]` — initialize repository in current directory or at given path
- `nucleus add <path>` — add a file to current working copy
- `nucleus remove <path>` — remove file from current working copy and filesystem
- `nucleus commit <message>` — commit current changes
- `nucleus branch [delete] <branchName>`
    * without `delete`: create a new branch
    * with `delete`: remove specified branch
- `nucleus checkout <revisionName>` — check out specified revision
- `nucleus merge <revisionName>` — merge specified revision into current working copy
- `nucleus log` — print log of commits in current branch
- `nucleus help` — print help

Shortcuts:

- `rm` — `remove` 
- `ci` — `commit` 
- `cout` — `checkout` 

## Structure

Internal repository information is stored in `.nuc` folder:

- `HEAD` — current revision name or reference
- `index` — files in current working copy
- `references/`
    * mappings from references' names to corresponding commits' names
- `objects/`
    * commit objects
    * tree objects
    * files stored in the repository
    
## Build

Use [Gradle](https://gradle.org) to build the project. `application` plugin is used to create executable script:

```
> ./gradlew installDist
> build/install/bin/nucleus
```


## Author

Egor Shcherbin

SPbAU Java course homework task

_Spring 2017_