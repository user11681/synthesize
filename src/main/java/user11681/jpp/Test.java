package user11681.jpp;

public class Test {
    public static void init() {
//        final StatefulInterface test = new StatefulInterface() {};
        final StatefulImpl impl = new StatefulImpl();
        final StatefulImplImpl implImpl = new StatefulImplImpl(43896);

        System.out.println(impl.energy());
        System.out.println(impl.width());
        System.out.println(implImpl.energy());
        System.out.println(implImpl.width());
        impl.set(1);
        impl.setWidth(11);
        implImpl.set(2);
        implImpl.setWidth(22);
        System.out.println(impl.energy());
        System.out.println(impl.width());
        System.out.println(implImpl.energy());
        System.out.println(implImpl.width());
    }
}
