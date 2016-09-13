/*
 * Copyright (c) 2013-2016 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the
 * Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.  See the Apache License Version 2.0 for the specific
 * language governing permissions and limitations there under.
 */
import sbt._
import Keys._
import org.scalafmt.sbt.ScalaFmtPlugin.autoImport._

object BuildSettings {

  // Basic settings for our app
  lazy val basicSettings = Seq[Setting[_]](
    organization := "com.snowplowanalytics",
    version := "0.8.0",
    description := "Scala Stream Collector for Snowplow raw events",
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
    maxErrors := 5,
    // http://www.scala-sbt.org/0.13.0/docs/Detailed-Topics/Forking.html
    fork in run := true,
    resolvers ++= Dependencies.resolutionRepos
  )

  // Makes our SBT app settings available from within the app
  lazy val scalifySettings = Seq(
    sourceGenerators in Compile <+=
      (sourceManaged in Compile, version, name, organization) map {
        (d, v, n, o) =>
          val file = d / "settings.scala"
          IO.write(
            file,
            s"""package com.snowplowanalytics.snowplow.collectors.scalastream.generated
      |object Settings {
      |  val organization = "$o"
      |  val version = "$v"
      |  val name = "$n"
      |  val shortName = "ssc"
      |}
      |""".stripMargin)
          Seq(file)
      })

  import sbtassembly.AssemblyPlugin._
  import sbtassembly.AssemblyKeys._

  // sbt-assembly settings for building an executable
  lazy val sbtAssemblySettings = baseAssemblySettings ++ Seq(
      // Executable jarfile
      assemblyOption in assembly := (assemblyOption in assembly).value
        .copy(prependShellScript = Some(defaultShellScript)),
      // Name it as an executable
      assemblyJarName in assembly := s"${name.value}-${version.value}"
    )

  lazy val buildSettings = basicSettings ++ reformatOnCompileSettings ++ scalifySettings ++ sbtAssemblySettings
}
