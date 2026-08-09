package io.github.nidhivgandhi.wordsmith.group;

import io.github.nidhivgandhi.wordsmith.group.dto.CreateGroupRequest;
import io.github.nidhivgandhi.wordsmith.group.dto.NearbyGroupResponse;
import io.github.nidhivgandhi.wordsmith.group.dto.NearbySearchRequest;
import io.github.nidhivgandhi.wordsmith.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WritingGroupService {

    private final WritingGroupRepository groupRepo;
    private final UserRepository userRepo;

    public WritingGroupService(WritingGroupRepository groupRepo, UserRepository userRepo) {
        this.groupRepo = groupRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public WritingGroup createGroup(CreateGroupRequest req, Long ownerId) {
        WritingGroup group = new WritingGroup();
        group.setOwner(userRepo.getReferenceById(ownerId));
        group.setName(req.name());
        group.setDescription(req.description());
        group.setCity(req.city());
        if (req.meetingFormat() != null) {
            group.setMeetingFormat(req.meetingFormat());
        }
        group.setLocation(GeoUtils.point(req.latitude(), req.longitude()));
        return groupRepo.save(group);
    }

    /**
     * Miles in, miles out -- metres exist only between here and the database.
     * Read-only transaction: it skips Hibernate's dirty-check flush on the way out,
     * which is pointless work for a query that never modifies anything.
     */
    @Transactional(readOnly = true)
    public List<NearbyGroupResponse> findNearby(NearbySearchRequest req) {
        double radiusMeters = GeoUtils.milesToMeters(req.radiusMiles());
        return groupRepo.findWithinRadius(req.lat(), req.lon(), radiusMeters)
                .stream()
                .map(NearbyGroupResponse::from)
                .toList();
    }
}
