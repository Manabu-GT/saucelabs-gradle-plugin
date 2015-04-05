package com.ms_square.plugin.gradle

import org.gradle.api.Project

class SauceLabsExtension {

    private String serverEndPoint = "https://saucelabs.com"

    private String userName
    private String accessKey

    private boolean overwrite = true

    private String testCommand

    SauceLabsExtension(Project project) {

    }

    String getServerEndPoint() {
        return serverEndPoint
    }

    void setServerEndPoint(String serverEndPoint) {
        this.serverEndPoint = serverEndPoint
    }

    void setUserName(String username) {
        this.userName = username
    }

    String getUserName() {
        return (userName != null) ? userName : System.getenv('SAUCE_USERNAME')
    }

    void setAccessKey(String accessKey) {
        this.accessKey = accessKey
    }

    String getAccessKey() {
        return (accessKey != null) ? accessKey: System.getenv('SAUCE_ACCESS_KEY')
    }

    boolean getOverwrite() {
        return overwrite
    }

    void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite
    }

    void setTestCommand(String testCommand) {
        this.testCommand = testCommand
    }

    String getTestCommand() {
        return testCommand
    }
}