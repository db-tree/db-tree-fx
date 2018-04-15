package com.vzhilin.dbview.test;

import java.util.ArrayList;

import org.junit.Test;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class ComplexBindingTest {
    @Test
    public void test() {
        ListProperty<String> first = new SimpleListProperty<String>(FXCollections.observableArrayList(new ArrayList<>()));
        first.add("A");
        first.add("B");
        first.add("C");
        first.add("D");


        ListProperty<String> second = new SimpleListProperty<String>(FXCollections.observableArrayList(new ArrayList<>()));
        second.bind(first);
        first.add("E");
        System.err.println(second.get());
    }

    @Test
    public void testConversion() {
        ListProperty<A> first = new SimpleListProperty<A>(FXCollections.observableArrayList(new ArrayList<>()));
        first.add(new A("1"));
        first.add(new A("2"));
        first.add(new A("3"));
        first.add(new A("4"));

        ListProperty<B> second = new SimpleListProperty<B>(FXCollections.observableArrayList(new ArrayList<>()));
    }

    private final static class A {
        private String value;

        public A(String value) {
            this.value = value;
        }

        @Override public String toString() {
            return "A{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    private final static class B {
        private String value;

        public B(String value) {
            this.value = value;
        }

        @Override public String toString() {
            return "B{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }
}
