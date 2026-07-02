package com.sales.visits.app.sales.utils;

public final class AddressPatterns {
    private AddressPatterns() {}
    // This example must be set in above the input field of the address in react "111 Broadway, New York, NY" -> Street, City, ST
    public static final String ADDRESS_REGEX =
            "^[A-Za-z0-9.\\-\\s]+,\\s*[A-Za-z\\s]+,\\s*[A-Z]{2}$";
}
