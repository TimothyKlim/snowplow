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
import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._

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

  lazy val javaRunOptions = Seq(
    "-server",
    "-XX:+UseAES",
    "-XX:+UseAESIntrinsics",
    "-Xms2g",
    "-Xmx2g",
    "-Xss6m",
    "-XX:NewSize=512m",
    "-XX:+UseNUMA",
    "-XX:+AlwaysPreTouch",
    "-XX:-UseBiasedLocking",
    "-XX:+TieredCompilation",
    "-XX:+UseG1GC",
    "-XX:MaxGCPauseMillis=200",
    "-XX:ParallelGCThreads=20",
    "-XX:ConcGCThreads=5",
    "-XX:InitiatingHeapOccupancyPercent=70"
  )

  lazy val packageSettings = SbtNativePackager.packageArchetype.java_application ++ Seq(
      publishArtifact in (Compile, packageDoc) := false,
      publishArtifact in packageDoc := false,
      sources in (Compile, doc) := Seq.empty,
      executableScriptName := "start",
      javaOptions in Universal ++= javaRunOptions.map(o => s"-J$o"),
      packageName in Universal := "dist",
      scriptClasspath ~= (cp => "../config" +: cp)
    )

  lazy val buildSettings = basicSettings ++ reformatOnCompileSettings ++ scalifySettings ++ packageSettings
}
