ALE Rainbow Android SDK
==

What is it?
==

The ALE Rainbow Android SDK is a platform which allows you to use Rainbow services as you want. For example, you can use this sdk to develop an application which uses webRTC technology. So, it is easy to use this sdk in your own application.

Usage
==

To compile the ALE Rainbow Android SDK in your Android project, add the following lines in **app\build.gradle**:

	allprojects {
	    repositories {
	        (...)
	        maven { url "https://jitpack.io" }
	    }
	}

	(...)

	dependencies {
		(...)
		compile 'com.github.Rainbow-CPaaS:Rainbow-Android-SDK:1.28.0'
	}

Then click on "Sync now". Your Android project is now all set to use the ALE Rainbow Android SDK.