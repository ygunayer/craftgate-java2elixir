import com.yalingunayer.sandbox.Utils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class UtilsTest {
    @Test
    public void should_segmentize() {
        // given
        var input = "FooBar_baz.quuxHoge-piyo   zem-_ ZemABC";
        var expected = List.of(
                "Foo",
                "Bar",
                //"_",
                "baz",
                //".",
                "quux",
                "Hoge",
                //"-",
                "piyo",
                //"   ",
                "zem",
                //"-_ ",
                "Zem",
                "ABC"
        );

        // when
        var actual = Utils.segmentize(input);

        // then
        assertIterableEquals(expected, actual);
    }

    @Test
    public void should_convert_to_title_case() {
        // given
        var input = "FooBar_baz.quuxHoge-piyo   zem-_ ZemABC";
        var expected = "FooBarBazQuuxHogePiyoZemZemABC";

        // when
        var actual = Utils.toTitleCase(input);

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void should_convert_to_snake_case() {
        // given
        var input = "FooBar_baz.quuxHoge-piyo   zem-_ ZemABC";
        var expected = "foo_bar_baz_quux_hoge_piyo_zem_zem_abc";

        // when
        var actual = Utils.toSnakeCase(input);

        // then
        assertEquals(expected, actual);
    }
}
