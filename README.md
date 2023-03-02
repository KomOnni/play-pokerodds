# Play pokerodds for Scala

To use the app, you will need the correct version of Java and sbt. The template requires:

* Java Software Developer's Kit (SE) 1.8 or higher
* sbt 1.3.4 or higher. Note: if you downloaded this project as a zip file from <https://developer.lightbend.com>, the file includes an sbt distribution for your convenience.

To check your Java version, enter the following in a command window:

```bash
java -version
```

To check your sbt version, enter the following in a command window:

```bash
sbt sbtVersion
```

If you do not have the required versions, follow these links to obtain them:

* [Java SE](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [sbt](http://www.scala-sbt.org/download.html)

## Build and run the project

This example Play project was created from a seed template. It includes all Play components and an Akka HTTP server. The project is also configured with filters for Cross-Site Request Forgery (CSRF) protection and security headers.

To build and run the project:

1. Use a command window to change into the example project directory, for example: `cd play-pokerodds`

2. Build the project. Enter: `sbt run`. The project builds and starts the embedded HTTP server. Since this downloads libraries and dependencies, the amount of time required depends partly on your connection's speed.

3. After the message `Server started, ...` displays, enter the following URL in a browser: <http://localhost:9000>

## To get the Odds

To get odds, one must give their own cards first, format being a string. In instance, if you have a spade king and a diamond seven, and the table has spade ace, club six and heart ten, the input string would be

SKD7SAC6HT (url localhost:9000/getOdds/SKD7SAC6HT)

One is able to do this with only their hand cards (2) or with any amount of additional cards on the table (2 hand cards + 3-5 cards flipped on the table)

This application can calculate all odds. Note that the odds are against a single player, and are not absolutely perfect, but close enough. Also the amount of computing needed for 2 hand cards and 3 table cards means, that it takes around half a minute to calculate.
