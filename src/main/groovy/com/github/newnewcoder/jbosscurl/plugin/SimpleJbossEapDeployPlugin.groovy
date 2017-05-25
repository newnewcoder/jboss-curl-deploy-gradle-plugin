package com.github.newnewcoder.jbosscurl.plugin

import com.github.newnewcoder.jbosscurl.operator.*
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction


class SimpleJbossEapDeployPlugin implements Plugin<Project> {

    static final String EXT_NAME = "jboss"

    @Override
    void apply(Project project) {
        project.extensions.create(EXT_NAME, SimpleJbossEapDeployPluginExtension)

        [["add", "Add war to jboss."],
         ["remove", "Remove war from jboss."],
         ["deploy", "Add war to jboss and enable it."],
         ["disable", "Disable war from jboss."],
         ["enable", "Enable war from jboss."],
         ["status", "Print jboss server status."],
         ["show", "Show deployments."]].each { task, desc ->
            project.task(task, type: SimpleJbossDeployTask) {
                description = desc
            }
        }

        project.afterEvaluate {
            def extension = project.extensions.findByName(EXT_NAME)
            project.tasks.withType(SimpleJbossDeployTask).all { task ->
                JbossClient jc = new JbossClient(host: extension.host, port: extension.port, user: extension.user, password: extension.password)
                File war = new File(extension.warPath, extension.warName)
                boolean debug = extension.debug
                switch (task.name) {
                    case 'add':
                        task.operator = new AddOperator(war, jc, debug)
                        break
                    case 'remove':
                        task.operator = new RemoveOperator(war, jc, debug)
                        break
                    case 'deploy':
                        task.operator = new DeployOperator(war, jc, debug)
                        break
                    case 'disable':
                        task.operator = new DisableOperator(war, jc, debug)
                        break
                    case 'enable':
                        task.operator = new EnableOperator(war, jc, debug)
                        break
                    case 'status':
                        task.operator = new StateOperator(war, jc, debug)
                        break
                    case 'show':
                        task.operator = new ShowOperator(war, jc, debug)
                        break
                    default:
                        break //TODO
                }
            }
        }
    }
}

class SimpleJbossDeployTask extends DefaultTask {
    Operator operator

    @TaskAction
    void run() {
        operator.run()
    }
}

class SimpleJbossEapDeployPluginExtension {
    String user, password, host, port, warName, warPath
    boolean debug
}