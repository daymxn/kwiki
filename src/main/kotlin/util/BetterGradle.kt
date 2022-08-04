package util

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

fun <T: Task> Task.waitUntil(task: TaskProvider<T>) {
    mustRunAfter(task)
    dependsOn(task)
}

fun <T: Task> Task.runInOrder(vararg tasks: TaskProvider<out T>) {
    tasks.toList().windowed(2, 1, true) {
        val prevTask = it[0]
        val currentTask = it.getOrNull(1)

        if(currentTask != null) {
            currentTask.get().waitUntil(prevTask)
        } else {
            waitUntil(prevTask)
        }
    }
}
