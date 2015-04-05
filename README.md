SauceLabs Gradle Plugin for Android
------------------------------------

Compatibility
--------------
Currently verified to work with Gradle 2.2.1, Android Gradle Plugin 1.1.3, and Android Studio 1.1.0

Installation
-------------
A SauceLabs Gradle Plugin is pushed to Maven Central,
so installation consists of adding the following to your ***build.gradle*** file:

 1. Add plugin dependency:

        classpath 'com.ms-square:saucelabs-gradle-plugin:1.0.0'

 2. Apply plugin:

        apply plugin: 'saucelabs'

Complete Example
----------------
For convenience, here is a snippet of a complete ***build.gradle*** file, including the additions above.

    apply plugin: 'com.android.application'
    apply plugin: 'saucelabs'

    buildscript {
        repositories {
            jcenter()
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:1.1.3'

            classpath 'com.ms-square:saucelabs-gradle-plugin:1.0.0'
        }
    }

Usage
------

With the plugin installed, a set of new tasks, prefixed "*sauceLabs*" will be added, one for each build type.

For example: to upload a debug build and test on SauceLabs, run the following from terminal:

    ./gradlew sauceLabsDebug

Parameters
---------------

    android {
        sauceLabsConfig {
            userName "foo" (optional, defaults to SAUCE_USERNAME environment variable)
            accessKey "hoge" (optional, defaults to SAUCE_ACCESS_KEY environment variable)
            overwrite true (optional, defaults to true)
            testCommand "py.test sample/appium/android_sauce_labs.py" (required, your test script)
        }
    }

Inserted Environment variables
--------------------------------
The plugin will inject the following environment variables and make them available to your test script.

SAUCE_COMMIT - Git commit that the current build is testing.
SAUCE_APK_FILE - APK file name uploaded to the sauce labs.
SAUCE_APK_VERSION_NAME - Android application version name in the manifest.
SAUCE_APK_VERSION_CODE - Android application version code in the manifest.

For its sample usage, please look at the test code of the [provided sample][1].

License
---------------------

```
 Copyright 2015 Manabu Shimobe

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
```

[1]: https://github.com/Manabu-GT/saucelabs-gradle-plugin/tree/master/sample/appium