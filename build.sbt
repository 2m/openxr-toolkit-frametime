ThisBuild / scalaVersion := "3.6.3"
ThisBuild / scalafmtOnCompile := true

ThisBuild / organization := "lt.dvim.oxrt.frametimes"
ThisBuild / organizationName := "github.com/2m/openxr-toolkit-frametimes/contributors"
ThisBuild / startYear := Some(2025)
ThisBuild / licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

ThisBuild / dynverSeparator := "-"

def linkerOutputDirectory(v: Attributed[org.scalajs.linker.interface.Report], t: File): Unit = {
  val output = v.get(scalaJSLinkerOutputDirectory.key).getOrElse {
    throw new MessageOnlyException(
      "Linking report was not attributed with output directory. " +
        "Please report this as a Scala.js bug."
    )
  }
  IO.write(t / "linker-output.txt", output.getAbsolutePath.toString)
  ()
}

val publicDev = taskKey[Unit]("output directory for `npm run dev`")
val publicProd = taskKey[Unit]("output directory for `npm run build`")

lazy val frontend = project
  .in(file("modules/frontend"))
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js"  %%% "scalajs-dom"                 % "2.8.0",
      "org.scala-js"  %%% "scala-js-macrotask-executor" % "1.1.1",
      "com.raquo"     %%% "laminar"                     % "17.2.0",
      "org.scalameta" %%% "munit"                       % "1.0.4" % Test
    ),
    // Tell Scala.js that this is an application with a main method
    scalaJSUseMainModuleInitializer := true,

    /* Configure Scala.js to emit modules in the optimal way to
     * connect to Vite's incremental reload.
     * - emit ECMAScript modules
     * - emit as many small modules as possible for classes in the "oxrt.frametimes" package
     * - emit as few (large) modules as possible for all other classes
     *   (in particular, for the standard library)
     */
    scalaJSLinkerConfig ~= {
      import org.scalajs.linker.interface.ModuleSplitStyle
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("oxrt.frametimes"))
        )
    },
    publicDev := linkerOutputDirectory((Compile / fastLinkJS).value, target.value),
    publicProd := linkerOutputDirectory((Compile / fullLinkJS).value, target.value),

    // scalably typed
    externalNpm := baseDirectory.value // Tell ScalablyTyped that we manage `npm install` ourselves
  )
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(
    (if (!sys.env.get("SCALA_STEWARD").isDefined) Seq(ScalablyTypedConverterExternalNpmPlugin) else Seq.empty)*
  )
