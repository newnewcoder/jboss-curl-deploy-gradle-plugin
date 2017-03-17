package com.github.newnewcoder.jbosscurl.operator

import org.apache.http.HttpEntity
import org.apache.http.HttpHost
import org.apache.http.client.fluent.Executor
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder

abstract class Operator {
    File war
    JbossClient jc
    boolean debug

    Operator(File war, JbossClient jc, boolean debug = false) {
        this.war = war
        this.jc = jc
        this.debug = debug
    }

    def execute(Request request) {
        String response
        if (!debug) {
            Executor executor = Executor.newInstance().auth(new HttpHost(jc.host), jc.user, jc.password)
            response = executor.execute(request).returnContent().asString()
        } else {
            println "[Debug] Just print...\r\n>${request.toString()}\r\n${this.toString()}\r\n"
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

    @Override
    String toString(){
"""
    JBoss Connection Info:${jc.toString()}
    War: ${war.toString()}"""
    }
}

abstract class SimpleOperator extends Operator {
    def attr

    SimpleOperator(File war, JbossClient jc, boolean debug) {
        super(war, jc, debug)
        attr = new HashMap()
    }

    def process() {
        Request request = Request.Post(jc.managementUrl).bodyByteArray(groovy.json.JsonOutput.toJson(attr).getBytes('utf-8'), ContentType.APPLICATION_JSON)
        execute(request)
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
        HttpEntity entity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE).addBinaryBody(war.name, war).build()
        Request request = Request.Post(jc.uploadUrl).body(entity)
        def uploadedResp = execute(request)
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

    @Override
    String toString(){
"""
        Host: ${host}
        Port: ${port}
        User: ${user}
        Password: ${password}"""
    }
}