package user11681.jpp;

import user11681.jpp.synthesis.Declare;

@Declare(name = "test", descriptor = "I")
public class StatefulImpl implements ExtraStatefulInterface {
    private int anotherThing = 123;

    public StatefulImpl() {
        {
            int thing = 123;
        }

        {
            int thing = 546;
            System.out.println("constructing.");
        }
    }

    public StatefulImpl(final int thing) {
    }

//    @Getter("test")
//    public native int getTest();
//
//    @Setter("test")
//    public native void setTest(int test);

    {
        System.out.println("nothing");
    }

    {
        System.out.println("something");
    }
}
