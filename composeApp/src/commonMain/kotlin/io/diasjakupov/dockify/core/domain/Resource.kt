package io.diasjakupov.dockify.core.domain

/**
 * A generic sealed interface for handling operation results in a type-safe manner.
 * Follows the Result pattern with explicit success and error types.
 *
 * @param D The type of data on success
 * @param E The type of error (must be a DataError)
 */
sealed interface Resource<out D, out E : DataError> {

    /**
     * Represents a successful operation with data.
     */
    data class Success<out D>(val data: D) : Resource<D, Nothing>

    /**
     * Represents a failed operation with an error.
     */
    data class Error<out E : DataError>(val error: E) : Resource<Nothing, E>
}

/**
 * Type alias for operations that don't return data on success.
 */
typealias EmptyResult<E> = Resource<Unit, E>

/**
 * Creates a successful Resource with the given data.
 */
fun <D> D.asSuccess(): Resource.Success<D> = Resource.Success(this)

/**
 * Creates an error Resource with the given error.
 */
fun <E : DataError> E.asError(): Resource.Error<E> = Resource.Error(this)

/**
 * Creates a successful empty result.
 */
fun emptySuccess(): Resource.Success<Unit> = Resource.Success(Unit)

/**
 * Maps the success data to a new type.
 */
inline fun <D, E : DataError, R> Resource<D, E>.map(transform: (D) -> R): Resource<R, E> {
    return when (this) {
        is Resource.Success -> Resource.Success(transform(data))
        is Resource.Error -> this
    }
}

/**
 * Maps the error to a new error type.
 */
inline fun <D, E : DataError, F : DataError> Resource<D, E>.mapError(transform: (E) -> F): Resource<D, F> {
    return when (this) {
        is Resource.Success -> this
        is Resource.Error -> Resource.Error(transform(error))
    }
}

/**
 * Executes the given block if this is a Success.
 */
inline fun <D, E : DataError> Resource<D, E>.onSuccess(action: (D) -> Unit): Resource<D, E> {
    if (this is Resource.Success) {
        action(data)
    }
    return this
}

/**
 * Executes the given block if this is an Error.
 */
inline fun <D, E : DataError> Resource<D, E>.onError(action: (E) -> Unit): Resource<D, E> {
    if (this is Resource.Error) {
        action(error)
    }
    return this
}

/**
 * Returns the success data or null if this is an error.
 */
fun <D, E : DataError> Resource<D, E>.getOrNull(): D? {
    return when (this) {
        is Resource.Success -> data
        is Resource.Error -> null
    }
}

/**
 * Returns the success data or the result of the default block if this is an error.
 */
inline fun <D, E : DataError> Resource<D, E>.getOrElse(default: (E) -> D): D {
    return when (this) {
        is Resource.Success -> data
        is Resource.Error -> default(error)
    }
}

/**
 * Flat maps the success data to another Resource.
 */
inline fun <D, E : DataError, R> Resource<D, E>.flatMap(transform: (D) -> Resource<R, E>): Resource<R, E> {
    return when (this) {
        is Resource.Success -> transform(data)
        is Resource.Error -> this
    }
}
