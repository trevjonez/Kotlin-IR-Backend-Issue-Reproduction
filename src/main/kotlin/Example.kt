import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlin.contracts.contract

sealed class NetRequestStatus<out T : Any> {

    abstract val value: T?

    data class Pending<out T : Any>(
        override val value: T? = null,
    ) : NetRequestStatus<T>()

    data class Error<out T : Any>(
        val error: Throwable,
        override val value: T? = null,
    ) : NetRequestStatus<T>()

    data class Success<T : Any>(
        override val value: T,
    ) : NetRequestStatus<T>()

    companion object {

        fun <T : Any> pending(
            value: T? = null,
        ): NetRequestStatus<T> = Pending(value)

        fun <T : Any> error(
            error: Throwable,
            value: T? = null,
        ): NetRequestStatus<T> = Error(error, value)

        fun <T : Any> success(
            value: T,
        ): NetRequestStatus<T> = Success(value)
    }
}

fun <T : Any> NetRequestStatus<T>.isPending(): Boolean {
    contract { returns(true) implies (this@isPending is NetRequestStatus.Pending) }
    return (this is NetRequestStatus.Pending)
}

fun <T : Any> NetRequestStatus<T>.isSuccess(): Boolean {
    contract { returns(true) implies (this@isSuccess is NetRequestStatus.Success) }
    return (this is NetRequestStatus.Success)
}

fun <T : Any> NetRequestStatus<T>.isError(): Boolean {
    contract { returns(true) implies (this@isError is NetRequestStatus.Error) }
    return (this is NetRequestStatus.Error)
}

/**
 * The usage of the isError within this function causes the issue if this function is in the same file as the isError definition.
 */
suspend fun <T : Any> Flow<NetRequestStatus<T>>.successOrThrow(): NetRequestStatus.Success<T> {
    val nextTerminal = filterNot { it is NetRequestStatus.Pending }.first()
    if (nextTerminal.isError()) throw nextTerminal.error
    else return nextTerminal as NetRequestStatus.Success<T>
}

suspend fun <T : Any> Flow<NetRequestStatus<T>>.successOrThrow2(): NetRequestStatus.Success<T> {
    val nextTerminal = filterNot { it is NetRequestStatus.Pending }.first()
    if (nextTerminal is NetRequestStatus.Error) throw nextTerminal.error
    else return nextTerminal as NetRequestStatus.Success<T>
}
