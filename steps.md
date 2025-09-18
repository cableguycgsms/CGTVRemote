## CREATE NEW MODULE

- IN ANDROID STUDIO

	- Go to `FILE` in the top nav

	- Click on `New` -> `New Module`

	- Give the module a name and click `Create`

  

## ADD CONFIG FILEs (in Root Project not inside Module)

- Create `jetpack.yml` file with this contents
   

 `` #configuration file for building snapshots and releases with jitpack.io
        
        jdk:
        
        - openjdk17
        
        before_install:
        
        - ./scripts/prepareJitpackEnvironment.sh
        
        install:
        
        - FILE="-Dfile=remotesetu-release.aar"
        
        - mvn install:install-file $FILE -DgroupId=com.github.cableguycgsms -DartifactId=RemoteSetu -Dversion=1.0 -Dpackaging=aar -DgeneratePom=true```

- Create `prom.xml` file with this contents and replace the placeholders with appropriate values

``<?xml version="1.0" encoding="UTF-8"?>  
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"  
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">  
 <modelVersion>4.0.0</modelVersion>  
 <groupId>com.github.GIT-USERNAME</groupId>  
 <artifactId>GIT-NAME</artifactId>  
 <version>BUILD-VERSION</version>  
 <packaging>pom</packaging>  
 <description>The RemoteSetu project is a mobile application that allows users to control their Smart TVs and OTT platforms (such as Netflix, Prime Video, YouTube, etc.) directly from their smartphone. It eliminates the dependency on physical remotes by providing a universal, feature-rich virtual remote interface with intuitive navigation</description>  
</project>``

## ADD BUILD CHANGES TO `build.gradle` in module

USE THE BELOW CODE TO REFERENCE AND UPDATE AS SUCH

    buildscript {
        ext.kotlin_version = '1.7.20'
        repositories {
            google()
            mavenCentral()
            mavenLocal()
        }
    
        dependencies {
            classpath 'com.android.tools.build:gradle:7.1.3'
            classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        }
    }
    
    plugins {
        id 'com.android.library'
        id 'maven-publish'
    }
    
    android {
        namespace 'com.sarj33t.android_demo_lib'
        compileSdk 33
    
        defaultConfig {
            minSdk 24
            targetSdk 33
    
            testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
            consumerProguardFiles "consumer-rules.pro"
        }
    
        buildTypes {
            release {
                minifyEnabled false
                proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
                consumerProguardFiles 'consumer-rules.pro'
            }
        }
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
    }
    
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)       /// << --- ADD This
        }
    }
    
    
    java {
        sourceCompatibility = JavaVersion.VERSION_17          ////  << --- ADD This
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    dependencies {
    
        implementation 'androidx.appcompat:appcompat:1.4.1'
        implementation 'com.google.android.material:material:1.5.0'
        testImplementation 'junit:junit:4.13.2'
        androidTestImplementation 'androidx.test.ext:junit:1.1.5'
        androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    }
    
    publishing {
        publications {
            maven(MavenPublication) {
                groupId = 'com.github.imchampagnepapi'
                artifactId = 'android-demo-lib'
                version = "1.0"
                pom {
                    description = 'First Android Library'
                }
            }
        }
        repositories {               //// << --- ADD This
            mavenLocal()
        }
    }

## Create a `.aar` build file 

- Open command prompt in the directory 
- Run `./gradlew clean assembleRelease`
- Rename the `.aar` file as the same in `jetpack.yml` file and then paste it to the root of the project not the module
- Then push the changes to the git repo
- Then create new tag in git using `git tag <tag-name>`
- Then push the tag to github using `git push origin <tag-name>`

## Create a new release on GITHUB
- Go to the github page and open the project
- On the right side is `new release ` button click on it
- Select the tag name from the dropdown
- Write down the changes in the description
- Then add the `.aar` file to the release and click create

## Create a release in jitpack.io
- Open `jitpack.io` and login using your github account
- Then enter `GITUSERNAME/RepoName` in the search bar and then search
- Click on `release` tab and then if the log show green you are ready to go
