package dev.ragnarok.fenrir.util.coroutines

import dev.ragnarok.fenrir.Constants
import kotlinx.coroutines.Job
import java.util.concurrent.CancellationException

class CompositeJob {
    private val jobList = HashSet<Job>(1)
    private var isCanceled: Boolean = false

    fun add(job: Job): Boolean {
        var ret = false
        if (!isCanceled) {
            synchronized(this) {
                if (!isCanceled) {
                    jobList.removeIf {
                        it.isCancelled || it.isCompleted
                    }
                    ret = jobList.add(job)
                }
            }
        }
        return ret
    }

    operator fun plusAssign(job: Job) {
        add(job)
    }

    fun remove(job: Job, cancel: Boolean = true): Boolean {
        var ret = false
        synchronized(this) {
            ret = jobList.remove(job)
            if (cancel && !isCanceled) {
                job.cancel()
            }
        }
        return ret
    }

    fun clear() {
        if (!isCanceled) {
            synchronized(this) {
                if (!isCanceled) {
                    jobList.removeIf {
                        if (it.isActive) {
                            it.cancel(if (Constants.IS_DEBUG) CancellationException("Clearing jobs!") else null)
                        }
                        true
                    }
                }
            }
        }
    }

    fun cancel() {
        if (!isCanceled) {
            synchronized(this) {
                if (!isCanceled) {
                    jobList.removeIf {
                        if (it.isActive) {
                            it.cancel(if (Constants.IS_DEBUG) CancellationException("Presenter destroyed, canceling job!") else null)
                        }
                        true
                    }
                    isCanceled = true
                }
            }
        }
    }

    fun size(): Int {
        var ret = 0
        if (!isCanceled) {
            synchronized(this) {
                if (!isCanceled) {
                    jobList.removeIf {
                        !it.isActive
                    }
                    ret = jobList.size
                }
            }
        }
        return ret
    }
}