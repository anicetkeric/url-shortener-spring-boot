package com.boottechsolutions.urlshortener.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class Base62EncoderTest {

    private final Base62Encoder encoder = new Base62Encoder();

    @ParameterizedTest
    @ValueSource(longs = {1, 10, 62, 100, 3844, 99999, 3_521_614_606_207L})
    void encodeThenDecode_shouldReturnOriginalId(long id) {
        String encoded = encoder.encode(id);
        assertThat(encoder.decode(encoded)).isEqualTo(id);
    }

    @Test
    void encode_id1_returnsSingleCharacter() {
        assertThat(encoder.encode(1L)).isEqualTo("1");
    }

    @Test
    void encode_id62_returnsTwoCharacters() {
        // 62 in base-62 is "10"
        assertThat(encoder.encode(62L)).isEqualTo("10");
    }

    @Test
    void encode_maxSevenCharIds_producesCompactCodes() {
        // 62^7 - 1 should still be 7 characters
        String encoded = encoder.encode(3_521_614_606_207L);
        assertThat(encoded).hasSizeLessThanOrEqualTo(7);
    }

    @Test
    void encode_zeroOrNegative_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> encoder.encode(0L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> encoder.encode(-1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void decode_invalidCharacter_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> encoder.decode("abc!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid character");
    }

    @Test
    void decode_blank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> encoder.decode(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
