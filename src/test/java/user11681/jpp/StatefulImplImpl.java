package user11681.jpp;

import user11681.jpp.api.Getter;
import user11681.jpp.api.Setter;

public class StatefulImplImpl extends StatefulImpl implements StatefulInterface {
    public StatefulImplImpl(int thing) {
        super(0);
    }

    @Getter("field")
    public native Object yes();

    @Setter("field")
    public native void no(final Object object);

    @Getter("field")
    @Setter("field")
    public native Object sgetter(final Object object);
}
