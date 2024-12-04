package com.senials.favorites.service;

import com.senials.favorites.dto.FavoriteSelectDTO;
import com.senials.favorites.entity.Favorites;
import com.senials.favorites.repository.FavoritesRepository;
import com.senials.hobbyboard.entity.Hobby;
import com.senials.hobbyboard.repository.HobbyRepository;
import com.senials.user.entity.User;
import com.senials.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoritesService {
    private final FavoritesRepository favoritesRepository;
    private final HobbyRepository hobbyRepository;

    private final UserRepository userRepository;

    public FavoritesService(FavoritesRepository favoritesRepository, HobbyRepository hobbyRepository, UserRepository userRepository) {
        this.favoritesRepository = favoritesRepository;
        this.hobbyRepository = hobbyRepository;
        this.userRepository = userRepository;
    }


    // 사용자 관심사 제목 가져오기
    public List<String> getFavoriteTitlesByUser(int userNumber) {

        User user = userRepository.findById(userNumber)
                .orElseThrow(IllegalArgumentException::new);


        List<Favorites> favorites = favoritesRepository.findByUser(user);
        return favorites.stream()
                .map(favorite -> favorite.getHobby().getHobbyName()) // 취미명만 추출
                .collect(Collectors.toList());
    }

    // 사용자의 관심사 목록 가져오기
    public List<FavoriteSelectDTO> getAllHobbiesWithCategoryAndFavoriteStatus(int userNumber) {

        User user = userRepository.findById(userNumber)
                .orElseThrow(IllegalArgumentException::new);

        List<Favorites> userFavorites = favoritesRepository.findByUser(user);
        List<Hobby> allHobbies = hobbyRepository.findAll();

        // 각 취미에 대해 저장 여부와 카테고리명 포함한 DTO 반환
        return allHobbies.stream().map(hobby -> {
            boolean isFavorite = userFavorites.stream()
                    .anyMatch(fav -> fav.getHobby().getHobbyNumber() == hobby.getHobbyNumber());
            return new FavoriteSelectDTO(
                    hobby.getHobbyName(),
                    hobby.getCategory().getCategoryName(),
                    isFavorite
            );
        }).collect(Collectors.toList());
    }
    //관심사 등록
    public void addFavorite(int userNumber, int hobbyNumber) {

        // 취미 조회
        Hobby hobby = hobbyRepository.findById(hobbyNumber)
                .orElseThrow(IllegalArgumentException::new);

        User user = userRepository.findById(userNumber)
                .orElseThrow(IllegalArgumentException::new);

        // 사용자가 이미 해당 취미를 즐겨찾기 했는지 확인
        boolean exists = favoritesRepository.existsByUserAndHobby(user, hobby);
        if (exists) {
            throw new RuntimeException("이미 등록된 관심사입니다.");
        }
        // 관심사 추가
        Favorites favorite = new Favorites();
        favorite.initializeUser(user);
        favorite.initializeHobby(hobby);

        favoritesRepository.save(favorite);
    }

}