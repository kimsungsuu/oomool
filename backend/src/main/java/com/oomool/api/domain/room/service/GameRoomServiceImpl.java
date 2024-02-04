package com.oomool.api.domain.room.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oomool.api.domain.player.dto.PlayerDto;
import com.oomool.api.domain.player.entity.Avatar;
import com.oomool.api.domain.player.entity.Player;
import com.oomool.api.domain.player.service.AvatarServiceImpl;
import com.oomool.api.domain.player.util.PlayerMapper;
import com.oomool.api.domain.room.dto.SettingOptionDto;
import com.oomool.api.domain.room.dto.TempRoomDto;
import com.oomool.api.domain.room.entity.GameRoom;
import com.oomool.api.domain.room.repository.GameRoomRepository;
import com.oomool.api.domain.room.util.GameRoomMapper;
import com.oomool.api.domain.room.util.MatchManitti;
import com.oomool.api.domain.room.util.UniqueCodeGenerator;
import com.oomool.api.domain.user.entity.User;
import com.oomool.api.domain.user.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameRoomServiceImpl implements GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final GameRoomMapper gameRoomMapper;

    // 연관관계 엔티티
    private final TempRoomRedisService tempRoomRedisService;
    private final UserService userService;
    private final AvatarServiceImpl avatarService;
    private final PlayerMapper playerMapper;

    @Override
    public String createGameRoom(String inviteCode) throws Exception {

        String roomUid = UniqueCodeGenerator.generateRandomString(8);                               // 방 UUID 생성
        TempRoomDto tempRoomDto = tempRoomRedisService.getTempRoom(inviteCode);         // 초대코드로 임시방 정보 조회

        // 대기방으로부터 저장해야할 목록
        GameRoom gameRoom = gameRoomMapper.dtoToEntity(roomUid, tempRoomDto.getSetting());
        List<PlayerDto> playerDtoList = tempRoomDto.getPlayers();

        // 마니또 매칭 결과
        Map<Integer, Integer> matchMap = MatchManitti.matchPair(playerDtoList);

        // Player Entity 객체로 변환
        for (PlayerDto playerDto : playerDtoList) {
            User user = userService.getUserById(playerDto.getUserId());
            Avatar avatar = avatarService.getAvatarByPlayerAvatarUrl(playerDto.getPlayerAvatarUrl());
            Player player = playerMapper.dtoToEntity(roomUid, playerDto, gameRoom, user, avatar,
                tempRoomDto.getMasterId(), matchMap.get(playerDto.getUserId()));
            gameRoom.getPlayers().add(player); // 문답방에 플레이어를 넣는다.
        }
        gameRoomRepository.save(gameRoom);

        return roomUid;
    }

    @Override
    public GameRoom getGameRoom(String roomUid) {
        return gameRoomRepository.findByRoomUid(roomUid)
            .orElseThrow(() -> new EntityNotFoundException("room UID가 존재하지 않습니다."));
    }

    @Override
    public Map<String, Object> getGameRoomDetail(String roomUid) {
        GameRoom gameRoom = getGameRoom(roomUid);
        SettingOptionDto settingOptionDto = gameRoomMapper.entityToSettingOptionDto(gameRoom);
        return Map.of(
            "room_uid", roomUid,
            "create_at", gameRoom.getCreateAt(),
            "setting", settingOptionDto,
            "players", gameRoom.getPlayers()
        );
    }

    @Override
    public SettingOptionDto getSettingOptionDto(String roomUid) {
        return gameRoomMapper.entityToSettingOptionDto(getGameRoom(roomUid));
    }

    @Override
    public SettingOptionDto getSettingOptionDtoByGameRoom(GameRoom gameRoom) {
        return gameRoomMapper.entityToSettingOptionDto(gameRoom);
    }
}
