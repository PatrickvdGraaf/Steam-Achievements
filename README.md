# Steam-Achievements

This is the repo for my hobby project Steam Achievements (name under construction). The general 

## CI build status

<strong>Master</strong>
[![CircleCI](https://circleci.com/gh/PatrickvdGraaf/Steam-Achievements/tree/master.svg?style=svg)](https://circleci.com/gh/PatrickvdGraaf/Steam-Achievements/tree/master)

<strong>Develop</strong>
[![CircleCI](https://circleci.com/gh/PatrickvdGraaf/Steam-Achievements/tree/develop.svg?style=svg)](https://circleci.com/gh/PatrickvdGraaf/Steam-Achievements/tree/develop)


## Build

In order to build this project you need to add an API Key and an ID for a test user to your 
app/gradle.properties. <strong>Make sure that this file is added to your .gitignore and not 
accidentally committed.</strong>

Your app/gradle.properties should look like this; 
STEAM_API_KEY="your_api_key"
TEST_USER_ID="your_test_id"

An API key can be obtained from https://steamcommunity.com/dev/apikey.
You can find your own Steam ID with tools like https://steamidfinder.com.

You should now be able to build the app.


## Git hooks

We automatically insert git hooks that perform validations on the code quality. The following hooks 
are enabled:

- commit-msg: We validate whether your commit message follows the [guidelines](https://github.com/tommarshall/git-good-commit),
except we don't enforce the 50 characters rule. Instead, we recommend shooting for 50 characters but 
have a hard limit of 72 characters.
- pre-commit: We validate that lint checks and unit tests do not result in errors.

You should keep these validations enabled. There are situations where the `pre-commit` hook 
temporarily needs to be disabled, for instance when you want to push partial solutions to the remote 
for safekeeping. You can disable this hook by removing the `pre-commit` file from `.git/hooks` 
as well as the `pre-commit.sh` file from `git-hooks`. You should not commit this change.
