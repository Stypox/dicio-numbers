# Dummy dicio-numbers
This branch contains a dummy version of *dicio-numbers*, containing only a dummy `org.dicio.numbers.ParserFormatter`.

## Motivation
It was created for [*dicio-skill*](https://github.com/Stypox/dicio-skill/), since in order to be built *dicio-skill* requires the definition of `ParserFormatter`. But other than that it doesn't use any other classes from *dicio-numbers*, nor `ParserFormatter`'s defined methods/fields. Having a dummy dependency:
- reduces *dicio-skill*'s dependencies
- allows users to use *dicio-skill* without *dicio-numbers*
- and, **most importantly**, removes the burden of having to update the version of *dicio-numbers* both in *dicio-skill* and in *dicio-android* each time (see below on how this is done)

## Replacing the dummy version at build time
[*dicio-android*](https://github.com/Stypox/dicio-android/) replaces the dummy version contained in *dicio-skill* with a proper implementation of *dicio-numbers* with the process below.

If you want to use *dicio-skill* and also want the **actual implementation** of *dicio-numbers* to be used, intead of this dummy one, you should declare it as another dependency, separate from dicio-skill, by adding this line in your `build.gradle`'s dependencies, as usual (replace `VERSION` with the version or commit hash you want to use):
```gradle
implementation("com.github.Stypox:dicio-numbers:VERSION")
```

Then, in order to make sure Gradle actually chooses the version you specified, and not the dummy version included in *dicio-skill*, add the following lines in `build.gradle` (make sure `VERSION` is the same as above):
```gradle
configurations.all {
    resolutionStrategy {
        // make sure VERSION is the same as above (!)
        force("com.github.Stypox:dicio-numbers:VERSION")
    }
}
```

See [this Stack Overflow question](https://stackoverflow.com/q/28444016) for more information about this process.

## Using this dummy version
You would need to use this dummy version **only if you are building something similar to *dicio-skill***. Just use the following line to the dependencies, like it's done in *dicio-skill*:
```gradle
implementation("com.github.Stypox:dicio-numbers:dummy-2")
```

Replace `dummy-2` with the git tag of the dummy version you want to use, see below. 

## Dummy versions available
Dummy versions are marked by git tags, and new versions are created whenever an API change in the real `dicio-numbers` requires the dummy alternative to introduce the same API change.
