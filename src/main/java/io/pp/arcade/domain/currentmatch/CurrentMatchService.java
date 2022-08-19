package io.pp.arcade.domain.currentmatch;

import io.pp.arcade.domain.admin.dto.create.CurrentMatchCreateRequestDto;
import io.pp.arcade.domain.admin.dto.delete.CurrentMatchDeleteDto;
import io.pp.arcade.domain.admin.dto.update.CurrentMatchUpdateRequestDto;
import io.pp.arcade.domain.currentmatch.dto.*;
import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.game.GameRepository;
import io.pp.arcade.domain.game.dto.GameDto;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.slot.SlotRepository;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.slotteamuser.SlotTeamUser;
import io.pp.arcade.domain.slotteamuser.SlotTeamUserRepository;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CurrentMatchService {
    private final CurrentMatchRepository currentMatchRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final SlotRepository slotRepository;
    private final SlotTeamUserRepository slotTeamUserRepository;

    @Transactional
    public void addCurrentMatch(CurrentMatchAddDto addDto){
        User user = userRepository.findById(addDto.getUserId()).orElseThrow(() -> new BusinessException("E0001"));
        Slot slot = slotRepository.findById(addDto.getSlot().getId()).orElseThrow(() -> new BusinessException("E0001"));
        currentMatchRepository.save(CurrentMatch.builder()
                        .user(user)
                        .slot(slot)
                        .matchImminent(false)
                        .isMatched(false)
                        .build());
    }

    @Transactional
    public void modifyCurrentMatch(CurrentMatchModifyDto modifyDto){
        List<CurrentMatch> currentMatchList = currentMatchRepository.findAllBySlotId(modifyDto.getSlotId());
        currentMatchList.forEach(currentMatch -> {
            currentMatch.setMatchImminent(modifyDto.getMatchImminent());
            currentMatch.setIsMatched(modifyDto.getIsMatched());
        });
    }

    @Transactional
    public void saveGameInCurrentMatch(CurrentMatchSaveGameDto saveDto) {
        Game game = gameRepository.findById(saveDto.getGameId()).orElseThrow(() -> new BusinessException("E0001"));
        User user = userRepository.findById(saveDto.getUserId()).orElseThrow(() -> new BusinessException("E0001"));
        CurrentMatch currentMatch = currentMatchRepository.findByUser(user).orElse(null);
        currentMatch.setGame(game);
    }

    @Transactional
    public CurrentMatchDto findCurrentMatchByUser(UserDto userDto) {
        //User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("E0001"));
        CurrentMatch currentMatch = currentMatchRepository.findByUserId(userDto.getId()).orElse(null);
        CurrentMatchDto dto = null;

        if (currentMatch != null) {
            dto = CurrentMatchDto.builder()
                    .game(currentMatch.getGame() == null ? null : GameDto.from(currentMatch.getGame()))
                    .userId(userDto.getId())
                    .slot(SlotDto.from(currentMatch.getSlot()))
                    .matchImminent(currentMatch.getMatchImminent())
                    .isMatched(currentMatch.getIsMatched())
                    .build();
        }
        return dto;
    }

    @Transactional
    public CurrentMatchDto findCurrentMatchByIntraId(String intraId) {
        User user = userRepository.findByIntraId(intraId).orElseThrow(() -> new BusinessException("E0001"));
        CurrentMatch currentMatch = currentMatchRepository.findByUser(user).orElse(null);
        CurrentMatchDto dto = null;

        if (currentMatch != null) {
            dto = CurrentMatchDto.builder()
                    .game(currentMatch.getGame() == null ? null : GameDto.from(currentMatch.getGame()))
                    .userId(user.getId())
                    .slot(SlotDto.from(currentMatch.getSlot()))
                    .matchImminent(currentMatch.getMatchImminent())
                    .isMatched(currentMatch.getIsMatched())
                    .build();
        }
        return dto;
    }

    @Transactional
    public List<CurrentMatchDto> findCurrentMatchByGame(CurrentMatchFindDto findDto) {
        List<CurrentMatch> matches = currentMatchRepository.findAllByGameId(findDto.getGame().getId());
        List<CurrentMatchDto> currentMatchDtos = matches.stream().map(CurrentMatchDto::from).collect(Collectors.toList());
        return currentMatchDtos;
    }

    @Transactional
    public void removeCurrentMatch(CurrentMatchRemoveDto removeDto) {
        if (removeDto.getSlotId() == null) {
            User user = userRepository.findById(removeDto.getUserId()).orElseThrow(() -> new BusinessException("E0001"));
            currentMatchRepository.deleteByUser(user);
        } else {
            List<SlotTeamUser> slotTeamUsers = slotTeamUserRepository.findAllBySlotId(removeDto.getSlotId());
            slotTeamUsers.forEach(slotTeamUser -> {
                currentMatchRepository.deleteByUser(slotTeamUser.getUser());
            } );
        }
    }

    @Transactional
    public void createCurrentMatchByAdmin(CurrentMatchCreateRequestDto createRequestDto) {
        User user = userRepository.findById(createRequestDto.getUserId()).orElseThrow();
        Slot slot = slotRepository.findById(createRequestDto.getSlotId()).orElseThrow();
        Game game = createRequestDto.getGameId() == null ? null : gameRepository.findById(createRequestDto.getGameId()).orElseThrow();
        CurrentMatch currentMatch = CurrentMatch.builder()
                .user(user)
                .slot(slot)
                .game(game)
                .matchImminent(createRequestDto.getMatchImminent())
                .isMatched(createRequestDto.getIsMatched()).build();
        currentMatchRepository.save(currentMatch);
    }

    @Transactional
    public void updateCurrentMatchByAdmin(CurrentMatchUpdateRequestDto updateRequestDto) {
        CurrentMatch currentMatch = currentMatchRepository.findById(updateRequestDto.getCurrentMatchId()).orElseThrow();
        currentMatch.setMatchImminent(updateRequestDto.getMatchImminent());
        currentMatch.setIsMatched(updateRequestDto.getIsMatched());
    }

    @Transactional
    public void deleteCurrentMatchByAdmin(CurrentMatchDeleteDto deleteDto) {
        CurrentMatch currentMatch = currentMatchRepository.findById(deleteDto.getCurrentMatchId()).orElseThrow();
        currentMatchRepository.delete(currentMatch);
    }

    @Transactional
    public List<CurrentMatchDto> findCurrentMatchByAdmin(Pageable pageable) {
        Page<CurrentMatch> currentMatches = currentMatchRepository.findAllByOrderByIdDesc(pageable);
        List<CurrentMatchDto> currentMatchDtos = currentMatches.stream().map(CurrentMatchDto::from).collect(Collectors.toList());
        return currentMatchDtos;
    }
}
