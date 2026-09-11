package com.sentinelcare.validation;

/**
 * Validation group for constraints that must hold when a record is created but
 * must not block later lifecycle updates, such as GDPR anonymization scrubbing
 * a date of birth.
 *
 * <p>Usage: validate the create path with
 * {@code @Validated({Default.class, OnCreate.class})} so the create-time
 * requirements are enforced. Hibernate's automatic pre-update validation runs
 * in the {@code Default} group only, so constraints moved to {@code OnCreate}
 * do not fire on updates.
 */
public interface OnCreate {
}
