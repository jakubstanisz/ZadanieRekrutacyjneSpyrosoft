package com.jakubstaniszewski.evcharging.backend.domain.specification;

/**
 * Specification contract for enforcing a single business invariant during booking creation.
 */
@FunctionalInterface
public interface BookingSpecification {

    /**
     * Validates the booking candidate against this specification's business rule.
     *
     * @param candidate proposed booking context
     */
    void validate(BookingCandidate candidate);
}
