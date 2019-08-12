package pl.socketbyte;

import java.util.List;

public class Test {

    @org.junit.Test
    public void test() {
        Twojstary stary = new Twojstary("Pijany");
        cos(stary);
        System.out.println(stary.getName());
    }

    void cos(Twojstary stary) {
        stary = new Twojstary("Nie pijany");
    }

    class Twojstary {
        private String name;
        public Twojstary(String name) {
            this.name = name;
        }
        public String getName() {
            return this.name;
        }
    }
}
