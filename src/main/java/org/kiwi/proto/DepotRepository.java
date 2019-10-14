package org.kiwi.proto;

import java.util.Optional;
import org.kiwi.proto.FloatingAverageProtos.Depot;

public interface DepotRepository {

    Optional<Depot> load(String id);

    void store(Depot depot);
}
