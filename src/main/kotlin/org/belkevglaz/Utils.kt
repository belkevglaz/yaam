package org.belkevglaz

/**
 * TODO : describe javaDocs.
 *
 * @author <a href="mailto:belkevlaz@gmail.com">Aksenov Ivan</a>
 * @since 0.0.1
 */

fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
	if (this != null) f(this)
}


inline fun <T:Any, R> whenNotNull(input: T?, callback: (T)->R): R? {
	return input?.let(callback)
}