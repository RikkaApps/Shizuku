package moe.shizuku.manager.viewmodel

open class Resource<out T>(val status: Status, val data: T?, val error: Throwable) {

    companion object {
        private val noError = Throwable("No error")

        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, noError)
        }

        fun <T> error(error: Throwable, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data, error)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, noError)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Resource<*>

        if (status != other.status) return false
        if (data != other.data) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + error.hashCode()
        return result
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

class SourcedResource<out T, out S>(status: Status, data: T?, error: Throwable, val source: S?) : Resource<T>(status, data, error) {

    companion object {
        private val noError = Throwable("No error")

        fun <T, S> success(data: T?, source: S?): SourcedResource<T, S> {
            return SourcedResource(Status.SUCCESS, data, noError, source)
        }

        fun <T, S> error(error: Throwable, data: T?, source: S?): SourcedResource<T, S> {
            return SourcedResource(Status.ERROR, data, error, source)
        }

        fun <T, S> loading(data: T?, source: S?): SourcedResource<T, S> {
            return SourcedResource(Status.LOADING, data, noError, source)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SourcedResource<*, *>

        if (source != other.source) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (source?.hashCode() ?: 0)
        return result
    }
}