package com.vzhilin.dbview.test;

import com.vzhilin.dbview.ui.autocomplete.SuggestionHelper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SuggestionTest {
    @Test
    public void test1() {
        SuggestionHelper helper = new SuggestionHelper("ABC DE.FG H", 3);

        assertThat(helper.beginClause(), equalTo(0));
        assertThat(helper.endClause(), equalTo(3));
        assertThat(helper.word(), equalTo("ABC"));
        assertThat(helper.select("XXX"), equalTo("XXX DE.FG H"));
    }

    @Test
    public void test2() {
        SuggestionHelper helper = new SuggestionHelper("", 0);

        assertThat(helper.beginClause(), equalTo(0));
        assertThat(helper.endClause(), equalTo(0));
        assertThat(helper.word(), equalTo(""));
    }

    @Test
    public void test3() {
        SuggestionHelper helper = new SuggestionHelper("A", 0);

        assertThat(helper.beginClause(), equalTo(0));
        assertThat(helper.endClause(), equalTo(0));
        assertThat(helper.word(), equalTo(""));
    }

    @Test
    public void test4() {
        SuggestionHelper helper = new SuggestionHelper("A", 1);

        assertThat(helper.beginClause(), equalTo(0));
        assertThat(helper.endClause(), equalTo(1));
        assertThat(helper.word(), equalTo("A"));
        assertThat(helper.select("XXX"), equalTo("XXX"));
    }

    @Test
    public void test5() {
        SuggestionHelper helper = new SuggestionHelper("ABC", 3);
        assertThat(helper.beginClause(), equalTo(0));
        assertThat(helper.endClause(), equalTo(3));
        assertThat(helper.word(), equalTo("ABC"));
        assertThat(helper.select("XXX"), equalTo("XXX"));
    }

    @Test
    public void test6() {
        SuggestionHelper helper = new SuggestionHelper("ABC.D ABC.D", 5);
        assertThat(helper.beginClause(), equalTo(4));
        assertThat(helper.endClause(), equalTo(5));
        assertThat(helper.word(), equalTo("D"));
        assertThat(helper.select("XXX"), equalTo("ABC.XXX ABC.D"));
    }
}
