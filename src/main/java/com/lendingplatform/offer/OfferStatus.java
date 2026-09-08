package com.lendingplatform.offer;

/**
 * All three of these states are actually reachable:
 *   AVAILABLE -- set when LoanOfferService.generateOffers() creates the offer
 *   SELECTED  -- the one offer the borrower picked (LoanOfferService.selectOffer)
 *   EXPIRED   -- every OTHER offer on the same application, flipped
 *                automatically the moment one is SELECTED - guarantees an
 *                application can never end up with two live offers.
 */
public enum OfferStatus {
    AVAILABLE,
    SELECTED,
    EXPIRED
}
