package com.boottechsolutions.urlshortener.util;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = ALPHABET.length();

    public String encode(long id) {
        if (id <= 0) throw new IllegalArgumentException("ID must be positive, got: " + id);

        StringBuilder result = new StringBuilder();
        long remaining = id;

        while (remaining > 0) {
            result.append(ALPHABET.charAt((int) (remaining % BASE)));
            remaining /= BASE;
        }

        return result.reverse().toString();
    }

    public long decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            throw new IllegalArgumentException("Encoded value must not be blank");
        }

        long result = 0;
        for (char c : encoded.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index == -1) throw new IllegalArgumentException("Invalid character in encoded value: " + c);
            result = result * BASE + index;
        }

        return result;
    }
}
