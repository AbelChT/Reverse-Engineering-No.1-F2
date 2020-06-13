# Companion Android Application
## Description
This application is intended to replace the official application for communication with this Smartwatch.

## Considerations
Before compiling the project, you must create a file in the package config named SecretKeyList.kt. This file must contain a variable called secretOpenWeatherApiKey and must have as value an official Openweather API key.  

Example of the file:
```kotlin
package com.abelcht.n01f2smwc.config

var secretOpenWeatherApiKey:String = "00000000000000000000000000000000"
```

## Screen
![Screenshot](art/screenshot.png = 250x)

