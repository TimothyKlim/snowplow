/*
 * Copyright (c) 2012-2015 Snowplow Analytics Ltd. All rights reserved.
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
import bintray.BintrayPlugin._

object BuildSettings {

  // Basic settings for our app
  lazy val basicSettings = Seq[Setting[_]](
    organization := "com.snowplowanalytics",
    version := "0.25.0-kt",
    description := "Common functionality for enriching raw Snowplow events",
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

  // Makes our SBT app settings available from within the ETL
  lazy val scalifySettings = Seq(
    sourceGenerators in Compile <+= (sourceManaged in Compile,
                                     version,
                                     name,
                                     organization,
                                     scalaVersion) map { (d, v, n, o, sv) =>
      val file = d / "settings.scala"
      IO.write(file,
               """package com.snowplowanalytics.snowplow.enrich.common.generated
                 |object ProjectSettings {
                 |  val version = "%s"
                 |  val name = "%s"
                 |  val organization = "%s"
                 |  val scalaVersion = "%s"
                 |}
                 |""".stripMargin.format(v, n, o, sv))
      Seq(file)
    })

  // For MaxMind support in the test suite
  import Dependencies._

  // Publish settings
  // TODO: update with ivy credentials etc when we start using Nexus
  // lazy val publishSettings = Seq[Setting[_]](
  //   crossPaths := false,
  //   publishTo <<= version { version =>
  //     val basePath = "target/repo/%s".format {
  //       if (version.trim.endsWith("SNAPSHOT")) "snapshots/" else "releases/"
  //     }
  //     Some(
  //       Resolver
  //         .file("Local Maven repository", file(basePath)) transactional ())
  //   }
  // )

  import bintray.BintrayPlugin._
  import bintray.BintrayKeys._
  // Bintray publishing settings
  lazy val publishSettings = bintraySettings ++ Seq[Setting[_]](
      licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
      bintrayOrganization := Some("fcomb"),
      bintrayRepository := "maven"
    )

  lazy val buildSettings = basicSettings ++ reformatOnCompileSettings ++ scalifySettings ++ publishSettings
}
