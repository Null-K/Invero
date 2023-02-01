import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.authentication.http.BasicAuthentication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven

fun PublishingExtension.createPublish(project: Project) {
    repositories {
        maven(repoTabooProject) {
            credentials {
                username = project.findProperty("taboolibUserName").toString()
                password = project.findProperty("taboolibPassword").toString()
            }
            authentication { create<BasicAuthentication>("basic") }
        }
        mavenLocal()
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = projectName
            groupId = projectGroup
            version = projectVersion
            artifact(project.tasks["kotlinSourcesJar"])
            artifact(project.tasks["shadowJar"]) { classifier = null }
            println("> Apply \"$groupId:$artifactId:$version\"")
        }
    }

}