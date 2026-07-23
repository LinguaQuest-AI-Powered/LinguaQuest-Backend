package gov.jets.iti.LinguaQuest.service;

import gov.jets.iti.LinguaQuest.dto.UserRankDto;
import gov.jets.iti.LinguaQuest.dto.response.LeaderBoardResponseDto;
import gov.jets.iti.LinguaQuest.entity.User;
import gov.jets.iti.LinguaQuest.enums.LeaderboardType;
import gov.jets.iti.LinguaQuest.repository.UserRepository;
import gov.jets.iti.LinguaQuest.util.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderBoardService {

    private final UserRepository userRepository;

    public LeaderBoardResponseDto getLeaderBoard(UserPrinciple userPrinciple, Integer page, Integer limit) {

        List<User> topThreeUsers = userRepository.findLeaderboard(PageRequest.of(0,3)).getContent();
        List<UserRankDto> topThreeUserRankDtos = toUserRankDtoList(topThreeUsers,1,userPrinciple.user().getId());

        Integer startRank =  page * limit + 1;
        List<User> pagingUsers = userRepository.findLeaderboard(PageRequest.of(page,limit)).getContent();
        List<UserRankDto> userRankDtos = toUserRankDtoList(pagingUsers,startRank,userPrinciple.user().getId());

        Integer myRank = userRepository.findRank(userPrinciple.user().getId(), userPrinciple.user().getXp());
        return new LeaderBoardResponseDto(myRank,topThreeUserRankDtos,userRankDtos);
    }
    private List<UserRankDto> toUserRankDtoList(List<User> users, Integer startRank, Long currentUserId) {
        List<UserRankDto> userRankDtoList = new ArrayList<>();
        for(Integer counter =0 ; counter < users.size() ; counter++) {
            User user = users.get(counter);
            UserRankDto userRankDto = new UserRankDto(
                    startRank + counter,
                    user.getId(),
                    user.getUsername(),
                    user.getPhoto(),
                    user.getLevel(),
                    user.getXp(),
                    user.getId().equals(currentUserId));
            userRankDtoList.add(userRankDto);
        }
        return userRankDtoList;
    }
}
