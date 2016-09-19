/*
 * Copyright (c) 2013-2016 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

// SBT
import sbt._
import Keys._
import org.scalafmt.sbt.ScalaFmtPlugin.autoImport._

object BuildSettings {

  // Basic settings for our app
  lazy val basicSettings = Seq[Setting[_]](
    organization := "com.snowplowanalytics",
    description := "The Snowplow Enrichment process, implemented as an Amazon Kinesis app",
    scalaVersion := "2.11.8",
    scalacOptions := Seq("-encoding",
                         "UTF-8",
                         "-target:jvm-1.8",
                         "-unchecked",
                         "-deprecation",
                         "-feature",
                         "-language:higherKinds",
                         "-language:existentials",
                         "-language:postfixOps",
                         "-Xexperimental",
                         "-Xlint",
                         // "-Xfatal-warnings",
                         "-Xfuture",
                         "-Ybackend:GenBCode",
                         "-Ydelambdafy:method",
                         "-Yno-adapted-args",
                         "-Yopt-warnings",
                         "-Yopt:l:classpath",
                         "-Yopt:unreachable-code" /*,
                         "-Ywarn-dead-code",
                         "-Ywarn-infer-any",
                         "-Ywarn-numeric-widen",
                         "-Ywarn-unused",
                         "-Ywarn-unused-import",
                         "-Ywarn-value-discard"*/ ),
    scalacOptions in Test := Seq("-Yrangepos"),
    resolvers ++= Dependencies.resolutionRepos
  )

  // Makes our SBT app settings available from within the app
  lazy val scalifySettings = Seq(
    sourceGenerators in Compile <+= (sourceManaged in Compile,
                                     version,
                                     name,
                                     organization) map { (d, v, n, o) =>
      val file = d / "settings.scala"
      IO.write(
        file,
        """package com.snowplowanalytics.snowplow.enrich.kinesis.generated
      |object Settings {
      |  val organization = "%s"
      |  val version = "%s"
      |  val name = "%s"
      |}
      |""".stripMargin.format(o, v, n))
      Seq(file)
    })

  import sbtassembly.AssemblyKeys._
  import sbtassembly.AssemblyPlugin._

  // sbt-assembly settings for building an executable
  lazy val sbtAssemblySettings = baseAssemblySettings ++ Seq(
      // Executable jarfile
      assemblyOption in assembly := (assemblyOption in assembly).value
        .copy(prependShellScript = Some(defaultShellScript)),
      // Name it as an executable
      assemblyJarName in assembly := name.value,
      excludedJars in assembly := (fullClasspath in assembly).value
        .filter(_.data.getName.contains("specs2")),
      test in assembly := {}
    )

  lazy val buildSettings = basicSettings ++ reformatOnCompileSettings ++ scalifySettings ++ sbtAssemblySettings
}
