package org.belkevglaz.features.upsource.model

import mu.*

private val logger = KotlinLogging.logger {}

/**
 *  todo:
 */
fun ReviewDescriptorDTO.branches(): Set<String> = branch ?: setOf(mergeFromBranch!!)

/**
 *
 */
fun ReviewDescriptorDTO.containsBranch(predicate: (String?) -> Boolean): Boolean = branches().any { predicate(it) }

/**
 *
 */
fun ReviewDescriptorDTO.readyToClose(): Boolean = run {

	val lastRevision = revisions.maxByOrNull { revision -> revision.revisionDate }?.revisionId
	val lastBuild = buildStatuses.find { s -> s.revisionId == lastRevision }
	val buildResult = lastBuild?.keys?.first()?.status

	logger.info { "(ReadyToClose) Review [${this.reviewId.reviewId}] last revision [$lastRevision] status [$buildResult]" }

	buildResult == 1 && isReadyToClose == true
}.also {
	if (it.not()) {
		val lastRevision = revisions.maxByOrNull { revision -> revision.revisionDate }
		logger.info {
			"(ReadyToClose) Review [${this.reviewId.reviewId}] failed revision [${
				lastRevision?.revisionCommitMessage?.replace("\n",
					" ")
			}]"
		}
	} else {
		logger.info { "(ReadyToClose) Review [${this.reviewId.reviewId}] to close? : ${it.toString().uppercase()}" }
	}
}