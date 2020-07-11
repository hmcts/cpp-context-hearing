package uk.gov.justice.ccr.notepad.view.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;

public class PartsResolverTest {
    @Test
    public void shouldResolvePartsCorrectly() {
        assertThat(Arrays.asList("UPWR", "80", "hrs").containsAll(new PartsResolver().getParts("UPWR 80 hrs").stream().map(p -> p.getValue()).collect(Collectors.toList())), is(true));
        assertThat(Arrays.asList("ddd", "ddd").containsAll(new PartsResolver().getParts("ddd ddd").stream().map(p -> p.getValue()).collect(Collectors.toList())), is(true));
        assertThat(Arrays.asList("ddd", "[ddd]", "ddd").containsAll(new PartsResolver().getParts("ddd [ddd] ddd").stream().map(p -> p.getValue()).collect(Collectors.toList())), is(true));
        assertThat(Arrays.asList("ddd", "[ddd ddd ]", "ddd").containsAll(new PartsResolver().getParts("ddd [ddd ddd ] ddd").stream().map(p -> p.getValue()).collect(Collectors.toList())), is(true));
        assertThat(Arrays.asList("ddd", "[ddd ddd ]", "ddd").containsAll(new PartsResolver().getParts("ddd ddd [ddd ddd ]").stream().map(p -> p.getValue()).collect(Collectors.toList())), is(true));
        assertThat(Arrays.asList("ddd", "[ddd [ddd ]", "[sss]", "ddd").containsAll(new PartsResolver().getParts("ddd ddd [ddd [ddd ] [sss]").stream().map(p -> p.getValue()).collect(Collectors.toList())), is(true));
        assertThat(Arrays.asList("ddd[ddd]", "[ddd ddd ]", "ffff").containsAll(new PartsResolver().getParts("ddd[ddd] [ddd ddd ] ffff").stream().map(p -> p.getValue()).collect(Collectors.toList())), is(true));
        assertThat(Arrays.asList("dd",  "[abc]", "[gh  efg ]", "xyz").containsAll(new PartsResolver().getParts("  dd    [abc]  [gh  efg ] xyz ").stream().map(p -> p.getValue()).collect(Collectors.toList())), is(true));
    }

}
