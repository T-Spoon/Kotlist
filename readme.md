# Kotlist

Kotlist is a simple ListView implementation in Kotlin (intended for learning purposes only)! It was created to explore the viability of using Kotlin for an Android project.  


## Advantages of Using Kotlin
I won't go into that here - there are plenty of reason why Kotlin is awesome! If you're interested in using Kotlin then you should take a look at [this write-up](https://docs.google.com/document/d/1ReS3ep-hjxWA8kZi0YqDbEhCqTt29hG8P44aA9W0DM8/edit#) by Jake Wharton.

## Limitations
  - **Debugger is Buggy** - Unfortunately the debugger in Android Studio goes a bit haywire when you try to use the Expression Evaluator, making it unreliable.  Variable inspection seems to work fine though.
  - **No Support For Secondary Constructors** - This essentially means that creating custom views is out of the question (since the constructor is invoked by the system, we can't use a Factory).  You can use a single constructor e.g. the 2 arg one, like I've done in this demo, but that means you can't use styles.  Until support for secondary constructors is here, custom views from XML are essentially a no-go! 

  
The are of course other limitations (e.g. lack of annotation processing, for now), but these were the ones that hindered me the most when creating this small test.

