package pl.socketbyte.sqldriver.test;

public class JVMWarmup {
    public static void load() {
        for (int i = 0; i < 100000; i++) {
            Dummy dummy = new Dummy();
            dummy.m();
        }
    }
    public static class Dummy {
        public void m() {
        }
    }
}
