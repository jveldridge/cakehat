# About Releasing #
Make sure to update the information in `manifest.mf` with the latest release info. This file lives in the root directory of the repository.

The format is:
```
cakehat-Version: Major.Minor (Development)
cakehat-Release-Commit-Number: r####
cakehat-Release-Date: MM/DD/YYYY
```

Example for a release:
```
cakehat-Version: 6.0
cakehat-Release-Commit-Number: r1100
cakehat-Release-Date: 9/16/2012
```

Example during development (in trunk branch):
```
cakehat-Version: 6.0 (Development)
cakehat-Release-Commit-Number: N/A
cakehat-Release-Date: N/A
```


To create a new branch:
```
svn copy https://cakehat.googlecode.com/svn/trunk https://cakehat.googlecode.com/svn/branches/[branch name] -m "Creates new release branch"
```

Where `[branch name]` has the format `release[Major.Minor]`. Example: `release6.0`

To update the symbolic link:
```
ln -nsf 6.0 latest
```
This will update the symbolic link `latest` to point to the release folder `6.0`.

# Release Types #
There are three type of releases for cakehat:
1. major (#.0.0)
2. minor (#1.#2.0, with #2 not 0)
3. minor-minor (#1.#2.#3, with #3 not 0)

## Major Releases ##
Major releases are **incompatible** with all previous versions of cakehat.  Any of the following may change for a major release:
  * the database structure
  * the .cakehat directory structure
  * valid configuration settings
  * run and lab checkoff scripts

Major releases are expected to occur only at the beginning of a semester.

A release branch should be created for all major release branches.

## Minor Releases ##
Minor releases add new features, enhancements, and non-critical bug fixes.

There could be several minor releases over the course of a semester, depending on the rate of development.

A release branch should be created for all minor release branches.

## Minor-Minor Releases ##
Minor-minor releases are used only for critical bug fixes.  They are not to add any new features.

For the time being, new release branches will not be created for minor-minor releases.  Instead, further critical fixes will be made in the corresponding minor release branch (which should be renamed).