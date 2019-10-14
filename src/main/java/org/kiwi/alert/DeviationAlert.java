package org.kiwi.alert;

import java.util.Collection;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public interface DeviationAlert {

    void alert(FloatingAverage floatingAverage);

    void alert(Collection<FloatingAverage> floatingAverage);
}
