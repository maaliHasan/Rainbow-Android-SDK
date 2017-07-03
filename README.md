ALE Rainbow Android SDK
==

What is it?
==

The ALE Rainbow Android SDK is a platform which allows you to use Rainbow services as you want. For example, you can use this sdk to develop an application which uses webRTC technology. So, it is easy to use this sdk in your own application.

Install
==

Create a new android project using your favourite IDE.

When the projet is created, add these lines in your **app\build.gradle**:

	allprojects {
	    repositories {
	        (...)
	        maven { url "https://jitpack.io" }
	    }
	}

	(...)

	dependencies {
		(...)
		compile 'com.github.Rainbow-CPaaS:Rainbow-Android-SDK:1.27.0'
	}

After the gradle build, the SDK will appear in your dependencies.






