package com.github.newnewcoder.jbosscurl.operator

abstract class Operator {
    File war
    JbossClient jc
    boolean debug

    Operator(File war, JbossClient jc, boolean debug = false) {
        this.war = war
        this.jc = jc
        this.debug = debug
    }

    def execute(String command) {
        String response
        if (!debug) {
            println "[Info] Process command..."
            def proc = command.execute()
            proc.waitFor()
            println "[Info] Exit value: ${proc.exitValue()}"
            response = proc.text
        } else {
            println "[Debug] Just print...\r\n>${command}"
            response = "{\"outcome\" : \"success\", \"result\" : { \"BYTES_VALUE\" : \"This is test value\" }}"
        }
        println "[Info] Response: \r\n${response}"
        def jsonResponse = response.readLines().findAll { it.startsWith("{outcome") || it.startsWith("{\"outcome") }
        new groovy.json.JsonSlurper().parseText(jsonResponse)
    }

    void run() {
        def resp = process()
        if ("success" != resp.outcome) {
            throw new RuntimeException(resp.toString())
        }
    }

    abstract process()
}

abstract class SimpleOperator extends Operator {
    def attr

    SimpleOperator(File war, JbossClient jc, boolean debug) {
        super(war, jc, debug)
        attr = new HashMap()
    }

    def process() {
        String command = "curl --digest -u ${jc.user}:${jc.password} -L -D - ${jc.managementUrl} --header Content-Type:application/json -d ${groovy.json.JsonOutput.toJson(attr)}"
        execute(command)
    }
}

class StateOperator extends SimpleOperator {
    StateOperator(File war, JbossClient jc, boolean debug) {
        super(war, jc, debug)
        attr << [operation: "read-attribute", name: "server-state"]
    }
}

abstract class SingleWarOperator extends SimpleOperator {
    SingleWarOperator(File war, JbossClient jc, boolean debug) {
        super(war, jc, debug)
        attr << [address: [deployment: war.name]]
    }
}

class AddOperator extends SingleWarOperator {
    boolean enable = false

    AddOperator(File war, JbossClient jc, boolean debug) {
        super(war, jc, debug)
    }

    def process() {
        def command = "curl --digest -u ${jc.user}:${jc.password} --form file=@${war.getAbsolutePath()} ${jc.uploadUrl}"
        def uploadedResp = execute(command)
        def uploadedKey = uploadedResp.result
        attr << [content: [[hash: uploadedKey]], operation: "add", enabled: "${enable}"]
        super.process()
    }
}

class DeployOperator extends AddOperator {
    DeployOperator(File war, JbossClient jc, boolean debug) {
        super(war, jc, debug)
        enable = true
    }
}

class RemoveOperator extends SingleWarOperator {
    RemoveOperator(File war, JbossClient jc, boolean debug) {
        super(war, jc, debug)
        attr << [operation: "remove"]
    }
}

class EnableOperator extends SingleWarOperator {
    EnableOperator(File war, JbossClient jc, boolean debug) {
        super(war, jc, debug)
        attr << [operation: "deploy"]
    }
}

class DisableOperator extends SingleWarOperator {
    DisableOperator(File war, JbossClient jc, boolean debug) {
        super(war, jc, debug)
        attr << [operation: "undeploy"]
    }
}

class JbossClient {
    String host
    String port
    String user
    String password

    String getUploadUrl() {
        this.managementUrl + "/add-content"
    }

    String getManagementUrl() {
        "http://${host}:${port}/management"
    }

}