package ee.bcskoolitus.persistance.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    @Query("select l from Location l order by l.id")
    List<Location> findAllLocationsOrderedById();
}
