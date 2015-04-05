package com.ms_square.plugin.gradle
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FilenameUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.FileEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class SauceLabsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // create an extension where the settings reside
        def extension = project.extensions.create("sauceLabsConfig", SauceLabsExtension, project)

        project.configure(project) {
            if (it.hasProperty("android")) {

                tasks.whenTaskAdded { task ->

                    project.("android").applicationVariants.all { variant ->

                        // locate packageRelease and packageDebug tasks
                        def expectingTask = "package${variant.name.capitalize()}".toString()
                        if (expectingTask.equals(task.name)) {

                            String variantName = variant.name
                            String versionName = variant.versionName
                            String versionCode = variant.versionCode

                            // create new task with name such as sauceLabsRelease and sauceLabsDebug
                            def newTaskName = "sauceLabs${variantName.capitalize()}"

                            project.task(newTaskName) << {

                                String userName = extension.getUserName()
                                String accessKey = extension.getAccessKey()

                                // use outputFile from packageApp task
                                String apkFilePath = task.outputFile.toString()
                                String apkFileName = FilenameUtils.getName(apkFilePath);
                                project.logger.info("Uploading ${apkFilePath}")

                                uploadApk(project, extension, apkFilePath)

                                println ""
                                println "Successfully uploaded to SauceLabs, build is available at: sauce-storage:${apkFileName}"
                                println ""

                                String command = extension.getTestCommand();
                                if (command) {
                                    println "Running test script:" + command
                                    // export commit, apk filename, version name, and version code as environment variables
                                    Map<String, String> envToAdd = new HashMap<>();
                                    envToAdd.put("SAUCE_COMMIT", getRevision())
                                    envToAdd.put("SAUCE_APK_FILE", apkFileName)
                                    envToAdd.put("SAUCE_APK_VERSION_NAME", versionName)
                                    envToAdd.put("SAUCE_APK_VERSION_CODE", versionCode)
                                    int exitValue = executeOnShell(command, envToAdd)
                                    println "Test script exited with " + exitValue
                                }
                                println "Done!"

                            }

                            project.(newTaskName.toString()).dependsOn(expectingTask)
                            project.(newTaskName.toString()).group = "SauceLabs"
                            project.(newTaskName.toString()).description = "Uploads the ${variantName.capitalize()} build to SauceLabs and run the test script"
                        }
                    }
                }
            }
        }
    }

    private static void uploadApk(Project project, SauceLabsExtension extension, String apkFilePath) {
        String serverEndpoint = extension.getServerEndPoint()
        String url = "${serverEndpoint}/rest/v1/storage/${extension.getUserName()}/${FilenameUtils.getName(apkFilePath)}?overwrite=${extension.getOverwrite()}"
        String authHeader = "Basic " + new String(Base64.encodeBase64("${extension.getUserName()}:${extension.getAccessKey()}".getBytes()))

        FileEntity fileEntity = new FileEntity(new File(apkFilePath), "application/octet-stream")

        post(url, authHeader, fileEntity)
    }

    private static void post(String url, String authHeader, HttpEntity entity) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url)
        post.addHeader("User-Agent", "SauceLabs Gradle Plugin")
        post.addHeader("Authorization", authHeader)
        post.setEntity(entity)
        HttpResponse response = httpClient.execute(post)

        int statusCode = response.getStatusLine().getStatusCode()
        if (statusCode != 200) {
            throw new GradleException("Upload failed with statusCode: " + statusCode)
        }
    }

    // Gets the revision number that the build is testing from Git
    private static String getRevision() {
        try {
            def process = 'git rev-parse HEAD'.execute()
            def outputStream = new StringBuffer();
            process.consumeProcessOutputStream(outputStream)
            process.waitForOrKill(3000)
            return outputStream.toString().trim()
        } catch(Exception e) {
            return ""
        }
    }

    private static int executeOnShell(String command, Map<String, String> envToAdd) {
        return executeOnShell(command, envToAdd, new File(System.properties.'user.dir'))
    }

    private static int executeOnShell(String command, Map<String, String> envToAdd, File workingDir) {
        def pb = new ProcessBuilder(addShellPrefix(command))
                .directory(workingDir)
                .redirectErrorStream(true)
        if (envToAdd) {
            for (String key : envToAdd.keySet()) {
                // might throw exceptions in some environments
                pb.environment().put(key, envToAdd.get(key))
            }
        }
        def process = pb.start()
        process.inputStream.eachLine {
            println it
        }
        process.waitFor();
        return process.exitValue()
    }

    private static String[] addShellPrefix(String command) {
        def commandArray = new String[3]
        commandArray[0] = "sh"
        commandArray[1] = "-c"
        commandArray[2] = command
        return commandArray
    }
}