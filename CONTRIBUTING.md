:warning: *This is currently a stub, it will be expanded in the future.*

It's great that you are taking the time to contribute to this repository! :+1:

This document details some guidelines for reporting / managing issues, writing code, doing releases and such things. They aren't rules set in stone, so feel free to contribute changes!

## Reporting Issues ##

- Check if already reported
- Environment, versions (OS, Ansible, Rundeck, Plugin) etc.
- How to reproduce

## Managing Issues ##

Tell people to try a new release, if their request is addressed in it.

### Labels ###

The goal is to attach at least one label to every issue (even user-closed ones). This makes for a nice overview and some stats.

- Usually one of:
    - bug (something is broken and needs to be fixed)
    - enhancement (new or better functionality)
    - question (unclear if bug or enhancement / configuration question / general usage question)
- Or one of:
    - duplicate (a matching issue already exists, close and link to it)
    - meta (project management stuff)
- These can be added additionally:
    - stalled (no response for a while, will be closed soon)
    - nope (declined bug or enhancement)

## Code Style ##

TBD

## Releases ##

- Change version in [build.gradle line 2](build.gradle)
    - Refer to [Semantic Versioning](http://semver.org/) to determine which level to increment
- Build a new jar with gradle (run `gradlew jar`), it will be in `build/libs`
- Check if everything works
    - *Tests are a TODO, I'm working on some automated Docker testing atm*
- Commit and push with message like "v1.3.0"
- Draft a new release on GitHub
    - Tag version: 1.3.0
    - Release title: v1.3.0
    - Describe the notable changes
    - Upload the .jar (drag and drop for example)
- Publish the release
